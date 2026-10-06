package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.audio.MultilingualNarrationService
import java.io.Serializable

@Entity(tableName = "sites")
data class Site(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val narrationText: String,
    val latitude: Double,
    val longitude: Double,
    val category: String, // "CATHEDRAL", "PALACE", "NATURE", "MUSEUM", "CUSTOM"
    val isPreset: Boolean = false,
    val audioDurationSec: Int = 60,
    val titleFr: String = "",
    val titleEn: String = "",
    val titleDe: String = "",
    val titleEs: String = "",
    val titleNl: String = "",
    val titleIt: String = "",
    val descriptionFr: String = "",
    val descriptionEn: String = "",
    val descriptionDe: String = "",
    val descriptionEs: String = "",
    val descriptionNl: String = "",
    val descriptionIt: String = "",
    val narrationFr: String = "",
    val narrationEn: String = "",
    val narrationDe: String = "",
    val narrationEs: String = "",
    val narrationNl: String = "",
    val narrationIt: String = "",
    val voiceOverAudio: String = "",
    val mysteryArticle: String = "",
    val imageUrl: String = ""
) : Serializable {

    fun getLocalizedTitle(lang: String): String {
        return MultilingualNarrationService.getTitle(this, lang)
    }

    fun getLocalizedDescription(lang: String): String {
        return MultilingualNarrationService.getDescription(this, lang)
    }

    fun getLocalizedNarration(lang: String): String {
        return MultilingualNarrationService.getNarration(this, lang)
    }
}
