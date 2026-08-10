package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiModuleConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val provider: String, // "Gemini", "Claude", "Grok", "OpenCode.ai", "DeepSeek", "Nemotron", "OpenCode Zen"
    val modelName: String, // e.g. "gemini-3.5-flash", "claude-3-5-sonnet", etc.
    val apiKey: String,
    val customEndpoint: String = "",
    val isActive: Boolean = false,
    val lastTestedStatus: String = "Not Tested" // "Connected ✅", "Failed ❌", "Not Tested"
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user", "ai", "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val attachmentName: String? = null,
    val attachmentType: String? = null, // "IMAGE", "PDF", "FILE"
    val attachmentUri: String? = null
)

object AiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun testConnection(config: AiModuleConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (config.apiKey.isBlank()) {
            return@withContext Pair(false, "API key is empty")
        }
        try {
            val responseText = sendChatRequest(
                config = config,
                messages = listOf(ChatMessage(sender = "user", text = "Hello! Test connection.")),
                marketContext = "Test mode",
                language = "English"
            )
            if (responseText.isNotBlank() && !responseText.contains("Error [") && !responseText.startsWith("Error:")) {
                Pair(true, "Connected ✅")
            } else {
                Pair(false, responseText)
            }
        } catch (e: Exception) {
            Pair(false, "Error ❌: ${e.message ?: "Connection failed"}")
        }
    }

    suspend fun sendChatRequest(
        config: AiModuleConfig,
        messages: List<ChatMessage>,
        marketContext: String,
        language: String = "English"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = config.apiKey.trim()
        val provider = config.provider.uppercase()
        val modelName = config.modelName.ifBlank { "gemini-3.5-flash" }

        val langInstruction = if (language.equals("Hindi", ignoreCase = true)) {
            "Language Directive: MUST reply in fluent Hindi / Hinglish (हिंदी) for Indian market users."
        } else {
            "Language Directive: Reply in clear English."
        }

        val systemPrompt = """
            You are A23 PRO AI - an expert Market Analyst, Math Formula Specialist, and Intelligent Assistant for Kalyan, Sridevi, Milan, Time Bazar, and custom market data analysis.
            $marketContext
            $langInstruction
            Provide precise, helpful, and concise responses.
        """.trimIndent()

        try {
            if (provider.contains("GEMINI")) {
                return@withContext callGeminiRest(apiKey, modelName, messages, systemPrompt)
            } else if (provider.contains("CLAUDE")) {
                return@withContext callClaudeRest(apiKey, modelName, config.customEndpoint, messages, systemPrompt)
            } else {
                return@withContext callOpenAiCompatibleRest(apiKey, modelName, config.customEndpoint, provider, messages, systemPrompt)
            }
        } catch (e: Exception) {
            return@withContext "Error: ${e.localizedMessage ?: "Failed to connect to AI provider"}"
        }
    }

    private fun callGeminiRest(
        apiKey: String,
        modelName: String,
        messages: List<ChatMessage>,
        systemPrompt: String
    ): String {
        val targetModel = when {
            modelName.contains("3.5") || modelName.contains("flash") -> "gemini-3.5-flash"
            modelName.contains("3.1") || modelName.contains("pro") -> "gemini-3.1-pro-preview"
            else -> modelName.ifBlank { "gemini-3.5-flash" }
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"

        val jsonBody = JSONObject()
        val contentsArray = JSONArray()

        val fullPromptBuilder = StringBuilder()
        fullPromptBuilder.append(systemPrompt).append("\n\n")

        for (m in messages.takeLast(10)) {
            val roleName = if (m.sender == "user") "User" else "Assistant"
            val attachStr = if (m.attachmentName != null) " [Attachment: ${m.attachmentName} (${m.attachmentType})]" else ""
            fullPromptBuilder.append("$roleName: ${m.text}$attachStr\n")
        }

        val partObj = JSONObject().put("text", fullPromptBuilder.toString().trim())
        val contentObj = JSONObject().put("parts", JSONArray().put(partObj))
        contentsArray.put(contentObj)

        jsonBody.put("contents", contentsArray)

        // Declare Tools / Functions for Gemini Function Calling
        val toolsArray = JSONArray()
        val funcDeclarations = JSONArray()

        // 1. readFormulaData
        funcDeclarations.put(JSONObject().apply {
            put("name", "readFormulaData")
            put("description", "Reads user's saved formulas, rules, and divisor setup.")
        })

        // 2. readMarketData
        funcDeclarations.put(JSONObject().apply {
            put("name", "readMarketData")
            put("description", "Reads currently loaded market records and chart history.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("market", JSONObject().put("type", "STRING").put("description", "Optional market name"))
                })
            })
        })

        // 3. calculatePrediction
        funcDeclarations.put(JSONObject().apply {
            put("name", "calculatePrediction")
            put("description", "Applies formula to market data to compute OTC digits, Jodi, and Panel predictions.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("formula", JSONObject().put("type", "STRING").put("description", "Formula name e.g. OTC, NEW-1, SPECIAL 30"))
                    put("market", JSONObject().put("type", "STRING").put("description", "Market name e.g. KALYAN, SRIDEVI"))
                })
            })
        })

        // 4. readHistory
        funcDeclarations.put(JSONObject().apply {
            put("name", "readHistory")
            put("description", "Reads past backtest history evaluations, pass/fail days report and accuracy percentage.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("market", JSONObject().put("type", "STRING").put("description", "Market name"))
                })
            })
        })

        // 5. saveRecord
        funcDeclarations.put(JSONObject().apply {
            put("name", "saveRecord")
            put("description", "Saves a new market result record into history database. REQUIRES PERMISSION.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("market", JSONObject().put("type", "STRING").put("description", "Market name"))
                    put("date", JSONObject().put("type", "STRING").put("description", "Date formatted dd-MM-yyyy"))
                    put("openPanel", JSONObject().put("type", "STRING").put("description", "Open Panel digits e.g. 149"))
                    put("jodi", JSONObject().put("type", "STRING").put("description", "Jodi digits e.g. 45"))
                    put("closePanel", JSONObject().put("type", "STRING").put("description", "Close Panel digits e.g. 140"))
                })
                put("required", JSONArray().put("market").put("date").put("jodi"))
            })
        })

        toolsArray.put(JSONObject().put("function_declarations", funcDeclarations))
        jsonBody.put("tools", toolsArray)

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBodyStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            return "Error [${response.code}]: $responseBodyStr"
        }

        val jsonResp = JSONObject(responseBodyStr)
        val candidates = jsonResp.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val cand = candidates.getJSONObject(0)
            val content = cand.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                val firstPart = parts.getJSONObject(0)

                // Check if Gemini invoked a functionCall
                val funcCall = firstPart.optJSONObject("functionCall")
                if (funcCall != null) {
                    val fnName = funcCall.optString("name")
                    val fnArgs = funcCall.optJSONObject("args") ?: JSONObject()
                    val callJson = JSONObject().apply {
                        put("type", "FUNCTION_CALL")
                        put("functionName", fnName)
                        put("args", fnArgs)
                    }
                    return callJson.toString()
                }

                return firstPart.optString("text", "No text generated.")
            }
        }
        return "No response text received from Gemini API."
    }

    private fun callOpenAiCompatibleRest(
        apiKey: String,
        modelName: String,
        customEndpoint: String,
        provider: String,
        messages: List<ChatMessage>,
        systemPrompt: String
    ): String {
        val baseUrl = when {
            customEndpoint.isNotBlank() -> customEndpoint.trimEnd('/')
            provider.contains("GROK") -> "https://api.x.ai/v1"
            provider.contains("DEEPSEEK") -> "https://api.deepseek.com/v1"
            provider.contains("OPENCODE") -> "https://api.opencode.ai/v1"
            provider.contains("NEMOTRON") -> "https://integrate.api.nvidia.com/v1"
            else -> "https://api.openai.com/v1"
        }

        val url = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"

        val jsonBody = JSONObject()
        jsonBody.put("model", modelName.ifBlank { "deepseek-chat" })

        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().put("role", "system").put("content", systemPrompt))

        for (m in messages.takeLast(10)) {
            val role = if (m.sender == "user") "user" else "assistant"
            val attachStr = if (m.attachmentName != null) " [Attachment: ${m.attachmentName} (${m.attachmentType})]" else ""
            messagesArray.put(JSONObject().put("role", role).put("content", "${m.text}$attachStr"))
        }

        jsonBody.put("messages", messagesArray)

        val reqBuilder = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .header("Content-Type", "application/json")

        if (apiKey.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer $apiKey")
        }

        val response = client.newCall(reqBuilder.build()).execute()
        val responseBodyStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            return "Error [${response.code}]: $responseBodyStr"
        }

        val jsonResp = JSONObject(responseBodyStr)
        val choices = jsonResp.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val choice = choices.getJSONObject(0)
            val msg = choice.optJSONObject("message")
            return msg?.optString("content", "No message content") ?: "Empty response"
        }
        return "No choices returned from API."
    }

    private fun callClaudeRest(
        apiKey: String,
        modelName: String,
        customEndpoint: String,
        messages: List<ChatMessage>,
        systemPrompt: String
    ): String {
        val url = if (customEndpoint.isNotBlank()) customEndpoint else "https://api.anthropic.com/v1/messages"

        val jsonBody = JSONObject()
        jsonBody.put("model", modelName.ifBlank { "claude-3-5-sonnet-20241022" })
        jsonBody.put("max_tokens", 1024)
        jsonBody.put("system", systemPrompt)

        val messagesArray = JSONArray()
        for (m in messages.takeLast(10)) {
            val role = if (m.sender == "user") "user" else "assistant"
            val attachStr = if (m.attachmentName != null) " [Attachment: ${m.attachmentName} (${m.attachmentType})]" else ""
            messagesArray.put(JSONObject().put("role", role).put("content", "${m.text}$attachStr"))
        }
        jsonBody.put("messages", messagesArray)

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBodyStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            return "Error [${response.code}]: $responseBodyStr"
        }

        val jsonResp = JSONObject(responseBodyStr)
        val contentArray = jsonResp.optJSONArray("content")
        if (contentArray != null && contentArray.length() > 0) {
            val item = contentArray.getJSONObject(0)
            return item.optString("text", "No text in Claude response")
        }
        return "No content returned from Claude API."
    }
}
