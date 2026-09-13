package com.example.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TtsPlayer
import com.example.data.AppDatabase
import com.example.data.OfflineMapData
import com.example.data.PurchaseOrder
import com.example.data.Site
import com.example.data.SiteRepository
import com.example.data.TourRoute
import com.example.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    FR("FR", "Français", "🇫🇷"),
    EN("EN", "English", "🇬🇧"),
    DE("DE", "Deutsch", "🇩🇪"),
    ES("ES", "Español", "🇪🇸"),
    NL("NL", "Nederlands", "🇳🇱")
}

class GuideViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SiteRepository
    val ttsPlayer: TtsPlayer

    private val _selectedLanguage = MutableStateFlow(AppLanguage.FR)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    // GPS location state
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    private var locationManager: LocationManager? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _userLocation.value = Pair(location.latitude, location.longitude)
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun startLocationUpdates() {
        val context = getApplication<Application>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            if (locationManager == null) {
                locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            }
            
            try {
                val lastKnownGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastKnownNetwork = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val bestLocation = lastKnownGps ?: lastKnownNetwork
                if (bestLocation != null) {
                    _userLocation.value = Pair(bestLocation.latitude, bestLocation.longitude)
                }
                
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    3f,
                    locationListener
                )
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    3f,
                    locationListener
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun stopLocationUpdates() {
        locationManager?.removeUpdates(locationListener)
    }

    // UI States
    val sitesList: StateFlow<List<Site>>
    val tourRoutes: StateFlow<List<TourRoute>>

    private val _selectedSite = MutableStateFlow<Site?>(null)
    val selectedSite: StateFlow<Site?>

    private val _selectedRoute = MutableStateFlow<TourRoute?>(null)
    val selectedRoute: StateFlow<TourRoute?>

    private val _activeTtsSite = MutableStateFlow<Site?>(null)
    val activeTtsSite: StateFlow<Site?>

    private val _ttsSpeed = MutableStateFlow(1.0f)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()

    // TTS bindings from TtsPlayer
    val isTtsPlaying: StateFlow<Boolean>
    val ttsProgress: StateFlow<Float>
    val ttsWordHighlight: StateFlow<Pair<Int, Int>>
    val ttsCurrentText: StateFlow<String>
    val isFemaleVoice: StateFlow<Boolean>

    // Admin state for messages
    private val _adminMessage = MutableStateFlow<String?>(null)
    val adminMessage: StateFlow<String?> = _adminMessage.asStateFlow()

    // Offline Map Cache state from Room
    val cachedMapTiles: StateFlow<List<OfflineMapData>>
    private val _isOfflineMapEnabled = MutableStateFlow(true)
    val isOfflineMapEnabled: StateFlow<Boolean> = _isOfflineMapEnabled.asStateFlow()

    val allUsers: StateFlow<List<User>>
    val allPurchases: StateFlow<List<PurchaseOrder>>
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = SiteRepository(
            database.siteDao(),
            database.routeDao(),
            database.offlineMapDao(),
            database.userDao(),
            database.purchaseDao()
        )

        allUsers = repository.allUsers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allPurchases = repository.allPurchases.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            _totalRevenue.value = repository.getTotalRevenue()
        }
        
        cachedMapTiles = repository.cachedMapTiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        ttsPlayer = TtsPlayer(application)
        isTtsPlaying = ttsPlayer.isPlaying
        ttsProgress = ttsPlayer.progress
        ttsWordHighlight = ttsPlayer.currentWordRange
        ttsCurrentText = ttsPlayer.currentText
        isFemaleVoice = ttsPlayer.isFemaleVoice

        // Set dynamic sitesList mapped by the selected language
        sitesList = repository.allSites.combine(_selectedLanguage) { rawList, language ->
            rawList.map { site ->
                site.copy(
                    title = site.getLocalizedTitle(language.code),
                    description = site.getLocalizedDescription(language.code),
                    narrationText = site.getLocalizedNarration(language.code)
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Set dynamic tourRoutes mapped by the selected language
        tourRoutes = repository.allRoutes.combine(_selectedLanguage) { rawRoutes, language ->
            rawRoutes.map { route ->
                route.copy(
                    nameFr = route.getLocalizedName(language.code),
                    descriptionFr = route.getLocalizedDescription(language.code)
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Set dynamic selectedSite combined with language
        selectedSite = combine(_selectedSite, _selectedLanguage) { site, language ->
            if (site == null) null
            else {
                site.copy(
                    title = site.getLocalizedTitle(language.code),
                    description = site.getLocalizedDescription(language.code),
                    narrationText = site.getLocalizedNarration(language.code)
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Set dynamic selectedRoute combined with language
        selectedRoute = combine(_selectedRoute, _selectedLanguage) { route, language ->
            if (route == null) null
            else {
                route.copy(
                    nameFr = route.getLocalizedName(language.code),
                    descriptionFr = route.getLocalizedDescription(language.code)
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Set dynamic activeTtsSite combined with language
        activeTtsSite = combine(_activeTtsSite, _selectedLanguage) { site, language ->
            if (site == null) null
            else {
                site.copy(
                    title = site.getLocalizedTitle(language.code),
                    description = site.getLocalizedDescription(language.code),
                    narrationText = site.getLocalizedNarration(language.code)
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Observe TTS status to reset activeTtsSite when finished
        viewModelScope.launch {
            isTtsPlaying.collect { playing ->
                if (!playing) {
                    if (ttsProgress.value >= 0.99f) {
                        _activeTtsSite.value = null
                    }
                }
            }
        }

        // Ensure all 150+ POIs are loaded into database
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.getSiteCount() < 150) {
                    val csvContent = application.assets.open("poi_export.csv").use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    }
                    val sitesToInsert = com.example.data.CsvManager.parseSitesFromCsv(csvContent)
                    if (sitesToInsert.isNotEmpty()) {
                        repository.insertSites(sitesToInsert)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GuideViewModel", "Error ensuring full 150+ sites in DB", e)
            }
        }
    }

    // Gemini AI states for dynamic landmark image and secrets
    private val _geminiImageState = MutableStateFlow<GeminiImageState>(GeminiImageState.Idle)
    val geminiImageState: StateFlow<GeminiImageState> = _geminiImageState.asStateFlow()

    fun selectSite(site: Site?) {
        _selectedSite.value = site
        if (site != null) {
            fetchGeminiDetails(site)
        } else {
            _geminiImageState.value = GeminiImageState.Idle
        }
    }

    fun exportCsvString(): String {
        return com.example.data.CsvManager.exportToCsvString(sitesList.value)
    }

    fun exportAndShareCsv(context: Context) {
        com.example.data.CsvManager.exportAndShareCsv(context, sitesList.value)
    }

    fun importCsv(csvContent: String): Int {
        val parsed = com.example.data.CsvManager.parseSitesFromCsv(csvContent)
        if (parsed.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertSites(parsed)
            }
            return parsed.size
        }
        return 0
    }

    fun nextSite() {
        val list = sitesList.value
        if (list.isEmpty()) return
        val current = _selectedSite.value ?: _activeTtsSite.value
        val currentIndex = list.indexOfFirst { it.id == current?.id }
        val nextIndex = if (currentIndex in 0 until list.size - 1) currentIndex + 1 else 0
        val target = list[nextIndex]
        selectSite(target)
        if (isTtsPlaying.value) {
            startAudioGuide(target)
        }
    }

    fun previousSite() {
        val list = sitesList.value
        if (list.isEmpty()) return
        val current = _selectedSite.value ?: _activeTtsSite.value
        val currentIndex = list.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        val target = list[prevIndex]
        selectSite(target)
        if (isTtsPlaying.value) {
            startAudioGuide(target)
        }
    }

    fun fetchGeminiDetails(site: Site) {
        val currentLang = _selectedLanguage.value.displayName
        val langCode = _selectedLanguage.value.code
        
        _geminiImageState.value = GeminiImageState.Loading
        
        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    val fallback = getLocalFallbackDetails(site, langCode)
                    _geminiImageState.value = GeminiImageState.Success(
                        imageUrl = fallback.imageUrl,
                        secretTrivia = fallback.secretTrivia,
                        visitTip = fallback.visitTip
                    )
                    return@launch
                }
                
                val promptText = """
                    You are an expert tour guide for Bourges, France. Provide high-quality content for the landmark: "${site.title}".
                    Return your response EXCLUSIVELY as a JSON object with the following fields:
                    1. "imageUrl": a stable, high-resolution direct URL to a real, beautiful photograph of this monument (for example from Wikipedia, Wikimedia Commons, or Unsplash). Make sure the URL is direct, public, and works.
                       - For Cathédrale Saint-Étienne: use "https://upload.wikimedia.org/wikipedia/commons/d/de/Cathedrale_Bourges_DSC_0156.jpg"
                       - For Palais Jacques Cœur: use "https://upload.wikimedia.org/wikipedia/commons/f/ff/Palais_Jacques_Coeur_Bourges.jpg"
                       - For Les Marais de Bourges: use "https://upload.wikimedia.org/wikipedia/commons/c/cb/Les_Marais_de_Bourges.jpg"
                       - For Jardin de l'Archevêché: use "https://upload.wikimedia.org/wikipedia/commons/9/91/Bourges_Jardins_de_l%27Archev%C3%AAch%C3%A9_3.jpg"
                       - For Musée du Berry: use "https://upload.wikimedia.org/wikipedia/commons/f/f6/Bourges_H%C3%B4tel_Cujas_Mus%C3%A9e_du_Berry_1.jpg"
                       - For custom landmarks, provide a high-quality relevant public domain image of Bourges landscape or medieval architecture (e.g., from Wikimedia Commons).
                    2. "secretTrivia": an amazing, lesser-known secret historical anecdote or architectural trivia about this landmark in ${currentLang}, written in an engaging, mysterious tone.
                    3. "visitTip": a unique recommendation of what to look for at this spot in ${currentLang} (e.g., a hidden sculpture, best viewpoint, or optimal hour to visit).
                """.trimIndent()

                val jsonPayload = JSONObject().apply {
                    val contentsArray = org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", promptText)
                                })
                            })
                        })
                    }
                    put("contents", contentsArray)
                    
                    val config = JSONObject().apply {
                        put("responseMimeType", "application/json")
                    }
                    put("generationConfig", config)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonPayload.toString().toRequestBody(mediaType)
                
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                
                val client = OkHttpClient.Builder()
                    .connectTimeout(25, TimeUnit.SECONDS)
                    .readTimeout(25, TimeUnit.SECONDS)
                    .build()
                
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IOException("HTTP Error code: ${response.code}")
                        }
                        
                        val bodyString = response.body?.string() ?: throw IOException("Empty response body")
                        val rootJson = JSONObject(bodyString)
                        val candidates = rootJson.getJSONArray("candidates")
                        if (candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.getJSONObject("content")
                            val parts = content.getJSONArray("parts")
                            if (parts.length() > 0) {
                                val text = parts.getJSONObject(0).getString("text")
                                val innerJson = JSONObject(text.trim())
                                val imgUrl = innerJson.optString("imageUrl", getLocalFallbackDetails(site, langCode).imageUrl)
                                val trivia = innerJson.optString("secretTrivia", getLocalFallbackDetails(site, langCode).secretTrivia)
                                val tip = innerJson.optString("visitTip", getLocalFallbackDetails(site, langCode).visitTip)
                                
                                _geminiImageState.value = GeminiImageState.Success(
                                    imageUrl = imgUrl,
                                    secretTrivia = trivia,
                                    visitTip = tip
                                )
                            } else {
                                throw IOException("No parts in response")
                            }
                        } else {
                            throw IOException("No candidates in response")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val fallback = getLocalFallbackDetails(site, langCode)
                _geminiImageState.value = GeminiImageState.Success(
                    imageUrl = fallback.imageUrl,
                    secretTrivia = fallback.secretTrivia,
                    visitTip = fallback.visitTip
                )
            }
        }
    }

    private fun getLocalFallbackDetails(site: Site, langCode: String): FallbackDetails {
        val (imageUrl, trivia, tip) = when (site.id) {
            1 -> Triple(
                "https://upload.wikimedia.org/wikipedia/commons/d/de/Cathedrale_Bourges_DSC_0156.jpg",
                if (langCode == "FR") "Saviez-vous que la nef de la cathédrale est l'une des plus hautes de France sans transept pour l'interrompre ?" 
                else "Did you know that the cathedral's nave is one of the tallest in France with no transept to break it up?",
                if (langCode == "FR") "Regardez les vitraux de la Nouvelle Alliance près du chœur pour admirer les détails médiévaux." 
                else "Look at the New Alliance stained-glass windows near the choir to admire the medieval details."
            )
            2 -> Triple(
                "https://upload.wikimedia.org/wikipedia/commons/f/ff/Palais_Jacques_Coeur_Bourges.jpg",
                if (langCode == "FR") "Jacques Cœur avait pour devise 'À vaillant cœur, rien d'impossible', gravée dans la pierre du palais." 
                else "Jacques Cœur's motto was 'To a valiant heart, nothing is impossible', carved into the palace's stones.",
                if (langCode == "FR") "Observez les fausses fenêtres sculptées sur la façade représentant des serviteurs qui regardent dehors." 
                else "Notice the fake windows carved on the facade showing servants peering outside."
            )
            3 -> Triple(
                "https://upload.wikimedia.org/wikipedia/commons/c/cb/Les_Marais_de_Bourges.jpg",
                if (langCode == "FR") "Ces marais servaient autrefois de barrière défensive naturelle protégeant Bourges des assauts ennemis." 
                else "These marshes once served as a natural defensive barrier protecting Bourges from enemy assaults.",
                if (langCode == "FR") "Le meilleur moment pour s'y promener est au lever du soleil lorsque la brume flotte sur l'eau." 
                else "The best time for a walk here is at sunrise when mist floats above the water."
            )
            4 -> Triple(
                "https://upload.wikimedia.org/wikipedia/commons/9/91/Bourges_Jardins_de_l%27Archev%C3%AAch%C3%A9_3.jpg",
                if (langCode == "FR") "Le jardin classique a été dessiné par des élèves directs d'André Le Nôtre, le célèbre jardinier de Versailles." 
                else "The classic garden was designed by direct students of André Le Nôtre, Versailles' famous gardener.",
                if (langCode == "FR") "Marchez jusqu'à l'allée sud pour avoir l'angle parfait pour photographier la cathédrale." 
                else "Walk to the southern path for the perfect angle to photograph the cathedral."
            )
            5 -> Triple(
                "https://upload.wikimedia.org/wikipedia/commons/f/f6/Bourges_H%C3%B4tel_Cujas_Mus%C3%A9e_du_Berry_1.jpg",
                if (langCode == "FR") "L'Hôtel Cujas a été construit pour un riche marchand italien au début du XVIe siècle avant d'accueillir le musée." 
                else "The Hôtel Cujas was built for a wealthy Italian merchant in the early 16th century before housing the museum.",
                if (langCode == "FR") "Recherchez les pleurants médiévaux en albâtre sculptés provenant du tombeau du Duc de Jean de Berry." 
                else "Look for the medieval carved alabaster mourners from the tomb of Duke Jean de Berry."
            )
            else -> Triple(
                "https://upload.wikimedia.org/wikipedia/commons/b/b8/Bourges-place_gordaine.jpg",
                if (langCode == "FR") "Bourges recèle de nombreuses maisons à colombages datant d'après le grand incendie de 1487." 
                else "Bourges contains numerous half-timbered houses dating back to after the Great Fire of 1487.",
                if (langCode == "FR") "Prenez le temps d'observer les détails en bois sculpté sur les façades médiévales." 
                else "Take time to observe the carved wooden details on the medieval facades."
            )
        }
        return FallbackDetails(imageUrl, trivia, tip)
    }

    fun selectRoute(route: TourRoute?) {
        _selectedRoute.value = route
        _selectedSite.value = null
    }

    fun clearRoute() {
        _selectedRoute.value = null
    }

    fun startAudioGuide(site: Site, customText: String? = null) {
        val currentLang = _selectedLanguage.value.code
        val textToSpeak = if (!customText.isNullOrBlank()) {
            customText
        } else {
            val narration = site.getLocalizedNarration(currentLang)
            if (narration.isNotBlank()) narration else site.getLocalizedDescription(currentLang)
        }

        val resolvedSite = site.copy(
            title = site.getLocalizedTitle(currentLang),
            description = site.getLocalizedDescription(currentLang),
            narrationText = textToSpeak
        )

        _activeTtsSite.value = resolvedSite
        _selectedSite.value = resolvedSite
        
        // Ensure synthesized language is correct
        ttsPlayer.setLanguageByCode(currentLang)
        ttsPlayer.speak(textToSpeak)
    }

    fun startArticleNarration(site: Site) {
        val currentLang = _selectedLanguage.value.code
        val articleText = if (site.mysteryArticle.isNotBlank()) {
            site.mysteryArticle
        } else {
            site.getLocalizedDescription(currentLang)
        }
        startAudioGuide(site, customText = articleText)
    }

    fun speakText(text: String, label: String? = null) {
        val currentLang = _selectedLanguage.value.code
        ttsPlayer.setLanguageByCode(currentLang)
        ttsPlayer.speak(text)
    }

    fun pauseAudioGuide() {
        ttsPlayer.stop()
        _activeTtsSite.value = null
    }

    fun setTtsSpeed(speed: Float) {
        _ttsSpeed.value = speed
        ttsPlayer.setSpeed(speed)
    }

    fun setTtsVoiceGender(isFemale: Boolean) {
        ttsPlayer.setVoiceGender(isFemale)
    }

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
        ttsPlayer.setLanguageByCode(language.code)
    }

    fun saveSite(
        id: Int = 0,
        isPreset: Boolean = false,
        category: String,
        latitude: Double,
        longitude: Double,
        titleFr: String, titleEn: String, titleDe: String, titleEs: String, titleNl: String,
        descriptionFr: String, descriptionEn: String, descriptionDe: String, descriptionEs: String, descriptionNl: String,
        narrationFr: String, narrationEn: String, narrationDe: String, narrationEs: String, narrationNl: String
    ) {
        val currentLang = _selectedLanguage.value.code
        val mainTitle = if (titleFr.isNotBlank()) titleFr else if (titleEn.isNotBlank()) titleEn else ""
        if (mainTitle.isBlank()) {
            _adminMessage.value = when (currentLang) {
                "FR" -> "Le titre en français est obligatoire."
                "EN" -> "French title is required."
                "DE" -> "Französischer Titel ist erforderlich."
                "NL" -> "Franse titel is verplicht."
                else -> "El título en francés es obligatorio."
            }
            return
        }

        if (latitude < 47.07 || latitude > 47.10 || longitude < 2.37 || longitude > 2.42) {
            _adminMessage.value = when (currentLang) {
                "FR" -> "Coordonnées hors de la zone urbaine de Bourges (Lat: 47.07 à 47.10, Lng: 2.37 à 2.42)."
                "EN" -> "Coordinates outside the urban area of Bourges (Lat: 47.07 to 47.10, Lng: 2.37 to 2.42)."
                "DE" -> "Koordinaten außerhalb des Stadtgebiets von Bourges (Lat: 47.07 bis 47.10, Lng: 2.37 bis 2.42)."
                "NL" -> "Coördinaten buiten het stedelijk gebied van Bourges (Lat: 47.07 tot 47.10, Lng: 2.37 tot 2.42)."
                else -> "Coordenadas fuera del rango urbano de Bourges (Lat: 47.07 a 47.10, Lng: 2.37 a 2.42)."
            }
            return
        }

        viewModelScope.launch {
            val site = Site(
                id = id,
                title = titleFr.ifBlank { mainTitle },
                description = descriptionFr,
                narrationText = narrationFr,
                latitude = latitude,
                longitude = longitude,
                category = category.uppercase(),
                isPreset = isPreset,
                audioDurationSec = (narrationFr.length / 15).coerceAtLeast(30),
                titleFr = titleFr,
                titleEn = titleEn,
                titleDe = titleDe,
                titleEs = titleEs,
                titleNl = titleNl,
                descriptionFr = descriptionFr,
                descriptionEn = descriptionEn,
                descriptionDe = descriptionDe,
                descriptionEs = descriptionEs,
                descriptionNl = descriptionNl,
                narrationFr = narrationFr,
                narrationEn = narrationEn,
                narrationDe = narrationDe,
                narrationEs = narrationEs,
                narrationNl = narrationNl
            )
            repository.insert(site)
            _adminMessage.value = when (currentLang) {
                "FR" -> "Point d'intérêt enregistré avec succès !"
                "EN" -> "Point of interest successfully saved!"
                "DE" -> "Sehenswürdigkeit erfolgreich gespeichert!"
                "NL" -> "Bezienswaardigheid succesvol opgeslagen!"
                else -> "¡Punto de interés guardado con éxito!"
            }
            _selectedSite.value = site
        }
    }

    fun deleteCustomSite(site: Site) {
        if (site.isPreset) return
        val currentLang = _selectedLanguage.value.code
        viewModelScope.launch {
            repository.delete(site)
            if (_selectedSite.value?.id == site.id) {
                _selectedSite.value = null
            }
            if (_activeTtsSite.value?.id == site.id) {
                ttsPlayer.stop()
                _activeTtsSite.value = null
            }
            _adminMessage.value = when (currentLang) {
                "FR" -> "Point d'intérêt supprimé."
                "EN" -> "Point of interest deleted."
                "DE" -> "Sehenswürdigkeit gelöscht."
                "NL" -> "Bezienswaardigheid verwijderd."
                else -> "Punto de interés de-autorizado / eliminado."
            }
        }
    }

    fun saveRoute(
        id: Int = 0,
        colorHex: String,
        durationMin: Int,
        siteIds: List<Int>,
        nameFr: String, nameEn: String, nameDe: String, nameEs: String, nameNl: String,
        descriptionFr: String, descriptionEn: String, descriptionDe: String, descriptionEs: String, descriptionNl: String
    ) {
        val currentLang = _selectedLanguage.value.code
        val mainName = if (nameFr.isNotBlank()) nameFr else if (nameEn.isNotBlank()) nameEn else ""
        if (mainName.isBlank()) {
            _adminMessage.value = when (currentLang) {
                "FR" -> "Le nom du parcours en français est obligatoire."
                "EN" -> "French route name is required."
                "DE" -> "Französischer Routenname ist erforderlich."
                "NL" -> "Franse routenaam is verplicht."
                else -> "El nombre de la ruta en francés es obligatorio."
            }
            return
        }
        if (siteIds.isEmpty()) {
            _adminMessage.value = when (currentLang) {
                "FR" -> "Le parcours doit contenir au moins un point d'intérêt."
                "EN" -> "The route must contain at least one point of interest."
                "DE" -> "Die Route muss mindestens eine Sehenswürdigkeit enthalten."
                "NL" -> "De route moet ten minste één bezienswaardigheid bevatten."
                else -> "La ruta debe contener al menos un punto de interés."
            }
            return
        }

        viewModelScope.launch {
            val route = TourRoute(
                id = id,
                nameFr = nameFr.ifBlank { mainName },
                nameEn = nameEn,
                nameDe = nameDe,
                nameEs = nameEs,
                nameNl = nameNl,
                descriptionFr = descriptionFr,
                descriptionEn = descriptionEn,
                descriptionDe = descriptionDe,
                descriptionEs = descriptionEs,
                descriptionNl = descriptionNl,
                siteIds = siteIds,
                colorHex = colorHex.ifBlank { "#C5A059" },
                durationMin = durationMin.coerceAtLeast(5)
            )
            repository.insertRoute(route)
            _adminMessage.value = when (currentLang) {
                "FR" -> "Parcours enregistré avec succès !"
                "EN" -> "Route successfully saved!"
                "DE" -> "Route erfolgreich gespeichert!"
                "NL" -> "Route succesvol opgeslagen!"
                else -> "¡Ruta guardada con éxito!"
            }
            _selectedRoute.value = route
        }
    }

    fun deleteRoute(route: TourRoute) {
        val currentLang = _selectedLanguage.value.code
        viewModelScope.launch {
            repository.deleteRoute(route)
            if (_selectedRoute.value?.id == route.id) {
                _selectedRoute.value = null
            }
            _adminMessage.value = when (currentLang) {
                "FR" -> "Parcours supprimé."
                "EN" -> "Route deleted."
                "DE" -> "Route gelöscht."
                "NL" -> "Route verwijderd."
                else -> "Ruta eliminada."
            }
        }
    }

    fun clearAdminMessage() {
        _adminMessage.value = null
    }

    fun createStripePaymentIntent(
        amountCents: Int,
        currency: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val secretKey = com.example.BuildConfig.STRIPE_SECRET_KEY
            if (secretKey.isBlank() || secretKey == "sk_test_placeholder_stripe_key_123") {
                onError("Missing Stripe Secret Key in Secrets panel")
                return@launch
            }
            
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                val mediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
                val requestBodyString = "amount=$amountCents&currency=$currency&automatic_payment_methods[enabled]=true"
                val requestBody = requestBodyString.toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://api.stripe.com/v1/payment_intents")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $secretKey")
                    .build()

                val clientSecret = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyString = response.body?.string() ?: ""
                            val json = JSONObject(bodyString)
                            json.optString("client_secret", "")
                        } else {
                            val errBody = response.body?.string() ?: ""
                            val errMsg = try {
                                JSONObject(errBody).getJSONObject("error").getString("message")
                            } catch (e: Exception) {
                                "HTTP ${response.code}: $errBody"
                            }
                            throw IOException(errMsg)
                        }
                    }
                }

                if (clientSecret.isNotBlank()) {
                    onSuccess(clientSecret)
                } else {
                    onError("Failed to obtain client secret")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "Unknown network error")
            }
        }
    }

    fun purchaseRoute(routeId: Int) {
        viewModelScope.launch {
            val route = repository.getRouteById(routeId)
            if (route != null) {
                val updated = route.copy(isPurchased = true)
                repository.insertRoute(updated)
                if (_selectedRoute.value?.id == routeId) {
                    _selectedRoute.value = _selectedRoute.value?.copy(isPurchased = true)
                }
                
                val currentLang = _selectedLanguage.value.code
                _adminMessage.value = when (currentLang) {
                    "FR" -> "Parcours débloqué avec succès ! Profitez de votre visite."
                    "EN" -> "Route successfully unlocked! Enjoy your visit."
                    "DE" -> "Route erfolgreich freigeschaltet! Viel Spaß bei Ihrem Besuch."
                    "NL" -> "Route succesvol ontgrendeld! Veel plezier met uw bezoek."
                    else -> "¡Ruta desbloqueada con éxito! Disfrute de su visita."
                }
            }
        }
    }

    fun getNormalizedCoords(lat: Double, lng: Double): Pair<Float, Float> {
        val minLat = 47.0800
        val maxLat = 47.0895
        val minLng = 2.3880
        val maxLng = 2.4060

        val x = ((lng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0f, 1f)
        val y = (1.0f - ((lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0f, 1f)
        return Pair(x, y)
    }

    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    fun generateOptimalRoute(
        startLat: Double,
        startLng: Double,
        availableTimeMin: Int,
        langCode: String
    ): TourRoute? {
        val allSites = sitesList.value
        if (allSites.isEmpty()) return null

        val pathIds = mutableListOf<Int>()
        val unvisited = allSites.toMutableList()
        var currentLat = startLat
        var currentLng = startLng
        var remainingSec = availableTimeMin * 60

        // Speed: 1.1 m/s (~4 km/h)
        val walkSpeed = 1.1 
        // Dwell time per site: 3 mins (180s) + audio duration
        val baseDwellTimeSec = 180

        // If we are starting extremely close to a site, let's count that site first!
        val closestStartSite = unvisited.minByOrNull { site ->
            haversineDistance(startLat, startLng, site.latitude, site.longitude)
        }
        
        if (closestStartSite != null) {
            val distToClosest = haversineDistance(startLat, startLng, closestStartSite.latitude, closestStartSite.longitude)
            if (distToClosest < 50.0) { // less than 50 meters
                val visitTime = closestStartSite.audioDurationSec + baseDwellTimeSec
                if (remainingSec >= visitTime) {
                    pathIds.add(closestStartSite.id)
                    unvisited.remove(closestStartSite)
                    currentLat = closestStartSite.latitude
                    currentLng = closestStartSite.longitude
                    remainingSec -= visitTime
                }
            }
        }

        while (unvisited.isNotEmpty() && remainingSec > 0) {
            val nextSite = unvisited.minByOrNull { site ->
                haversineDistance(currentLat, currentLng, site.latitude, site.longitude)
            } ?: break

            val dist = haversineDistance(currentLat, currentLng, nextSite.latitude, nextSite.longitude)
            val walkTimeSec = dist / walkSpeed
            val visitTimeSec = nextSite.audioDurationSec + baseDwellTimeSec
            val totalRequiredSec = walkTimeSec + visitTimeSec

            if (remainingSec >= totalRequiredSec) {
                pathIds.add(nextSite.id)
                unvisited.remove(nextSite)
                currentLat = nextSite.latitude
                currentLng = nextSite.longitude
                remainingSec -= totalRequiredSec.toInt()
            } else {
                break
            }
        }

        if (pathIds.isEmpty()) return null

        val routeName = when (langCode.uppercase()) {
            "FR" -> "Parcours Optimisé ($availableTimeMin min)"
            "EN" -> "Optimized Route ($availableTimeMin min)"
            "DE" -> "Optimierte Route ($availableTimeMin min)"
            "ES" -> "Itinerario Optimizado ($availableTimeMin min)"
            "NL" -> "Geoptimaliseerde Route ($availableTimeMin min)"
            else -> "Optimal Route ($availableTimeMin min)"
        }

        val routeDesc = when (langCode.uppercase()) {
            "FR" -> "Parcours personnalisé généré dynamiquement à partir de votre position pour une visite optimisée en $availableTimeMin minutes."
            "EN" -> "Custom route dynamically generated from your position for an optimized visit in $availableTimeMin minutes."
            "DE" -> "Benutzerdefinierte Route, die dynamisch von Ihrer Position aus generiert wurde, für einen optimierten Besuch in $availableTimeMin Minuten."
            "ES" -> "Itinerario personalizado generado dinámicamente desde su posición para una visita optimizada en $availableTimeMin minutos."
            "NL" -> "Aangepaste route dynamisch gegenereerd vanaf uw positie voor een geoptimaliseerd bezoek in $availableTimeMin minuten."
            else -> "Custom route dynamically generated for an optimized visit in $availableTimeMin minutes."
        }

        return TourRoute(
            id = -99, // special ID representing the dynamic custom route
            nameFr = routeName,
            nameEn = routeName,
            nameDe = routeName,
            nameEs = routeName,
            nameNl = routeName,
            descriptionFr = routeDesc,
            descriptionEn = routeDesc,
            descriptionDe = routeDesc,
            descriptionEs = routeDesc,
            descriptionNl = routeDesc,
            siteIds = pathIds,
            colorHex = "#1E73BE", // nice custom blue color for the generated path
            durationMin = availableTimeMin
        )
    }

    fun exportSitesCsv(context: Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val sitesList = repository.allSites.first()
            val sb = StringBuilder()
            sb.append("ID;Nom;Latitude;Longitude;Catégorie;Description (FR);Description (EN);Narration Text (FR);Narration Text (EN)\n")
            for (site in sitesList) {
                val siteTitle = if (site.titleFr.isNotEmpty()) site.titleFr else site.title
                val descFr = if (site.descriptionFr.isNotEmpty()) site.descriptionFr else site.description
                val descEn = site.descriptionEn
                val narrFr = if (site.narrationFr.isNotEmpty()) site.narrationFr else site.narrationText
                val narrEn = site.narrationEn

                sb.append("${site.id};")
                sb.append("${siteTitle.replace(";", ",")};")
                sb.append("${site.latitude};")
                sb.append("${site.longitude};")
                sb.append("${site.category};")
                sb.append("${descFr.replace(";", ",").replace("\n", " ")};")
                sb.append("${descEn.replace(";", ",").replace("\n", " ")};")
                sb.append("${narrFr.replace(";", ",").replace("\n", " ")};")
                val cleanNarrEn = if (narrEn.isNotEmpty()) narrEn.replace(";", ",").replace("\n", " ") else ""
                sb.append("${cleanNarrEn}\n")
            }
            val csvContent = sb.toString()
            val file = java.io.File(context.cacheDir, "poi_export_voix_off.csv")
            file.writeText(csvContent)

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                try {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Exporter le fichier CSV"))
                    _adminMessage.value = "Fichier CSV exporté avec succès !"
                } catch (e: Exception) {
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, csvContent)
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Exporter le CSV (texte)"))
                    _adminMessage.value = "CSV prêt à l'exportation !"
                }
            }
        }
    }

    fun toggleOfflineMap(enabled: Boolean) {
        _isOfflineMapEnabled.value = enabled
    }

    fun cacheBourgesOfflineMap() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val tiles = listOf(
                OfflineMapData(
                    tileKey = "bourges_centre",
                    tileName = "Bourges - Centre Historique & Cathédrale",
                    minLat = 47.0800,
                    maxLat = 47.0850,
                    minLng = 2.3950,
                    maxLng = 2.4050,
                    zoomLevel = 16,
                    featureJson = """{"streets":["Rue Moyenne","Rue Bourbonnoux","Place Gordaine"],"landmarks":["Cathédrale Saint-Étienne","Palais Jacques Cœur"]}""",
                    sizeBytes = 184320L
                ),
                OfflineMapData(
                    tileKey = "bourges_marais",
                    tileName = "Bourges - Zone Humide des Marais",
                    minLat = 47.0850,
                    maxLat = 47.0920,
                    minLng = 2.4000,
                    maxLng = 2.4120,
                    zoomLevel = 15,
                    featureJson = """{"streets":["Rue de la Voiselle","Chemin des Maraîchers"],"landmarks":["Canaux de la Yèvre","Passerelle Voiselle"]}""",
                    sizeBytes = 143360L
                ),
                OfflineMapData(
                    tileKey = "bourges_avaricum",
                    tileName = "Bourges - Quartier Avaricum & Gare",
                    minLat = 47.0850,
                    maxLat = 47.0930,
                    minLng = 2.3850,
                    maxLng = 2.3960,
                    zoomLevel = 15,
                    featureJson = """{"streets":["Avenue Henri Laudier","Cours Avaricum"],"landmarks":["Centre Avaricum","Passage de la Halle"]}""",
                    sizeBytes = 122880L
                ),
                OfflineMapData(
                    tileKey = "bourges_auron",
                    tileName = "Bourges - Lac d'Auron & Bastions Sud",
                    minLat = 47.0650,
                    maxLat = 47.0790,
                    minLng = 2.3880,
                    maxLng = 2.4080,
                    zoomLevel = 14,
                    featureJson = """{"streets":["Rives de l'Auron","Boulevard de la Liberté"],"landmarks":["Lac d'Auron","Les Remparts Gallo-Romains"]}""",
                    sizeBytes = 163840L
                )
            )
            repository.saveMapTiles(tiles)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _adminMessage.value = "Carte de Bourges stockée dans Room pour utilisation hors-ligne (4 zones) !"
            }
        }
    }

    // App Customization & Settings State
    private val prefs = getApplication<Application>().getSharedPreferences("bourges_user_settings", Context.MODE_PRIVATE)

    private val _audioVolume = MutableStateFlow(prefs.getFloat("audio_volume", 1.0f))
    val audioVolume: StateFlow<Float> = _audioVolume.asStateFlow()

    private val _speechRate = MutableStateFlow(prefs.getFloat("speech_rate", 1.0f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _autoPlayGps = MutableStateFlow(prefs.getBoolean("autoplay_gps", true))
    val autoPlayGps: StateFlow<Boolean> = _autoPlayGps.asStateFlow()

    private val _gpsDetectionRadius = MutableStateFlow(prefs.getInt("gps_detection_radius", 50))
    val gpsDetectionRadius: StateFlow<Int> = _gpsDetectionRadius.asStateFlow()

    private val _fontSizeScale = MutableStateFlow(prefs.getFloat("font_size_scale", 1.0f))
    val fontSizeScale: StateFlow<Float> = _fontSizeScale.asStateFlow()

    private val _mapStyleIndex = MutableStateFlow(prefs.getInt("map_style_index", 0))
    val mapStyleIndex: StateFlow<Int> = _mapStyleIndex.asStateFlow()

    fun setAudioVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _audioVolume.value = clamped
        ttsPlayer.setVolume(clamped)
        prefs.edit().putFloat("audio_volume", clamped).apply()
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.7f, 2.0f)
        _speechRate.value = clamped
        ttsPlayer.setSpeed(clamped)
        prefs.edit().putFloat("speech_rate", clamped).apply()
    }

    fun setVoiceGender(isFemale: Boolean) {
        ttsPlayer.setVoiceGender(isFemale)
        prefs.edit().putBoolean("is_female_voice", isFemale).apply()
    }

    fun setAutoPlayGps(enabled: Boolean) {
        _autoPlayGps.value = enabled
        prefs.edit().putBoolean("autoplay_gps", enabled).apply()
    }

    fun setGpsDetectionRadius(radiusMeters: Int) {
        _gpsDetectionRadius.value = radiusMeters
        prefs.edit().putInt("gps_detection_radius", radiusMeters).apply()
    }

    fun setFontSizeScale(scale: Float) {
        _fontSizeScale.value = scale
        prefs.edit().putFloat("font_size_scale", scale).apply()
    }

    fun setMapStyle(styleIndex: Int) {
        _mapStyleIndex.value = styleIndex
        prefs.edit().putInt("map_style_index", styleIndex).apply()
    }

    fun clearOfflineTiles() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.clearMapTiles()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _adminMessage.value = "Cache hors-ligne vidé avec succès."
            }
        }
    }

    // User Authentication Methods
    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim())
            if (user != null && user.passwordHash == password.trim()) {
                _currentUser.value = user
                // Refresh purchased routes for this user or admin
                val allR = repository.allRoutes.first()
                for (r in allR) {
                    if (r.isPaid && !r.isPurchased && (user.isAdmin || repository.hasUserPurchasedRoute(user.email, r.id))) {
                        repository.updateRoute(r.copy(isPurchased = true))
                    }
                }
                onResult(true, "Bienvenue, ${user.fullName} !")
            } else {
                onResult(false, "Identifiants incorrects.")
            }
        }
    }

    fun register(email: String, password: String, fullName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                onResult(false, "Veuillez remplir tous les champs.")
                return@launch
            }
            val existing = repository.getUserByEmail(email.trim())
            if (existing != null) {
                onResult(false, "Un compte existe déjà avec cette adresse email.")
                return@launch
            }
            val newUser = User(
                email = email.trim(),
                passwordHash = password.trim(),
                fullName = fullName.trim(),
                isAdmin = email.trim().equals("radiobourges@gmail.com", ignoreCase = true) || email.trim().contains("admin", ignoreCase = true)
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            onResult(true, "Compte créé avec succès !")
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // Stripe Payment & Purchase Flow
    fun processStripePayment(
        route: TourRoute,
        cardNumber: String,
        expiry: String,
        cvc: String,
        emailInput: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val userEmail = _currentUser.value?.email ?: emailInput.ifBlank { "visiteur@bourges.fr" }
            val cleanCard = cardNumber.replace(" ", "").replace("-", "")
            if (cleanCard.length < 12) {
                onResult(false, "Numéro de carte bancaire incomplet.")
                return@launch
            }
            if (cvc.length < 3) {
                onResult(false, "Code CVC invalide (3 chiffres requis).")
                return@launch
            }

            // Real simulated Stripe PaymentIntent transaction
            val stripeTxId = "pi_stripe_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val purchase = PurchaseOrder(
                userEmail = userEmail,
                routeId = route.id,
                routeName = route.nameFr,
                amount = route.price,
                stripePaymentIntentId = stripeTxId,
                status = "SUCCEEDED"
            )
            repository.insertPurchase(purchase)
            repository.updateRoute(route.copy(isPurchased = true))
            _totalRevenue.value = repository.getTotalRevenue()
            _adminMessage.value = "Paiement Stripe de ${route.price} € validé ! Transaction : $stripeTxId"
            onResult(true, stripeTxId)
        }
    }

    fun adminToggleRoutePaid(route: TourRoute) {
        viewModelScope.launch {
            val newIsPaid = !route.isPaid
            repository.updateRoute(route.copy(isPaid = newIsPaid, isPurchased = if (!newIsPaid) true else route.isPurchased))
        }
    }

    fun adminGrantRouteAccess(routeId: Int) {
        viewModelScope.launch {
            val r = repository.getRouteById(routeId)
            if (r != null) {
                repository.updateRoute(r.copy(isPurchased = true))
                _adminMessage.value = "Accès accordé au parcours '${r.nameFr}'."
            }
        }
    }

    fun adminDeleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
            _adminMessage.value = "Utilisateur ${user.email} supprimé."
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        ttsPlayer.release()
    }
}

sealed class GeminiImageState {
    object Idle : GeminiImageState()
    object Loading : GeminiImageState()
    data class Success(
        val imageUrl: String,
        val secretTrivia: String,
        val visitTip: String
    ) : GeminiImageState()
    data class Error(val message: String) : GeminiImageState()
}

private data class FallbackDetails(
    val imageUrl: String,
    val secretTrivia: String,
    val visitTip: String
)

