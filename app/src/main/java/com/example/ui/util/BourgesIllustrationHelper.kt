package com.example.ui.util

import com.example.R
import com.example.data.Site
import com.example.data.TourRoute

object BourgesIllustrationHelper {

    /**
     * Resolves the best illustration for any Site in Bourges.
     * Guarantees an authentic, high-quality visual asset is always returned.
     */
    fun resolveImageForSite(site: Site?): Any {
        if (site == null) return R.drawable.img_cathedrale

        // If a remote image URL is already set, prefer it
        if (site.imageUrl.isNotBlank()) {
            return site.imageUrl
        }

        val title = site.title.lowercase()
        val category = site.category.uppercase()

        return when {
            // Cathedral & crypt & Gothic religious landmarks
            title.contains("cathédrale") || title.contains("cathedrale") ||
            title.contains("saint-étienne") || title.contains("crypte") ||
            title.contains("horloge astronomique") || title.contains("athanor") ||
            title.contains("vitraux") || title.contains("clocher") -> R.drawable.img_cathedrale

            // Jacques Coeur & Palaces
            title.contains("jacques cœur") || title.contains("jacques coeur") ||
            title.contains("palais") || title.contains("argentier") ||
            title.contains("toison d'or") || title.contains("grand maître") ||
            title.contains("table d'émeraude") || title.contains("souffleur") -> R.drawable.img_palais_jacques_coeur

            // Marshes & nature
            title.contains("marais") || title.contains("yèvre") ||
            title.contains("voiselle") || title.contains("vouivre") ||
            title.contains("barque") || title.contains("plaque") ||
            title.contains("nature") || category == "NATURE" -> R.drawable.img_marais

            // Gardens & Archbishop
            title.contains("jardin") || title.contains("archevêché") ||
            title.contains("archeveche") || title.contains("méridien") ||
            title.contains("parc") || title.contains("promenade") -> R.drawable.img_jardin_archeveche

            // Museums & Renaissance mansions & Antiquity
            title.contains("musée") || title.contains("musee") ||
            title.contains("cujas") || title.contains("berry") ||
            title.contains("échevins") || title.contains("echevins") ||
            title.contains("avaricum") || title.contains("momie") ||
            category == "MUSEUM" -> R.drawable.img_musee_berry

            // Category fallbacks
            category == "CATHEDRAL" -> R.drawable.img_cathedrale
            category == "PALACE" || category == "PALAIS" -> R.drawable.img_palais_jacques_coeur
            category == "SOUTERRAINS" -> R.drawable.img_cathedrale
            category == "ALCHIMIE" || category == "SORCELLERIE" || category == "TEMPLIERS" -> R.drawable.img_palais_jacques_coeur
            else -> R.drawable.img_cathedrale
        }
    }

    /**
     * Resolves the flagship illustration for a TourRoute.
     */
    fun resolveImageForRoute(route: TourRoute?): Any {
        if (route == null) return R.drawable.img_cathedrale
        if (route.imageUrl.isNotBlank()) return route.imageUrl

        return when (route.id) {
            1 -> R.drawable.img_cathedrale // Cœur Historique & Incontournables
            2 -> R.drawable.img_palais_jacques_coeur // Les Énigmes Alchimiques
            3 -> R.drawable.img_cathedrale // Ombres & Légendes Médiévales
            4 -> R.drawable.img_marais // Les Marais Secrets & Nature
            5 -> R.drawable.img_palais_jacques_coeur // Jacques Cœur & les Grands Argentiers
            6 -> R.drawable.img_musee_berry // Souterrains & Mystères Médiévaux
            7 -> R.drawable.img_jardin_archeveche // Renaissance & Hôtels Particuliers
            8 -> R.drawable.img_cathedrale // Secrets Stellaires & L'Alignement Solsticial
            else -> R.drawable.img_cathedrale
        }
    }

    /**
     * Generates a descriptive caption for the site's illustration to be featured in the article.
     */
    fun getIllustrationCaption(site: Site?, lang: String): String {
        if (site == null) return "Illustration architecturale de Bourges"
        val title = site.title.lowercase()
        val isFr = lang.equals("FR", ignoreCase = true)

        return when {
            title.contains("cathédrale") || title.contains("cathedrale") ->
                if (isFr) "Illustration : Façade gothique flamboyant et verrières du XIIIe siècle" else "Illustration: Gothic flamboyant facade and 13th-century stained glass"
            title.contains("jacques cœur") || title.contains("jacques coeur") ->
                if (isFr) "Illustration : La Grant' Maison et les sculptures hermétiques de Jacques Cœur" else "Illustration: Jacques Cœur's Grand Palace and hermetic stonework"
            title.contains("marais") ->
                if (isFr) "Illustration : Canaux navigables et parcelles maraîchères de l'Yèvre" else "Illustration: Canals and historic market gardens of the Yèvre river"
            title.contains("jardin") || title.contains("archevêché") ->
                if (isFr) "Illustration : Perspectives classiques à la Le Nôtre et vue sur l'abside" else "Illustration: Le Nôtre classical vistas overlooking the cathedral apse"
            title.contains("cujas") || title.contains("berry") ->
                if (isFr) "Illustration : Hôtel Renaissance en briques et pierre, mémoire d'Avaricum" else "Illustration: Renaissance brick and limestone mansion, memory of Avaricum"
            title.contains("alchimie") || site.category.equals("ALCHIMIE", ignoreCase = true) ->
                if (isFr) "Illustration : Tracé ésotérique et symboles gravés de la pierre philosophale" else "Illustration: Esoteric geometry and carved signs of the philosopher's stone"
            else ->
                if (isFr) "Illustration : Trésor du patrimoine historique et architectural berruyer" else "Illustration: Historic and architectural treasure of Bourges heritage"
        }
    }
}
