package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Site::class,
        TourRoute::class,
        OfflineMapData::class,
        User::class,
        PurchaseOrder::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun routeDao(): RouteDao
    abstract fun offlineMapDao(): OfflineMapDao
    abstract fun userDao(): UserDao
    abstract fun purchaseDao(): PurchaseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bourges_guide_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(context.applicationContext, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(
                        context,
                        database.siteDao(),
                        database.routeDao(),
                        database.offlineMapDao(),
                        database.userDao(),
                        database.purchaseDao()
                    )
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(
                        context,
                        database.siteDao(),
                        database.routeDao(),
                        database.offlineMapDao(),
                        database.userDao(),
                        database.purchaseDao()
                    )
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    try {
                        if (database.siteDao().getSiteCount() < 170) {
                            val csvContent = context.assets.open("poi_export.csv").use { stream ->
                                stream.readBytes().toString(Charsets.UTF_8)
                            }
                            val sitesToInsert = CsvManager.parseSitesFromCsv(csvContent)
                            if (sitesToInsert.isNotEmpty()) {
                                database.siteDao().insertSites(sitesToInsert)
                            }
                        }
                        if (database.offlineMapDao().getTileCount() < 3) {
                            seedOfflineMapTiles(database.offlineMapDao())
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AppDatabase", "Error ensuring full 170 sites onOpen", e)
                    }
                }
            }
        }

        private suspend fun seedOfflineMapTiles(offlineMapDao: OfflineMapDao) {
            val tiles = listOf(
                OfflineMapData(
                    tileKey = "bourges_center_high",
                    tileName = "Bourges - Cœur Historique & Cathédrale",
                    minLat = 47.0780,
                    maxLat = 47.0890,
                    minLng = 2.3900,
                    maxLng = 2.4060,
                    zoomLevel = 16,
                    featureJson = """{"streets":["Rue Bourbonnoux","Rue Moyenne","Rue Mirebeau","Rue Coursarlon","Rue Édouard Branly"],"landmarks":["Cathédrale Saint-Étienne","Palais Jacques Cœur","Hôtel Lallemant","Place Gordaine","Musée du Berry"]}""",
                    sizeBytes = 327680L
                ),
                OfflineMapData(
                    tileKey = "bourges_marais",
                    tileName = "Bourges - Les Marais & Canaux de l'Yèvre",
                    minLat = 47.0850,
                    maxLat = 47.0980,
                    minLng = 2.3980,
                    maxLng = 2.4180,
                    zoomLevel = 15,
                    featureJson = """{"streets":["Chemin des Marais","Avenue de Saint-Amand","Rives de l'Yèvre"],"landmarks":["Marais Hauts","Marais Bas","Canaux de la Voiselle"]}""",
                    sizeBytes = 245760L
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
            offlineMapDao.insertTiles(tiles)
        }

        private suspend fun populateDatabase(
            context: Context,
            siteDao: SiteDao,
            routeDao: RouteDao,
            offlineMapDao: OfflineMapDao,
            userDao: UserDao,
            purchaseDao: PurchaseDao
        ) {
            seedOfflineMapTiles(offlineMapDao)

            // Seed default users (Admin + Test user)
            userDao.insertUser(
                User(
                    id = 1,
                    email = "radiobourges@gmail.com",
                    passwordHash = "bourges2026",
                    fullName = "Radio Bourges Admin",
                    isAdmin = true
                )
            )
            userDao.insertUser(
                User(
                    id = 2,
                    email = "admin@bourges.fr",
                    passwordHash = "admin",
                    fullName = "Administrateur Municipal",
                    isAdmin = true
                )
            )
            userDao.insertUser(
                User(
                    id = 3,
                    email = "visiteur@bourges.fr",
                    passwordHash = "visiteur123",
                    fullName = "Jean Dupont",
                    isAdmin = false
                )
            )

            // 8 Curated, Beautiful Tour Routes (2 Free, 6 Paid at 9.0 €)
            val presetRoutes = listOf(
                // 1. GRATUIT (0 €)
                TourRoute(
                    id = 1,
                    nameFr = "L'Athanor et le Cœur d'Or : Les Incontournables",
                    nameEn = "The Athanor and the Golden Heart: Highlights",
                    nameDe = "Der Athanor und das Goldene Herz",
                    nameEs = "El Atanor y el Corazón de Oro: Lo Esencial",
                    nameNl = "De Athanor en het Gouden Hart: Hoogtepunten",
                    descriptionFr = "Le grand parcours fondateur de Bourges reliant la cathédrale Saint-Étienne, l'Horloge Astronomique de 1424, la mystérieuse crypte et le somptueux Palais Jacques Cœur.",
                    descriptionEn = "Bourges' signature trail linking Saint-Étienne Cathedral, the 1424 Astronomical Clock, the lower crypt, and the sumptuous Jacques Cœur Palace.",
                    descriptionDe = "Die Gründungsroute von Bourges, die Kathedrale, Astronomische Uhr, Krypta und Jacques-Cœur-Palast verbindet.",
                    descriptionEs = "La gran ruta fundacional de Bourges que une la Catedral, el Reloj Astronómico, la cripta y el Palacio Jacques Cœur.",
                    descriptionNl = "De grote grondleggersroute van Bourges die de Kathedraal, Astronomische Klok, crypte en Jacques Cœur Paleis verbindt.",
                    siteIds = listOf(1, 4, 7, 2),
                    colorHex = "#C5A059",
                    durationMin = 45,
                    isPaid = false,
                    isPurchased = true,
                    price = 0.0,
                    articleTitle = "Le Siècle d'Or de Charles VII et l'Athanor Gothique",
                    articleContent = "Ce parcours fondamental vous guide à travers le cœur névralgique de la cité médiévale. De la cathédrale Saint-Étienne, chef-d'œuvre sans transept où les bâtisseurs ont inscrit les étapes du Grand Œuvre alchimique, à l'incroyable Horloge Astronomique de 1424 offerte par le roi Charles VII, jusqu'à l'immense palais de son Grand Argentier Jacques Cœur. Découvrez comment la capitale du Berry a sauvé le royaume de France et abrité les plus grands maîtres bâtisseurs d'Occident.",
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Bourges_Cathedrale_Saint-Etienne_01.jpg/800px-Bourges_Cathedrale_Saint-Etienne_01.jpg"
                ),
                // 2. GRATUIT (0 €)
                TourRoute(
                    id = 2,
                    nameFr = "L'Échappée Verte & Légende de la Vouivre",
                    nameEn = "Green Waters & The Legend of the Vouivre",
                    nameDe = "Grüner Pfad & Die Legende der Vouivre",
                    nameEs = "Paseo Verde y la Leyenda de la Vouivre",
                    nameNl = "Groene Tocht & De Legende van de Vouivre",
                    descriptionFr = "Une promenade poétique entre le Jardin classique de l'Archevêché, les 135 hectares de marais potagers entourés d'eau et l'église Saint-Bonnet.",
                    descriptionEn = "A poetic stroll connecting the Archbishop's Garden, the 135 hectares of idyllic marsh canals, and Saint-Bonnet church.",
                    descriptionDe = "Ein Spaziergang durch die Gärten des Erzbistums, die 135 Hektar Kanäle und die Kirche Saint-Bonnet.",
                    descriptionEs = "Un paseo poético entre los jardines del Arzobispado, las marismas flotantes y la iglesia de Saint-Bonnet.",
                    descriptionNl = "Een poëtische wandeling tussen de Tuinen van het Aartsbisdom, de 135 hectare moerassen en de Saint-Bonnet kerk.",
                    siteIds = listOf(13, 5, 19),
                    colorHex = "#2E7D32",
                    durationMin = 40,
                    isPaid = false,
                    isPurchased = true,
                    price = 0.0,
                    articleTitle = "La Féerie des Eaux et la Créature au Front de Rubis",
                    articleContent = "Quittez la pierre pour l'eau et la chlorophylle. Ce parcours longe le Jardin de l'Archevêché avant de s'enfoncer dans les 135 hectares de marais potagers entourés de canaux. C'est ici, dans ces brumes crépusculaires, que la légende berrichonne situe le domaine de la Vouivre, serpent ailé fantastique dont l'œil de rubis étincelle à fleur d'eau. La balade se conclut devant l'église Saint-Bonnet et la mémoire de sa flamme perpétuelle.",
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Marais_de_Bourges_-_Canal.jpg/800px-Marais_de_Bourges_-_Canal.jpg"
                ),
                // 3. PAYANT (9 €)
                TourRoute(
                    id = 3,
                    nameFr = "Le Grand Œuvre Alchimique & Fulcanelli",
                    nameEn = "The Great Alchemical Work & Fulcanelli",
                    nameDe = "Das Große Werk der Alchemie & Fulcanelli",
                    nameEs = "La Gran Obra Alquímica y Fulcanelli",
                    nameNl = "Het Grote Alchemistische Werk & Fulcanelli",
                    descriptionFr = "Le circuit initiatique suprême décryptant les 30 caissons hermétiques de l'Hôtel Lallemant, la rue Bourbonnoux, la Porte des Étoiles et la Tour des Échevins.",
                    descriptionEn = "The supreme esoteric circuit decoding Hôtel Lallemant's 30 coffered caissons, Rue Bourbonnoux, Star Gate, and the Échevins Tower.",
                    descriptionDe = "Die hermetische Route: Entschlüsseln Sie die 30 Kassetten des Hôtel Lallemant und geheime Alchemistenzeichen.",
                    descriptionEs = "El circuito hermético supremo que descifra los 30 casetones del Hôtel Lallemant y símbolos ocultos.",
                    descriptionNl = "De ultieme hermetische route die de 30 cassetten van Hôtel Lallemant en alchemistische tekens ontcijfert.",
                    siteIds = listOf(3, 1, 11, 20, 10),
                    colorHex = "#D32F2F",
                    durationMin = 65,
                    isPaid = true,
                    isPurchased = false,
                    price = 9.0,
                    articleTitle = "L'Énigme des 30 Caissons Hermétiques et de la Pierre Philosophale",
                    articleContent = "Considéré par les initiés comme le parcours hermétique le plus fascinant d'Europe, ce circuit décrypte les symboles alchimiques immortalisés par Fulcanelli en 1926 dans Le Mystère des Cathédrales. Du plafond sculpté de l'oratoire de l'Hôtel Lallemant aux sculptures cabalistiques des rues médiévales et de la Porte des Étoiles, découvrez la transmutation des métaux, le lion céleste et les secrets jalousement gardés par la confrérie des frères Lallemant.",
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/25/H%C3%B4tel_Lallemant_Bourges_facade.jpg/800px-H%C3%B4tel_Lallemant_Bourges_facade.jpg"
                ),
                // 4. PAYANT (9 €)
                TourRoute(
                    id = 4,
                    nameFr = "Sabbats, Sorcellerie & Meneurs de Loups",
                    nameEn = "Witches' Sabbaths & Berry Werewolves",
                    nameDe = "Hexensabbate & Wolfsbändiger von Berry",
                    nameEs = "Aquelarres, Brujería y Domadores de Lobos",
                    nameNl = "Heksensabbats & Wolventemmers van Berry",
                    descriptionFr = "Plongez dans les croyances occultes du Berry : le carrefour des quatre vents de la Place Gordaine, la maison de la sorcière rue Mirebeau et la Halle au Blé.",
                    descriptionEn = "Immerse in Berry occult folklore: Place Gordaine crossroads, Rue Mirebeau witch's abode, and historic grain vaults.",
                    descriptionDe = "Tauchen Sie ein in die Hexensagen von Berry: Place Gordaine, das Hexenhaus in der Rue Mirebeau und alte Sagen.",
                    descriptionEs = "Sumérjase en el folclore oculto: la Plaza Gordaine, la casa de la bruja de la calle Mirebeau y antiguos ritos.",
                    descriptionNl = "Duik in de heksenverhalen van Berry: Place Gordaine, het heksenhuis in de Rue Mirebeau en occulte sagen.",
                    siteIds = listOf(12, 16, 14, 5),
                    colorHex = "#7B1FA2",
                    durationMin = 55,
                    isPaid = true,
                    isPurchased = false,
                    price = 9.0,
                    articleTitle = "La Terre des Sorciers, des Guérisseuses et des Procès d'Inquisition",
                    articleContent = "Le Berry a longtemps porté la réputation sulfureuse de 'terre des sorciers'. Ce parcours explore les carrefours où se tenaient les sabbats nocturnes, la mystérieuse maison de la guérisseuse de la rue Mirebeau qui échappa mystérieusement au bûcher en 1605, et les histoires de meneurs de loups et rebouteux immortalisés par George Sand. Une immersion envoûtante dans le folklore occulte berruyer.",
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Bourges_Place_Gordaine.jpg/800px-Bourges_Place_Gordaine.jpg"
                ),
                // 5. PAYANT (9 €)
                TourRoute(
                    id = 5,
                    nameFr = "L'Ombre des Templiers & les Ordres Secrets",
                    nameEn = "Shadow of the Templars & Secret Orders",
                    nameDe = "Der Schatten der Templer & Geheime Orden",
                    nameEs = "La Sombra de los Templarios y Órdenes Secretas",
                    nameNl = "Schaduw van de Tempeliers & Geheime Ordes",
                    descriptionFr = "Enquêtez sur la tragédie du vendredi 13 octobre 1307 : Prieuré Saint-Martin, Église Notre-Dame au Puits du Griffon et la Grange des Dîmes.",
                    descriptionEn = "Investigate the tragedy of October 13, 1307: Saint-Martin Priory, Griffin Well at Notre-Dame, and fortified tithe barn.",
                    descriptionDe = "Erforschen Sie die Templerspuren von 1307: Priorat Saint-Martin, Notre-Dame und die Zehntscheune.",
                    descriptionEs = "Investigue la tragedia templaria de 1307: Priorato de Saint-Martin, Pozo del Grifo y la Granja de Diezmos.",
                    descriptionNl = "Onderzoek de tempeliersgeheimen van 1307: Saint-Martin Priorij, Griffioenput en de Tiendschuur.",
                    siteIds = listOf(17, 9, 18, 8),
                    colorHex = "#C2185B",
                    durationMin = 60,
                    isPaid = true,
                    isPurchased = false,
                    price = 9.0,
                    articleTitle = "La Malédiction de 1307 et l'Évacuation du Trésor du Temple",
                    articleContent = "Au cœur du Berry, les chevaliers du Temple possédaient un puissant réseau de commanderies. Lors du coup de filet fatal ordonné le 13 octobre 1307 par Philippe le Bel, les Templiers de Bourges auraient disparu dans les boyaux souterrains avec leurs parchemins secrets et reliques inestimables. Partez sur les traces des croix pattées gravées dans la pierre et des portes de fer scellées dans le calcaire.",
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Prieure_Saint_Martin_Bourges.jpg/800px-Prieure_Saint_Martin_Bourges.jpg"
                ),
                // 6. PAYANT (9 €)
                TourRoute(
                    id = 6,
                    nameFr = "Le Bourges Souterrain & Carrières d'Avaricum",
                    nameEn = "Underground Bourges & Quarries of Avaricum",
                    nameDe = "Unterirdisches Bourges & Steinbrüche von Avaricum",
                    nameEs = "Bourges Subterráneo y Canteras de Avaricum",
                    nameNl = "Ondergronds Bourges & Groeven van Avaricum",
                    descriptionFr = "Pénétrez dans les entrailles de la cité : le Château d'Eau néoclassique, la crypte géante, les remparts du IVe siècle et les galeries militaires Célestines.",
                    descriptionEn = "Venture into subterranean depths: Water Tower rotunda, colossal Gothic crypt, 4th-century fortress ramparts, and military tunnels.",
                    descriptionDe = "Erkunden Sie die Tiefen: Wasserturm, Katakomben, römische Stadtmauer und alte Militärstollen.",
                    descriptionEs = "Descubra las entrañas de Bourges: Rotonda del depósito, cripta gótica, murallas romanas y galerías militares.",
                    descriptionNl = "Verken de ondergrondse diepten: Watertoren, gigantische crypte, Romeinse wallen en militaire galerijen.",
                    siteIds = listOf(15, 7, 8, 18),
                    colorHex = "#455A64",
                    durationMin = 70,
                    isPaid = true,
                    isPurchased = false,
                    price = 9.0,
                    articleTitle = "Le Labyrinthe des 40 Kilomètres de Cavités et Galeries Antiques",
                    articleContent = "Bourges repose sur un invraisemblable gruyère souterrain creusé depuis l'Antiquité romaine pour extraire la pierre calcaire. Des carrières de la garnison romaine aux boyaux militaires utilisés par les tireurs de Charles VII et les imprimeurs clandestins de la Résistance en 1943, ce parcours dévoile les accès méconnus aux entrailles de la colline sacrée.",
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/Chateau_eau_Bourges.jpg/800px-Chateau_eau_Bourges.jpg"
                ),
                // 7. PAYANT (9 €)
                TourRoute(
                    id = 7,
                    nameFr = "Renaissance Flamboyante, Arts & Officines",
                    nameEn = "Flamboyant Renaissance, Arts & Apothecaries",
                    nameDe = "Prachtvolle Renaissance, Kunst & Apotheken",
                    nameEs = "Renacimiento Flamígero, Artes y Boticas",
                    nameNl = "Flamboyante Renaissance, Kunst & Apotheken",
                    descriptionFr = "L'âge d'or des humanistes et maîtres d'art : Hôtel Cujas (Musée du Berry), rue Bourbonnoux aux Trois Flacons, Tour des Échevins et Hôtel Lallemant.",
                    descriptionEn = "Golden era of humanists and glassmakers: Hôtel Cujas, Rue Bourbonnoux timber homes, Échevins Tower, and Hôtel Lallemant.",
                    descriptionDe = "Die Blütezeit des Humanismus: Hôtel Cujas, Rue Bourbonnoux, Tour des Échevins und Hôtel Lallemant.",
                    descriptionEs = "La época dorada de los humanistas: Hôtel Cujas, calle Bourbonnoux, Torre de los Échevins y Hôtel Lallemant.",
                    descriptionNl = "De gouden eeuw van humanisten: Hôtel Cujas, Rue Bourbonnoux, Tour des Échevins en Hôtel Lallemant.",
                    siteIds = listOf(6, 11, 10, 3),
                    colorHex = "#E65100",
                    durationMin = 50,
                    isPaid = true,
                    isPurchased = false,
                    price = 9.0,
                    articleTitle = "Le Siècle des Humanistes, des Peintres et des Maîtres-Verriers",
                    articleContent = "Après le grand incendie de la Madeleine de 1487, Bourges s'est métamorphosée en foyer d'humanisme et d'architecture d'avant-garde. Ce parcours vous emmène chez les grands juristes de l'université comme Jacques Cujas, auprès des maîtres-verriers qui élaboraient le rouge pourpre alchimique, et le long des spectaculaires façades en pans de bois de la rue Bourbonnoux.",
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/H%C3%B4tel_Cujas_Bourges.jpg/800px-H%C3%B4tel_Cujas_Bourges.jpg"
                ),
                // 8. PAYANT (9 €)
                TourRoute(
                    id = 8,
                    nameFr = "Secrets Stellaires & L'Alignement Solsticial",
                    nameEn = "Stellar Secrets & Solstice Alignment",
                    nameDe = "Sternengeheimnisse & Sonnwend-Ausrichtung",
                    nameEs = "Secretos Estelares y Alineación Solsticial",
                    nameNl = "Stellaire Geheimen & Zonnewende Uitlijning",
                    descriptionFr = "Comprenez la géométrie sacrée cosmique de Bourges : Horloge Astronomique de 1424, Cathédrale, Porte des Étoiles et méridien druidique de l'Archevêché.",
                    descriptionEn = "Unveil Bourges' celestial layout: 1424 Astronomical Clock, Saint-Étienne Cathedral, Gate of Stars, and the sacred meridian axis.",
                    descriptionDe = "Entschlüsseln Sie die kosmische Geometrie: Astronomische Uhr, Kathedrale, Sternentor und der geheime Meridian.",
                    descriptionEs = "Descubra la geometría cósmica sagrada: Reloj Astronómico, Catedral, Puerta de las Estrellas y meridiano secreto.",
                    descriptionNl = "Begrijp de kosmische geometrie: Astronomische Klok, Kathedraal, Sterrenpoort en de heilige meridiaan.",
                    siteIds = listOf(4, 1, 20, 13),
                    colorHex = "#0D47A1",
                    durationMin = 45,
                    isPaid = true,
                    isPurchased = false,
                    price = 9.0,
                    articleTitle = "L'Harmonie des Sphères et la Géométrie Céleste des Bâtisseurs",
                    articleContent = "Pourquoi l'archevêché et la cathédrale sont-ils alignés sur l'axe exact du solstice d'hiver ? Comment le chanoine Fusoris a-t-il calculé en 1424 une horloge avec un décalage d'une seule seconde par siècle et demi ? Ce parcours astronomique et poétique déchiffre les cartes du ciel gravées dans la pierre berruyère et vous fait vivre l'expérience cosmique des bâtisseurs du Moyen Âge.",
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Bourges_horloge_astronomique_01.jpg/800px-Bourges_horloge_astronomique_01.jpg"
                )
            )

            for (route in presetRoutes) {
                routeDao.insertRoute(route)
            }

            // Seed sites from assets CSV file using CsvManager
            try {
                val csvContent = context.assets.open("poi_export.csv").use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                }
                val sitesToInsert = CsvManager.parseSitesFromCsv(csvContent)
                if (sitesToInsert.isNotEmpty()) {
                    siteDao.insertSites(sitesToInsert)
                }
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Error seeding database from CSV", e)
            }
        }
    }
}
