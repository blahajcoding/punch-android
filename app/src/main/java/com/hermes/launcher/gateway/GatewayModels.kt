package com.hermes.launcher.gateway

data class ChatMessage(
    val role: String,
    val content: String,
)

data class GatewayChatResult(
    val text: String,
    val sessionId: String? = null,
)

data class GatewayHealth(
    val ok: Boolean,
    val detail: String = "",
)

class GatewayException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

data class GatewayHttpResponse(
    val statusCode: Int,
    val body: String,
)

fun interface GatewayTransport {
    fun exchange(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: String?,
    ): GatewayHttpResponse
}
