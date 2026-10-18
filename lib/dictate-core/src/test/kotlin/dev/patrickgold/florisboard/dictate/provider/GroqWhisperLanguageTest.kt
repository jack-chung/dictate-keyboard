/*
 * Copyright (C) 2026 The Dictate Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.provider

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile

class GroqWhisperLanguageTest : FunSpec({
    val languages = mapOf(
        "yue-HK" to "yue",
        "yue-CN" to "yue",
        "yue-hk" to "yue",
        "YUE-cn" to "yue",
        "YuE-Hk" to "yue",
        "yue" to "yue",
        "en" to "en",
        "en-US" to "en-US",
        "unknown-code" to "unknown-code",
        "yue-TW" to "yue-TW",
    )

    languages.forEach { (input, expected) ->
        test("Groq Whisper normalization preserves or maps $input") {
            OpenAiCompatibleClient.groqWhisperLanguage(input) shouldBe expected
        }
    }

    // An HTTP proxy keeps the real endpoint hostname in the public transcribe path while capturing
    // every request locally. No DNS resolution, TLS connection or API call to a provider is needed.
    suspend fun requestBody(baseUrl: String, model: String, language: String?): String {
        val audio = createTempFile(suffix = ".wav").toFile().apply {
            writeBytes("RIFF-test-audio".encodeToByteArray())
        }
        try {
            return MockWebServer().use { server ->
                server.start()
                server.enqueue(MockResponse().setBody("""{"text":"test transcript"}"""))
                val client = OpenAiCompatibleClient(
                    ProviderConfig(
                        baseUrl = baseUrl,
                        apiKey = "test",
                        proxy = ProxyConfig(Proxy.Type.HTTP, "127.0.0.1", server.port),
                    ),
                )
                client.transcribe(TranscriptionRequest(audio, model, language = language))
                    .text shouldBe "test transcript"
                val recorded = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
                recorded.method shouldBe "POST"
                recorded.path shouldBe baseUrl + "audio/transcriptions"
                recorded.getHeader("Content-Type").orEmpty() shouldContain "multipart/form-data"
                recorded.body.readUtf8()
            }
        } finally {
            audio.delete()
        }
    }

    listOf("whisper-large-v3", "whisper-large-v3-turbo").forEach { model ->
        languages.forEach { (input, expected) ->
            test("$model sends the expected Groq multipart language for $input") {
                val body = requestBody("http://api.groq.com/openai/v1/", model, input)
                body shouldContain "name=\"language\"\r\n\r\n$expected\r\n"
                body shouldNotContain "name=\"languages[]\""
            }
        }
        listOf(null, "", "detect").forEach { language ->
            test("$model keeps automatic detection for $language") {
                val body = requestBody("http://api.groq.com/openai/v1/", model, language)
                body shouldNotContain "name=\"language\""
                body shouldNotContain "name=\"languages[]\""
            }
        }
    }

    listOf(
        "http://api.openai.com/v1/" to "whisper-large-v3-turbo",
        "http://api.siliconflow.cn/v1/" to "FunAudioLLM/SenseVoiceSmall",
        "http://api.groq.com.example.test/openai/v1/" to "whisper-large-v3-turbo",
        "http://api.groq.com/openai/v1/" to "other-model",
    ).forEach { (baseUrl, model) ->
        test("$baseUrl $model retains the original Cantonese tag") {
            requestBody(baseUrl, model, "yue-HK") shouldContain
                "name=\"language\"\r\n\r\nyue-HK\r\n"
        }
    }
})
