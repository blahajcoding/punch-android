package com.punch.android.gateway

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GatewayUrlTest {
    @Test
    fun acceptsHttpAndHttps() {
        assertEquals(
            "http://192.168.1.10:4096",
            GatewayUrl.normalizeOrigin("http://192.168.1.10:4096").getOrThrow(),
        )
        assertEquals(
            "https://100.64.0.2:4096",
            GatewayUrl.normalizeOrigin("https://100.64.0.2:4096").getOrThrow(),
        )
    }

    @Test
    fun rejectsNonHttpSchemes() {
        val result = GatewayUrl.normalizeOrigin("ftp://example.com")
        assertTrue(result.isFailure)
    }

    @Test
    fun buildsPiEndpoints() {
        val origin = "http://192.168.1.10:4096"
        assertEquals("http://192.168.1.10:4096/health", GatewayUrl.healthUrl(origin))
        assertEquals("http://192.168.1.10:4096/session", GatewayUrl.sessionUrl(origin))
        assertEquals(
            "http://192.168.1.10:4096/session/abc/message",
            GatewayUrl.messageUrl(origin, "abc"),
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GatewayClientTest {
    @Test
    fun extractsAssistantTextFromParts() {
        val body = """
            {
              "info": {"role": "assistant"},
              "parts": [{"type": "text", "text": "hello from pi"}]
            }
        """.trimIndent()
        assertEquals("hello from pi", GatewayClient.extractAssistantText(body))
    }

    @Test
    fun promptCreatesSessionThenPostsMessage() {
        val calls = mutableListOf<String>()
        val transport = GatewayTransport { method, url, headers, body ->
            calls += "$method $url"
            when {
                url.endsWith("/session") && method == "POST" ->
                    GatewayHttpResponse(200, """{"id":"sess-1"}""")
                url.endsWith("/session/sess-1/message") -> {
                    assertTrue(headers["Authorization"]!!.startsWith("Basic "))
                    assertTrue(body!!.contains("ping"))
                    GatewayHttpResponse(
                        200,
                        """{"info":{"role":"assistant"},"parts":[{"type":"text","text":"pong"}]}""",
                    )
                }
                else -> GatewayHttpResponse(404, """{"error":"not found"}""")
            }
        }
        val client = GatewayClient(transport)
        val result = client.prompt(
            origin = "http://10.0.0.2:4096",
            username = "opencode",
            password = "secret",
            text = "ping",
        )
        assertEquals("pong", result.text)
        assertEquals("sess-1", result.sessionId)
        assertEquals(
            listOf(
                "POST http://10.0.0.2:4096/session",
                "POST http://10.0.0.2:4096/session/sess-1/message",
            ),
            calls,
        )
    }

    @Test
    fun healthUsesHealthEndpoint() {
        var capturedUrl = ""
        val transport = GatewayTransport { method, url, _, _ ->
            assertEquals("GET", method)
            capturedUrl = url
            GatewayHttpResponse(200, """{"ok":true,"tools":["bash"]}""")
        }
        val health = GatewayClient(transport).health("https://100.64.1.1:4096", "u", "p")
        assertTrue(health.ok)
        assertEquals("https://100.64.1.1:4096/health", capturedUrl)
    }

    @Test
    fun basicAuthEncodesUserAndPassword() {
        val header = GatewayClient.authHeaders("opencode", "devpassword")["Authorization"]!!
        val encoded = header.removePrefix("Basic ")
        val decoded = String(Base64.decode(encoded, Base64.NO_WRAP))
        assertEquals("opencode:devpassword", decoded)
    }
}
