package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "routes")
data class TourRoute(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameFr: String,
    val nameEn: String,
    val nameDe: String,
    val nameEs: String,
    val nameNl: String,
    val descriptionFr: String,
    val descriptionEn: String,
    val descriptionDe: String,
    val descriptionEs: String,
    val descriptionNl: String,
    val siteIds: List<Int>, // list of site IDs in order
    val colorHex: String,
    val durationMin: Int,
    val isPaid: Boolean = false,
    val isPurchased: Boolean = false,
    val price: Double = 9.0,
    val articleTitle: String = "",
    val articleContent: String = "",
    val imageUrl: String = ""
) : Serializable {

    fun getLocalizedName(lang: String): String {
        val upperLang = lang.uppercase()
        val name = when (upperLang) {
            "FR" -> nameFr
            "EN" -> nameEn
            "DE" -> nameDe
            "ES" -> nameEs
            "NL" -> nameNl
            else -> ""
        }
        return if (name.isNotBlank()) name else nameFr
    }

    fun getLocalizedDescription(lang: String): String {
        val upperLang = lang.uppercase()
        val desc = when (upperLang) {
            "FR" -> descriptionFr
            "EN" -> descriptionEn
            "DE" -> descriptionDe
            "ES" -> descriptionEs
            "NL" -> descriptionNl
            else -> ""
        }
        return if (desc.isNotBlank()) desc else descriptionFr
    }

    fun getLocalizedArticleTitle(lang: String): String = if (articleTitle.isNotBlank()) articleTitle else getLocalizedName(lang)

    fun getLocalizedArticleContent(lang: String): String = if (articleContent.isNotBlank()) articleContent else getLocalizedDescription(lang)
}

