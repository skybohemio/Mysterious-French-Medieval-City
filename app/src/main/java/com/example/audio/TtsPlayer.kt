package com.example.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class TtsPlayer(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentText = MutableStateFlow("")
    val currentText: StateFlow<String> = _currentText

    private val _progress = MutableStateFlow(0f) // 0.0 to 1.0
    val progress: StateFlow<Float> = _progress

    private val _currentWordRange = MutableStateFlow(Pair(0, 0)) // start, end indices of active word
    val currentWordRange: StateFlow<Pair<Int, Int>> = _currentWordRange

    private val _isFemaleVoice = MutableStateFlow(true)
    val isFemaleVoice: StateFlow<Boolean> = _isFemaleVoice

    // Default to 100% Offline Audio as requested: Zero data usage, instant local narration
    private val _isOfflineAudioPreferred = MutableStateFlow(true)
    val isOfflineAudioPreferred: StateFlow<Boolean> = _isOfflineAudioPreferred

    fun setOfflineAudioPreferred(enabled: Boolean) {
        _isOfflineAudioPreferred.value = enabled
        Log.i("TtsPlayer", "Offline audio mode set to: $enabled")
    }

    private var speechRate = 1.0f
    private var pitch = 1.0f
    private var activeUtteranceId = ""
    private var activeLanguageCode = "FR"
    private var pendingSpeechText: String? = null

    // OkHttpClient with optimized connection for streaming TTS
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    // MediaPlayer for playing synthesized audio streams
    private var mediaPlayer: MediaPlayer? = null

    // Coroutine scope for network synthesis and progress tracking
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var synthesisJob: Job? = null
    private var trackingJob: Job? = null

    init {
        try {
            // Initialize local TTS as hardware fallback
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TtsPlayer", "Failed to construct local TextToSpeech fallback", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = getLocaleForCode(activeLanguageCode)
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.FRENCH)
            }
            updateLocalVoice(locale)
            setupProgressListener()
            isTtsInitialized = true
            tts?.setSpeechRate(speechRate)
            val basePitch = if (_isFemaleVoice.value) 1.12f else 0.88f
            tts?.setPitch(basePitch * pitch)
            Log.i("TtsPlayer", "Offline TextToSpeech engine successfully initialized.")

            pendingSpeechText?.let { pending ->
                pendingSpeechText = null
                fallbackToLocalAndroidTts(pending)
            }
        } else {
            Log.w("TtsPlayer", "Initialization of local TextToSpeech returned status $status")
        }
    }

    private fun getLocaleForCode(langCode: String): Locale {
        return when (langCode.uppercase()) {
            "FR" -> Locale.FRENCH
            "EN" -> Locale.ENGLISH
            "DE" -> Locale.GERMAN
            "ES" -> Locale("es", "ES")
            "NL" -> Locale("nl", "NL")
            "IT" -> Locale.ITALIAN
            else -> Locale.FRENCH
        }
    }

    fun setLanguageByCode(langCode: String) {
        activeLanguageCode = langCode
        if (!isTtsInitialized || tts == null) return
        val locale = getLocaleForCode(langCode)
        try {
            val result = tts?.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                updateLocalVoice(locale)
            }
        } catch (e: Exception) {
            Log.w("TtsPlayer", "Could not set language on local fallback TTS", e)
        }
    }

    private fun updateLocalVoice(locale: Locale) {
        try {
            val isFemale = _isFemaleVoice.value
            val basePitch = if (isFemale) 1.12f else 0.88f
            tts?.setPitch(basePitch * pitch)

            val voices = tts?.voices ?: return
            val matching = voices.filter { it.locale.language.equals(locale.language, ignoreCase = true) }
            if (matching.isNotEmpty()) {
                val preferredVoice = matching.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    if (isFemale) {
                        name.contains("female") || name.contains("-fem-") || name.contains("fra") ||
                        name.contains("sfg") || name.contains("eed") || name.contains("deg") ||
                        name.contains("itb") || name.contains("nla") || name.contains("woman") ||
                        name.contains("carla") || name.contains("elvira") || name.contains("katja")
                    } else {
                        name.contains("male") || name.contains("-mal-") || name.contains("frb") ||
                        name.contains("iol") || name.contains("eef") || name.contains("deb") ||
                        name.contains("ita") || name.contains("nlb") || name.contains("man") ||
                        name.contains("giorgio") || name.contains("alvaro") || name.contains("kilian")
                    }
                } ?: if (isFemale) matching.firstOrNull() else matching.lastOrNull() ?: matching.first()
                if (preferredVoice != null) {
                    tts?.voice = preferredVoice
                }
            }
        } catch (e: Exception) {
            Log.w("TtsPlayer", "Could not customize local voice", e)
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isPlaying.value = true
                _progress.value = 0f
            }

            override fun onDone(utteranceId: String?) {
                _isPlaying.value = false
                _progress.value = 1f
                _currentWordRange.value = Pair(0, 0)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isPlaying.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isPlaying.value = false
                Log.w("TtsPlayer", "Local TTS error code: $errorCode")
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                val textLength = _currentText.value.length
                if (textLength > 0) {
                    _progress.value = (start.toFloat() / textLength.toFloat()).coerceIn(0f, 1f)
                }
                _currentWordRange.value = Pair(start, end)
            }
        })
    }

    fun speak(text: String, siteId: Int? = null, langCode: String? = null) {
        stop()
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        if (!langCode.isNullOrBlank()) {
            activeLanguageCode = langCode.uppercase()
        }

        _currentText.value = cleanText
        activeUtteranceId = "utterance_" + System.currentTimeMillis()

        // 1. Check if a pre-bundled studio audio file exists in assets for this landmark site & language!
        if (siteId != null) {
            val langSub = activeLanguageCode.lowercase()
            val candidateAsset = "audio/site_${siteId}_${langSub}.mp3"
            try {
                val afd = context.assets.openFd(candidateAsset)
                Log.i("TtsPlayer", "Playing bundled offline studio audio: $candidateAsset")
                playAssetAudioFile(afd, cleanText)
                return
            } catch (_: Exception) {
                // Not pre-bundled as standalone mp3, proceed with offline Android TTS
            }
        }

        if (_isOfflineAudioPreferred.value) {
            // OFFLINE AUDIO BY DEFAULT: Instant, reliable local Android TTS with zero internet latency
            Log.i("TtsPlayer", "Playing audio in offline mode using local Android TTS (Language: $activeLanguageCode)")
            fallbackToLocalAndroidTts(cleanText)
        } else {
            // Optional Online HD Cloud TTS
            synthesisJob = coroutineScope.launch {
                val success = synthesizeAndPlayCloudTts(cleanText, activeLanguageCode)
                if (!success) {
                    Log.i("TtsPlayer", "Cloud TTS unavailable, falling back to local Android TextToSpeech engine")
                    withContext(Dispatchers.Main) {
                        fallbackToLocalAndroidTts(cleanText)
                    }
                }
            }
        }
    }

    private fun playAssetAudioFile(afd: AssetFileDescriptor, text: String) {
        stopAudioPlaybackOnly()
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val params = player.playbackParams
                    params.speed = speechRate.coerceIn(0.5f, 2.0f)
                    params.pitch = pitch.coerceIn(0.5f, 2.0f)
                    player.playbackParams = params
                } catch (e: Exception) {
                    Log.w("TtsPlayer", "PlaybackParams adjustment note: ${e.message}")
                }
            }

            player.setOnCompletionListener {
                _isPlaying.value = false
                _progress.value = 1f
                _currentWordRange.value = Pair(0, 0)
                stopProgressTracking()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.w("TtsPlayer", "Asset MediaPlayer error: what=$what extra=$extra")
                _isPlaying.value = false
                stopProgressTracking()
                fallbackToLocalAndroidTts(text)
                true
            }

            mediaPlayer = player
            player.start()
            _isPlaying.value = true

            val durationMs = player.duration.coerceAtLeast(1000)
            startAccurateProgressTracking(text, durationMs)
        } catch (e: Exception) {
            Log.e("TtsPlayer", "Failed to start Asset MediaPlayer", e)
            fallbackToLocalAndroidTts(text)
        }
    }

    private suspend fun synthesizeAndPlayCloudTts(text: String, langCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val chunks = splitTextForTts(text, 170)
                if (chunks.isEmpty()) return@withContext false

                val langTag = when (langCode.uppercase()) {
                    "FR" -> "fr"
                    "EN" -> "en"
                    "DE" -> "de"
                    "ES" -> "es"
                    "NL" -> "nl"
                    "IT" -> "it"
                    else -> "fr"
                }

                val audioBuffer = ByteArrayOutputStream()
                for (chunk in chunks) {
                    val encoded = URLEncoder.encode(chunk, "UTF-8")
                    val url = "https://translate.google.com/translate_tts?ie=UTF-8&q=$encoded&tl=$langTag&client=tw-ob"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Referer", "https://translate.google.com/")
                        .build()

                    val response = okHttpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        response.close()
                        return@withContext false
                    }
                    val bodyBytes = response.body?.bytes()
                    response.close()
                    if (bodyBytes == null || bodyBytes.isEmpty()) {
                        return@withContext false
                    }
                    audioBuffer.write(bodyBytes)
                }

                val allBytes = audioBuffer.toByteArray()
                if (allBytes.isEmpty()) return@withContext false

                val cacheFile = File(context.cacheDir, "tts_narration_audio.mp3")
                cacheFile.writeBytes(allBytes)

                withContext(Dispatchers.Main) {
                    playAudioFile(cacheFile, text)
                }
                true
            } catch (e: Exception) {
                Log.w("TtsPlayer", "Cloud TTS synthesis exception: ${e.message}")
                false
            }
        }
    }

    private fun playAudioFile(file: File, text: String) {
        stopAudioPlaybackOnly()
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val params = player.playbackParams
                    params.speed = speechRate.coerceIn(0.5f, 2.0f)
                    params.pitch = pitch.coerceIn(0.5f, 2.0f)
                    player.playbackParams = params
                } catch (e: Exception) {
                    Log.w("TtsPlayer", "PlaybackParams adjustment note: ${e.message}")
                }
            }

            player.setOnCompletionListener {
                _isPlaying.value = false
                _progress.value = 1f
                _currentWordRange.value = Pair(0, 0)
                stopProgressTracking()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.w("TtsPlayer", "MediaPlayer playback error: what=$what extra=$extra")
                _isPlaying.value = false
                stopProgressTracking()
                fallbackToLocalAndroidTts(text)
                true
            }

            mediaPlayer = player
            player.start()
            _isPlaying.value = true

            val durationMs = player.duration.coerceAtLeast(1000)
            startAccurateProgressTracking(text, durationMs)
        } catch (e: Exception) {
            Log.e("TtsPlayer", "Failed to start MediaPlayer", e)
            fallbackToLocalAndroidTts(text)
        }
    }

    private fun fallbackToLocalAndroidTts(text: String) {
        val wordsCount = text.split("\\s+".toRegex()).size
        val estDurationMs = (wordsCount * 260 / speechRate).toInt().coerceAtLeast(1500)
        startAccurateProgressTracking(text, estDurationMs)

        if (isTtsInitialized && tts != null) {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, activeUtteranceId)
            }
            _isPlaying.value = true
            val locale = getLocaleForCode(activeLanguageCode)
            tts?.setLanguage(locale)
            tts?.setSpeechRate(speechRate)
            updateLocalVoice(locale)

            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, activeUtteranceId)
            if (result == TextToSpeech.ERROR) {
                Log.w("TtsPlayer", "Local TTS speak error, using visual reading simulator")
            }
        } else {
            Log.i("TtsPlayer", "Local TTS initializing, queued pending speech")
            pendingSpeechText = text
            _isPlaying.value = true
        }
    }

    private fun startVirtualOnlySimulation(text: String) {
        stopProgressTracking()
        _currentText.value = text
        _isPlaying.value = true
        _progress.value = 0f
        _currentWordRange.value = Pair(0, 0)

        val wordsCount = text.split("\\s+".toRegex()).size
        val estDurationMs = (wordsCount * 260).coerceAtLeast(1200)
        startAccurateProgressTracking(text, estDurationMs)
    }

    private fun startAccurateProgressTracking(text: String, durationMs: Int) {
        stopProgressTracking()
        _progress.value = 0f
        _currentWordRange.value = Pair(0, 0)

        val wordsWithOffsets = mutableListOf<Triple<String, Int, Int>>()
        val pattern = Pattern.compile("[a-zA-Z0-9àâäéèêëîïôöùûüçœÆæÀÂÄÉÈÊËÎÏÔÖÙÛÜÇŒ']+")
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            wordsWithOffsets.add(Triple(matcher.group(), matcher.start(), matcher.end()))
        }

        trackingJob = coroutineScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive && _isPlaying.value) {
                val mp = mediaPlayer
                val frac = if (mp != null && mp.isPlaying && mp.duration > 0) {
                    (mp.currentPosition.toFloat() / mp.duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    val elapsed = System.currentTimeMillis() - startTime
                    (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                }

                _progress.value = frac

                if (wordsWithOffsets.isNotEmpty()) {
                    val wordIndex = ((frac * wordsWithOffsets.size).toInt()).coerceIn(0, wordsWithOffsets.size - 1)
                    val currentWord = wordsWithOffsets[wordIndex]
                    _currentWordRange.value = Pair(currentWord.second, currentWord.third)
                }

                if (frac >= 0.999f) {
                    _isPlaying.value = false
                    _progress.value = 1f
                    _currentWordRange.value = Pair(0, 0)
                    break
                }
                delay(50)
            }
        }
    }

    private fun splitTextForTts(text: String, maxLen: Int = 170): List<String> {
        if (text.length <= maxLen) return listOf(text)

        val parts = text.split(Regex("(?<=[.!?;\n:])\\s+"))
        val chunks = mutableListOf<String>()
        var curr = ""

        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.isEmpty()) continue

            if (curr.isEmpty()) {
                if (trimmed.length <= maxLen) {
                    curr = trimmed
                } else {
                    // Split further by commas or spaces
                    val words = trimmed.split(" ")
                    var sub = ""
                    for (word in words) {
                        if (sub.isEmpty()) {
                            sub = word
                        } else if (sub.length + 1 + word.length <= maxLen) {
                            sub += " $word"
                        } else {
                            chunks.add(sub)
                            sub = word
                        }
                    }
                    if (sub.isNotEmpty()) {
                        curr = sub
                    }
                }
            } else if (curr.length + 1 + trimmed.length <= maxLen) {
                curr += " $trimmed"
            } else {
                chunks.add(curr)
                if (trimmed.length <= maxLen) {
                    curr = trimmed
                } else {
                    val words = trimmed.split(" ")
                    var sub = ""
                    for (word in words) {
                        if (sub.isEmpty()) {
                            sub = word
                        } else if (sub.length + 1 + word.length <= maxLen) {
                            sub += " $word"
                        } else {
                            chunks.add(sub)
                            sub = word
                        }
                    }
                    curr = sub
                }
            }
        }
        if (curr.isNotEmpty()) {
            chunks.add(curr)
        }
        return chunks
    }

    private fun stopProgressTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    private fun stopAudioPlaybackOnly() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w("TtsPlayer", "Error cleaning MediaPlayer: ${e.message}")
        }
    }

    fun stop() {
        synthesisJob?.cancel()
        synthesisJob = null
        stopProgressTracking()
        stopAudioPlaybackOnly()

        try {
            if (isTtsInitialized && tts != null) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.w("TtsPlayer", "Error stopping local fallback TTS: ${e.message}")
        }

        _isPlaying.value = false
        _progress.value = 0f
        _currentWordRange.value = Pair(0, 0)
    }

    fun setSpeed(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        val params = it.playbackParams
                        params.speed = speechRate
                        it.playbackParams = params
                    }
                }
            }
            if (isTtsInitialized && tts != null) {
                tts?.setSpeechRate(speechRate)
            }
        } catch (e: Exception) {
            Log.w("TtsPlayer", "Error setting speech rate: ${e.message}")
        }
    }

    fun setPitchVal(pitchVal: Float) {
        pitch = pitchVal.coerceIn(0.5f, 2.0f)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        val params = it.playbackParams
                        params.pitch = pitch
                        it.playbackParams = params
                    }
                }
            }
            if (isTtsInitialized && tts != null) {
                tts?.setPitch(pitch)
            }
        } catch (e: Exception) {
            Log.w("TtsPlayer", "Error setting pitch: ${e.message}")
        }
    }

    fun release() {
        stop()
        try {
            if (tts != null) {
                tts?.stop()
                tts?.shutdown()
            }
        } catch (e: Exception) {
            Log.w("TtsPlayer", "Error releasing local fallback TTS: ${e.message}")
        }
        tts = null
        isTtsInitialized = false
    }

    fun setVoiceGender(isFemale: Boolean) {
        _isFemaleVoice.value = isFemale
        if (isTtsInitialized) {
            updateLocalVoice(getLocaleForCode(activeLanguageCode))
        }
    }

    private var volume = 1.0f

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            Log.w("TtsPlayer", "Error setting volume: ${e.message}")
        }
    }

    fun getVolume(): Float = volume

    fun getVoiceName(langCode: String): String {
        val isFemale = _isFemaleVoice.value
        val prefix = if (_isOfflineAudioPreferred.value) "⚡ Hors-Ligne • " else "☁️ HD • "
        val voiceName = when (langCode.uppercase()) {
            "FR" -> if (isFemale) "Voix Française (Naturelle)" else "Voix Française (Studio Homme)"
            "EN" -> if (isFemale) "English Voice (Natural Female)" else "English Voice (Male)"
            "DE" -> if (isFemale) "Deutsche Stimme (Katja)" else "Deutsche Stimme (Kilian)"
            "ES" -> if (isFemale) "Voz Española (Elvira)" else "Voz Española (Álvaro)"
            "NL" -> if (isFemale) "Nederlandse Stem (Colette)" else "Nederlandse Stem (Maarten)"
            "IT" -> if (isFemale) "Voce Italiana (Carla)" else "Voce Italiana (Giorgio)"
            else -> if (isFemale) "Voix Locale (Femme)" else "Voix Locale (Homme)"
        }
        return prefix + voiceName
    }
}
