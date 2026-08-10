package com.example.formula

import com.example.data.model.MarketRecordEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class FormulaResult(
    val jodi1: String,
    val jodi2: String,
    val product: Long,
    val quotientString: String,
    val otcDigits: List<Int>, // e.g. [4, 9, 7]
    val otcFormatted: String, // e.g. "4, 9, 7"
    val superJodis: String, // e.g. "49, 97, 47"
    val step1Description: String,
    val step2Description: String,
    val step3Description: String
)

data class BacktestEvaluation(
    val record: MarketRecordEntity,
    val formulaResult: FormulaResult?,
    val isPass: Boolean,
    val matchedDigit: Int? = null,
    val superJodiHit: Boolean = false,
    val dayOfWeekHindi: String
)

data class WeeklyStats(
    val totalEvaluated: Int,
    val passDays: Int,
    val failDays: Int,
    val accuracyPercentage: Float,
    val dailyBreakdown: Map<String, Pair<Int, Int>> // DayName -> Pair(PassCount, TotalCount)
)

data class LockedMarketRule(
    val marketKey: String,
    val index1: Int,
    val index2: Int,
    val operation: String,
    val multiplier: Int,
    val ruleDescription: String
)

object FormulaEngine {

    fun getLockedMarketRule(marketName: String): LockedMarketRule {
        val m = marketName.uppercase()
        return when {
            m.contains("KALYAN") -> LockedMarketRule(
                marketKey = "KALYAN",
                index1 = 2,
                index2 = 3,
                operation = "ADD_MUL",
                multiplier = 9,
                ruleDescription = "Jodi(i-2) & Jodi(i-3) [ADD_MUL] × 9"
            )
            m.contains("TIME") -> LockedMarketRule(
                marketKey = "TIME-BAZAR",
                index1 = 2,
                index2 = 3,
                operation = "ADD_MUL",
                multiplier = 2,
                ruleDescription = "Jodi(i-2) & Jodi(i-3) [ADD_MUL] × 2"
            )
            m.contains("MILAN") -> LockedMarketRule(
                marketKey = "MILAN",
                index1 = 1,
                index2 = 3,
                operation = "MUL",
                multiplier = 8,
                ruleDescription = "Jodi(i-1) & Jodi(i-3) [MUL] × 8"
            )
            m.contains("SRIDEVI") || m.contains("SHRIDEVI") -> LockedMarketRule(
                marketKey = "SRIDEVI",
                index1 = 1,
                index2 = 4,
                operation = "ADD_MUL",
                multiplier = 9,
                ruleDescription = "Jodi(i-1) & Jodi(i-4) [ADD_MUL] × 9"
            )
            else -> LockedMarketRule(
                marketKey = marketName.uppercase(),
                index1 = 1,
                index2 = 2,
                operation = "ADD_MUL",
                multiplier = 8,
                ruleDescription = "Jodi(i-1) & Jodi(i-2) [ADD_MUL] × 8"
            )
        }
    }

    fun calculateLockedMarketFormula(
        marketName: String,
        jodi1Str: String,
        jodi2Str: String
    ): FormulaResult {
        val rule = getLockedMarketRule(marketName)
        val j1 = jodi1Str.toIntOrNull() ?: 0
        val j2 = jodi2Str.toIntOrNull() ?: 0

        val product: Long = if (rule.operation == "ADD_MUL" && j1 > 0) {
            ((j1 + j2) * j1).toLong()
        } else {
            (j1 * j2).toLong()
        }

        val div = if (rule.multiplier != 0) rule.multiplier else 1
        val quotient = product / div

        val rawQuotString = quotient.toString()
        val first3Str = if (rawQuotString.length >= 3) {
            rawQuotString.substring(0, 3)
        } else {
            rawQuotString.padEnd(3, '0')
        }

        val digitsList = first3Str.mapNotNull { it.digitToIntOrNull() }
        val otcFormatted = digitsList.joinToString(", ")

        val superJodiList = mutableListOf<String>()
        val d0 = digitsList.getOrElse(0) { 4 }
        val d1 = digitsList.getOrElse(1) { 9 }
        val d2 = digitsList.getOrElse(2) { 7 }

        superJodiList.add("%d%d".format(d0, d1))
        superJodiList.add("%d%d".format(d1, d0))
        superJodiList.add("%d%d".format(d1, d2))
        superJodiList.add("%d%d".format(d2, d1))
        superJodiList.add("%d%d".format(d0, d2))
        superJodiList.add("%d%d".format(d2, d0))

        val superJodisFormatted = superJodiList.distinct().take(4).joinToString(", ")

        val j1Formatted = j1StrPad(jodi1Str)
        val j2Formatted = j1StrPad(jodi2Str)

        val step1 = if (rule.operation == "ADD_MUL") {
            "#( ($j1Formatted + $j2Formatted) × $j1Formatted = $product )"
        } else {
            "#( $j1Formatted × $j2Formatted = $product )"
        }
        val step2 = "#( $product ÷ ${rule.multiplier} = $quotient )"
        val step3 = "#( otc $first3Str )"

        return FormulaResult(
            jodi1 = j1Formatted,
            jodi2 = j2Formatted,
            product = product,
            quotientString = first3Str,
            otcDigits = digitsList,
            otcFormatted = otcFormatted,
            superJodis = if (superJodisFormatted.isNotEmpty()) superJodisFormatted else "00, 05",
            step1Description = step1,
            step2Description = step2,
            step3Description = step3
        )
    }

    fun evaluateLockedRecord(
        marketName: String,
        currentRecord: MarketRecordEntity,
        validHistoryBefore: List<MarketRecordEntity>
    ): BacktestEvaluation {
        val dayHindi = getDayOfWeekHindi(currentRecord.date)

        if (currentRecord.isHoliday || currentRecord.jodi.contains("*") || currentRecord.jodi == "**") {
            return BacktestEvaluation(
                record = currentRecord,
                formulaResult = null,
                isPass = false,
                dayOfWeekHindi = dayHindi
            )
        }

        val rule = getLockedMarketRule(marketName)
        val rec1 = validHistoryBefore.getOrNull(validHistoryBefore.size - rule.index1)
        val rec2 = validHistoryBefore.getOrNull(validHistoryBefore.size - rule.index2)

        if (rec1 == null || rec2 == null) {
            return BacktestEvaluation(
                record = currentRecord,
                formulaResult = null,
                isPass = false,
                dayOfWeekHindi = dayHindi
            )
        }

        val formulaResult = calculateLockedMarketFormula(marketName, rec1.jodi, rec2.jodi)

        val actualJodi = currentRecord.jodi.trim()
        val openDigit = actualJodi.getOrNull(0)?.digitToIntOrNull()
        val closeDigit = actualJodi.getOrNull(1)?.digitToIntOrNull()

        val otcDigits = formulaResult.otcDigits

        var isPass = false
        var matchedDigit: Int? = null

        for (d in otcDigits) {
            if (d == openDigit || d == closeDigit) {
                isPass = true
                matchedDigit = d
                break
            }
        }

        val superJodiHit = formulaResult.superJodis.contains(actualJodi)

        return BacktestEvaluation(
            record = currentRecord,
            formulaResult = formulaResult,
            isPass = isPass,
            matchedDigit = matchedDigit,
            superJodiHit = superJodiHit,
            dayOfWeekHindi = dayHindi
        )
    }

    fun calculateFormula(
        jodi1Str: String,
        jodi2Str: String,
        divisor: Int = 8
    ): FormulaResult {
        val j1 = jodi1Str.toIntOrNull() ?: 0
        val j2 = jodi2Str.toIntOrNull() ?: 0

        val product = (j1 * j2).toLong()
        val quotient = if (divisor != 0) product / divisor else product

        val rawQuotString = quotient.toString()
        val first3Str = if (rawQuotString.length >= 3) {
            rawQuotString.substring(0, 3)
        } else {
            rawQuotString.padEnd(3, '0')
        }

        val digitsList = first3Str.mapNotNull { it.digitToIntOrNull() }
        val otcFormatted = digitsList.joinToString(", ")

        // Generate Super Jodis from OTC digits - Exactly 4 Jodi Pairs (Safe Non-blocking)
        val superJodiList = mutableListOf<String>()
        val d0 = digitsList.getOrElse(0) { 4 }
        val d1 = digitsList.getOrElse(1) { 9 }
        val d2 = digitsList.getOrElse(2) { 7 }

        superJodiList.add("%d%d".format(d0, d1))
        superJodiList.add("%d%d".format(d1, d0))
        superJodiList.add("%d%d".format(d1, d2))
        superJodiList.add("%d%d".format(d2, d1))
        superJodiList.add("%d%d".format(d0, d2))
        superJodiList.add("%d%d".format(d2, d0))

        // Cut Jodis
        superJodiList.add("%d%d".format(d0, (d0 + 5) % 10))
        superJodiList.add("%d%d".format(d1, (d1 + 5) % 10))
        superJodiList.add("%d%d".format(d2, (d2 + 5) % 10))

        // Standard Fallbacks
        superJodiList.addAll(listOf("05", "50", "16", "61", "27", "72", "38", "83", "49", "94"))

        val superJodisFormatted = superJodiList.distinct().take(4).joinToString(", ")

        val j1Formatted = j1StrPad(jodi1Str)
        val j2Formatted = j1StrPad(jodi2Str)

        val step1 = "#( $j1Formatted × $j2Formatted = $product )"
        val step2 = "#( $product ÷ $divisor = $quotient )"
        val step3 = "#( otc $first3Str )"

        return FormulaResult(
            jodi1 = j1Formatted,
            jodi2 = j2Formatted,
            product = product,
            quotientString = first3Str,
            otcDigits = digitsList,
            otcFormatted = otcFormatted,
            superJodis = if (superJodisFormatted.isNotEmpty()) superJodisFormatted else "00, 05",
            step1Description = step1,
            step2Description = step2,
            step3Description = step3
        )
    }

    private fun j1StrPad(str: String): String {
        val trimmed = str.trim()
        return if (trimmed.length == 1) "0$trimmed" else trimmed
    }

    fun getMarketFormulaIndices(marketName: String): Pair<Int, Int> {
        val m = marketName.uppercase()
        return when {
            m.contains("KALYAN") -> Pair(2, 3)
            m.contains("TIME") -> Pair(2, 3)
            m.contains("MILAN") -> Pair(1, 3)
            m.contains("SRIDEVI") || m.contains("SHRIDEVI") -> Pair(1, 4)
            else -> Pair(1, 2)
        }
    }

    fun calculateMarketSpecificFormula(
        marketName: String,
        jodi1Str: String,
        jodi2Str: String,
        customMultiplier: Int? = null
    ): FormulaResult {
        val j1 = jodi1Str.toIntOrNull() ?: 0
        val j2 = jodi2Str.toIntOrNull() ?: 0

        val upperMarket = marketName.uppercase()
        val defaultDiv = when {
            upperMarket.contains("KALYAN") -> 9
            upperMarket.contains("TIME") -> 2
            upperMarket.contains("MILAN") -> 8
            upperMarket.contains("SHRIDEVI") || upperMarket.contains("SRIDEVI") -> 9
            else -> customMultiplier ?: 8
        }

        val useAddMul = upperMarket.contains("KALYAN") || upperMarket.contains("TIME") || upperMarket.contains("SHRIDEVI") || upperMarket.contains("SRIDEVI")

        val product: Long = if (useAddMul && j1 > 0) {
            ((j1 + j2) * j1).toLong()
        } else {
            (j1 * j2).toLong()
        }

        val quotient = if (defaultDiv != 0) product / defaultDiv else product

        val rawQuotString = quotient.toString()
        val first3Str = if (rawQuotString.length >= 3) {
            rawQuotString.substring(0, 3)
        } else {
            rawQuotString.padEnd(3, '0')
        }

        val digitsList = first3Str.mapNotNull { it.digitToIntOrNull() }
        val otcFormatted = digitsList.joinToString(", ")

        val superJodiList = mutableListOf<String>()
        val d0 = digitsList.getOrElse(0) { 4 }
        val d1 = digitsList.getOrElse(1) { 9 }
        val d2 = digitsList.getOrElse(2) { 7 }

        superJodiList.add("%d%d".format(d0, d1))
        superJodiList.add("%d%d".format(d1, d0))
        superJodiList.add("%d%d".format(d1, d2))
        superJodiList.add("%d%d".format(d2, d1))
        superJodiList.add("%d%d".format(d0, d2))
        superJodiList.add("%d%d".format(d2, d0))

        val superJodisFormatted = superJodiList.distinct().take(4).joinToString(", ")

        val j1Formatted = j1StrPad(jodi1Str)
        val j2Formatted = j1StrPad(jodi2Str)

        val opSymbol = if (useAddMul) "+" else "×"
        val step1 = "#( ($j1Formatted $opSymbol $j2Formatted) = $product )"
        val step2 = "#( $product ÷ $defaultDiv = $quotient )"
        val step3 = "#( otc $first3Str )"

        return FormulaResult(
            jodi1 = j1Formatted,
            jodi2 = j2Formatted,
            product = product,
            quotientString = first3Str,
            otcDigits = digitsList,
            otcFormatted = otcFormatted,
            superJodis = if (superJodisFormatted.isNotEmpty()) superJodisFormatted else "00, 05",
            step1Description = step1,
            step2Description = step2,
            step3Description = step3
        )
    }

    fun evaluateRecord(
        currentRecord: MarketRecordEntity,
        prevRecord1: MarketRecordEntity?,
        prevRecord2: MarketRecordEntity?,
        divisor: Int = 8
    ): BacktestEvaluation {
        val dayHindi = getDayOfWeekHindi(currentRecord.date)

        if (prevRecord1 == null || prevRecord2 == null || currentRecord.isHoliday) {
            return BacktestEvaluation(
                record = currentRecord,
                formulaResult = null,
                isPass = false,
                dayOfWeekHindi = dayHindi
            )
        }

        val formulaResult = calculateFormula(prevRecord2.jodi, prevRecord1.jodi, divisor)

        // Result jodi digits
        val actualJodi = currentRecord.jodi.trim()
        val openDigit = actualJodi.getOrNull(0)?.digitToIntOrNull()
        val closeDigit = actualJodi.getOrNull(1)?.digitToIntOrNull()

        val otcDigits = formulaResult.otcDigits

        // Check match
        var isPass = false
        var matchedDigit: Int? = null

        for (d in otcDigits) {
            if (d == openDigit || d == closeDigit) {
                isPass = true
                matchedDigit = d
                break
            }
        }

        // Check if super jodi hit
        val superJodiHit = formulaResult.superJodis.contains(actualJodi)

        return BacktestEvaluation(
            record = currentRecord,
            formulaResult = formulaResult,
            isPass = isPass,
            matchedDigit = matchedDigit,
            superJodiHit = superJodiHit,
            dayOfWeekHindi = dayHindi
        )
    }

    private val SUPPORTED_DATE_FORMATS = listOf(
        "dd-MM-yyyy",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "yyyy/MM/dd",
        "d-M-yyyy",
        "d/M/yyyy",
        "dd.MM.yyyy"
    )

    fun normalizeDateStr(rawDate: String): String {
        val trimmed = rawDate.trim()
        if (trimmed.isBlank()) return getTodayDateStr()

        for (fmt in SUPPORTED_DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US)
                sdf.isLenient = true
                val parsed = sdf.parse(trimmed)
                if (parsed != null) {
                    val outSdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    return outSdf.format(parsed)
                }
            } catch (_: Exception) {}
        }
        return trimmed
    }

    private fun getTodayDateStr(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    fun parseDateToTimestamp(dateStr: String): Long {
        val trimmed = dateStr.trim()
        if (trimmed.isBlank()) return System.currentTimeMillis()

        for (fmt in SUPPORTED_DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US)
                sdf.isLenient = true
                val parsed = sdf.parse(trimmed)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    fun getDayOfWeekHindi(dateStr: String): String {
        val trimmed = dateStr.trim()
        var date: java.util.Date? = null
        for (fmt in SUPPORTED_DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US)
                sdf.isLenient = true
                date = sdf.parse(trimmed)
                if (date != null) break
            } catch (_: Exception) {}
        }

        if (date == null) return "Somvaar (Mon)"

        return try {
            val cal = Calendar.getInstance()
            cal.time = date
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Somvaar (Mon)"
                Calendar.TUESDAY -> "Mangalvaar (Tue)"
                Calendar.WEDNESDAY -> "Budhvaar (Wed)"
                Calendar.THURSDAY -> "Guruvaar (Thu)"
                Calendar.FRIDAY -> "Shukravaar (Fri)"
                Calendar.SATURDAY -> "Shanivaar (Sat)"
                Calendar.SUNDAY -> "Ravivaar (Sun)"
                else -> "Somvaar (Mon)"
            }
        } catch (e: Exception) {
            "Somvaar (Mon)"
        }
    }

    fun calculateSpecial30JodiFormula(
        lastJodiStr: String
    ): FormulaResult {
        val jodiVal = lastJodiStr.toIntOrNull() ?: 0
        val product = 30L * jodiVal
        val resultVal = product / 2L
        val resultStr = resultVal.toString()

        val rawDigits = resultStr.mapNotNull { it.digitToIntOrNull() }
        val otcDigits = rawDigits.distinct()
        val otcFormatted = otcDigits.joinToString(" - ")

        val superJodiList = mutableListOf<String>()
        if (otcDigits.size >= 2) {
            for (i in otcDigits.indices) {
                for (j in otcDigits.indices) {
                    if (i != j) {
                        superJodiList.add("${otcDigits[i]}${otcDigits[j]}")
                    }
                }
            }
        } else if (otcDigits.size == 1) {
            val d = otcDigits[0]
            superJodiList.add("$d$d")
            superJodiList.add("$d${(d + 5) % 10}")
        }
        val superJodisFormatted = superJodiList.distinct().take(4).joinToString(", ")

        val jodiPad = j1StrPad(lastJodiStr)
        val step1 = "#( 30 × $jodiPad = $product )"
        val step2 = "#( $product ÷ 2 = $resultVal )"
        val step3 = "#( Dynamic OTC [${otcDigits.size} Digits]: $otcFormatted )"

        return FormulaResult(
            jodi1 = "30",
            jodi2 = jodiPad,
            product = product,
            quotientString = resultStr,
            otcDigits = otcDigits,
            otcFormatted = otcFormatted,
            superJodis = if (superJodisFormatted.isNotEmpty()) superJodisFormatted else "00, 05",
            step1Description = step1,
            step2Description = step2,
            step3Description = step3
        )
    }

    fun evaluateSpecial30Record(
        currentRecord: MarketRecordEntity,
        prevRecord: MarketRecordEntity?
    ): BacktestEvaluation {
        val dayHindi = getDayOfWeekHindi(currentRecord.date)

        if (prevRecord == null || currentRecord.isHoliday || currentRecord.jodi.contains("*") || currentRecord.jodi == "**") {
            return BacktestEvaluation(
                record = currentRecord,
                formulaResult = null,
                isPass = false,
                dayOfWeekHindi = dayHindi
            )
        }

        val formulaResult = calculateSpecial30JodiFormula(prevRecord.jodi)

        val actualJodi = currentRecord.jodi.trim()
        val openDigit = actualJodi.getOrNull(0)?.digitToIntOrNull()
        val closeDigit = actualJodi.getOrNull(1)?.digitToIntOrNull()

        val otcDigits = formulaResult.otcDigits

        var isPass = false
        var matchedDigit: Int? = null

        for (d in otcDigits) {
            if (d == openDigit || d == closeDigit) {
                isPass = true
                matchedDigit = d
                break
            }
        }

        val superJodiHit = formulaResult.superJodis.contains(actualJodi)

        return BacktestEvaluation(
            record = currentRecord,
            formulaResult = formulaResult,
            isPass = isPass,
            matchedDigit = matchedDigit,
            superJodiHit = superJodiHit,
            dayOfWeekHindi = dayHindi
        )
    }

    fun calculateWeeklyStats(evaluations: List<BacktestEvaluation>): WeeklyStats {
        val validEvals = evaluations.filter { it.formulaResult != null && !it.record.isHoliday }
        val total = validEvals.size
        val passes = validEvals.count { it.isPass }
        val fails = total - passes
        val acc = if (total > 0) (passes.toFloat() / total) * 100f else 0f

        val daysOrder = listOf(
            "Somvaar (Mon)" to "Som (Mon)",
            "Mangalvaar (Tue)" to "Mangal (Tue)",
            "Budhvaar (Wed)" to "Budh (Wed)",
            "Guruvaar (Thu)" to "Guru (Thu)",
            "Shukravaar (Fri)" to "Shukra (Fri)",
            "Shanivaar (Sat)" to "Shani (Sat)",
            "Ravivaar (Sun)" to "Ravi (Sun)"
        )

        val breakdown = mutableMapOf<String, Pair<Int, Int>>()
        for ((fullDay, shortLabel) in daysOrder) {
            val dayEvals = validEvals.filter { it.dayOfWeekHindi == fullDay }
            val dayPasses = dayEvals.count { it.isPass }
            val dayTotal = dayEvals.size
            breakdown[shortLabel] = Pair(dayPasses, dayTotal)
        }

        return WeeklyStats(
            totalEvaluated = total,
            passDays = passes,
            failDays = fails,
            accuracyPercentage = acc,
            dailyBreakdown = breakdown
        )
    }
}
