package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val descriptionFr: String = "",
    val descriptionEn: String = "",
    val descriptionDe: String = "",
    val descriptionEs: String = "",
    val descriptionNl: String = "",
    val narrationFr: String = "",
    val narrationEn: String = "",
    val narrationDe: String = "",
    val narrationEs: String = "",
    val narrationNl: String = "",
    val voiceOverAudio: String = "",
    val mysteryArticle: String = "",
    val imageUrl: String = ""
) : Serializable {

    fun getLocalizedTitle(lang: String): String {
        val upperLang = lang.uppercase()
        val localized = when (upperLang) {
            "FR" -> titleFr
            "EN" -> titleEn
            "DE" -> titleDe
            "ES" -> titleEs
            "NL" -> titleNl
            else -> ""
        }
        return if (localized.isNotBlank()) localized else title
    }

    fun getLocalizedDescription(lang: String): String {
        val upperLang = lang.uppercase()
        val localized = when (upperLang) {
            "FR" -> descriptionFr
            "EN" -> descriptionEn
            "DE" -> descriptionDe
            "ES" -> descriptionEs
            "NL" -> descriptionNl
            else -> ""
        }
        return if (localized.isNotBlank()) localized else description
    }

    fun getLocalizedNarration(lang: String): String {
        val upperLang = lang.uppercase()
        val localized = when (upperLang) {
            "FR" -> narrationFr
            "EN" -> narrationEn
            "DE" -> narrationDe
            "ES" -> narrationEs
            "NL" -> narrationNl
            else -> ""
        }
        return if (localized.isNotBlank()) localized else narrationText
    }
}
