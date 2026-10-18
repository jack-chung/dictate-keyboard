/*
 * Copyright (C) 2026 The Dictate Contributors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may obtain a copy at https://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.sync

import dev.patrickgold.florisboard.dictate.provider.TranscriptionApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Wire fixtures shared by both app identities; these tokens are synthetic test data. */
class DictateWearSettingsCompatibilityTest {
    @Test
    fun `existing phone snapshot preserves standalone and dictation configuration`() {
        val fixture = """{
            "transcriptionProviderId":"groq-account", "providerLabel":"Groq",
            "transcriptionApi":"OPENAI_MULTIPART", "baseUrl":"https://example.invalid/v1/",
            "model":"whisper-large-v3-turbo", "apiKey":"synthetic-stt-token", "keySyncEnabled":true,
            "language":"yue-HK", "languageName":"Cantonese", "stylePrompt":"fixture vocabulary",
            "accentColorArgb":123, "autoRewordingEnabled":true, "rewordingEnabled":true,
            "autoFormattingEnabled":true, "chatModel":"fixture-chat",
            "rewordingBaseUrl":"https://example.invalid/chat/", "rewordingApiKey":"synthetic-chat-token",
            "rewordingApi":"GEMINI_GENERATE_CONTENT", "systemPrompt":"fixture system",
            "autoApplyPrompts":[{"instruction":"fixture prompt", "requiresSelection":true}],
            "hapticFeedback":true, "requestTimeoutSeconds":45, "tetherProtocol":1
        }"""
        val settings = requireNotNull(DictateSyncedSettings.decode(fixture))
        assertEquals("groq-account", settings.transcriptionProviderId)
        assertEquals("whisper-large-v3-turbo", settings.model)
        assertEquals(TranscriptionApi.OPENAI_MULTIPART, settings.transcriptionApi)
        assertEquals("yue-HK", settings.language)
        assertEquals("Cantonese", settings.languageName)
        assertEquals("fixture vocabulary", settings.stylePrompt)
        assertEquals(123, settings.accentColorArgb)
        assertEquals("synthetic-stt-token", settings.apiKey)
        assertEquals("synthetic-chat-token", settings.rewordingApiKey)
        assertEquals("fixture-chat", settings.chatModel)
        assertEquals(TranscriptionApi.GEMINI_GENERATE_CONTENT, settings.rewordingApi)
        assertEquals("fixture system", settings.systemPrompt)
        assertEquals(listOf(SyncedPrompt("fixture prompt", true)), settings.autoApplyPrompts)
        assertTrue(settings.keySyncEnabled)
        assertTrue(settings.autoRewordingEnabled)
        assertTrue(settings.rewordingEnabled)
        assertTrue(settings.autoFormattingEnabled)
        assertTrue(settings.canStandalone)
        assertTrue(settings.canRewordStandalone)
        assertTrue(settings.hapticFeedback)
        assertEquals(45, settings.requestTimeoutSeconds)
        assertEquals(1, settings.tetherProtocol)
        assertEquals(settings, DictateSyncedSettings.decode(settings.encode()))
    }

    @Test
    fun `key-free snapshot remains tether-only without losing provider configuration`() {
        val fixture = """{"transcriptionProviderId":"groq-account","model":"whisper-large-v3-turbo",
            "baseUrl":"https://example.invalid/v1/","apiKey":"","rewordingApiKey":"",
            "keySyncEnabled":false,"language":null}"""
        val settings = requireNotNull(DictateSyncedSettings.decode(fixture))
        val decoded = requireNotNull(DictateSyncedSettings.decode(settings.encode()))
        assertFalse(decoded.keySyncEnabled)
        assertFalse(decoded.canStandalone)
        assertFalse(decoded.canRewordStandalone)
        assertEquals("whisper-large-v3-turbo", decoded.model)
        assertEquals("", decoded.apiKey)
        assertEquals("", decoded.rewordingApiKey)
        assertNull(decoded.language)
    }

    @Test
    fun `unknown future settings do not break the current watch schema`() {
        // Compatibility only: these fields are ignored, not implemented or synchronized by this task.
        val fixture = """{"model":"fixture-model","language":"en",
            "futureThinkingLevel":"high","futureLanguageProfiles":[{"language":"yue"}],
            "futureFallbackProfile":{"id":"unused"}}"""
        val settings = requireNotNull(DictateSyncedSettings.decode(fixture))
        assertEquals("fixture-model", settings.model)
        assertEquals("en", settings.language)
        assertEquals(0, settings.tetherProtocol)
        assertEquals(0, settings.requestTimeoutSeconds)
        assertFalse(settings.encode().contains("futureThinkingLevel"))
        assertNull(DictateSyncedSettings.decode("not a settings document"))
    }

    @Test
    fun `tether responses preserve Cantonese text and existing error envelopes`() {
        val text = "廣東話測試"
        assertEquals(text, DictateWearProtocol.parseTranscribeResponse(text.toByteArray()).text)
        for (status in listOf(DictateWearProtocol.RESP_OK, DictateWearProtocol.RESP_NO_SPEECH,
            DictateWearProtocol.RESP_BAD_KEY, DictateWearProtocol.RESP_OFFLINE,
            DictateWearProtocol.RESP_QUOTA, DictateWearProtocol.RESP_ERROR)) {
            assertEquals(DictateWearProtocol.TranscribeResponse(status, text),
                DictateWearProtocol.parseTranscribeResponse(DictateWearProtocol.encodeTranscribeResponse(status, text)))
        }
    }
}
