package com.example.audio

import com.example.data.Site

/**
 * Service providing high-quality narrated audio guides and titles in:
 * - Français (FR)
 * - English (EN)
 * - Español (ES)
 * - Deutsch (DE)
 * - Nederlands (NL)
 * - Italiano (IT)
 */
object MultilingualNarrationService {

    fun getTitle(site: Site, langCode: String): String {
        val code = langCode.uppercase()
        val explicit = when (code) {
            "FR" -> site.titleFr.ifBlank { site.title }
            "EN" -> site.titleEn
            "DE" -> site.titleDe
            "ES" -> site.titleEs
            "NL" -> site.titleNl
            "IT" -> site.titleIt
            else -> site.title
        }
        if (explicit.isNotBlank()) return explicit

        return when (code) {
            "IT" -> translateTitleToItalian(site.title)
            "ES" -> translateTitleToSpanish(site.title)
            "DE" -> translateTitleToGerman(site.title)
            "NL" -> translateTitleToDutch(site.title)
            "EN" -> site.titleEn.ifBlank { site.title }
            else -> site.title
        }
    }

    fun getDescription(site: Site, langCode: String): String {
        val code = langCode.uppercase()
        val explicit = when (code) {
            "FR" -> site.descriptionFr.ifBlank { site.description }
            "EN" -> site.descriptionEn
            "DE" -> site.descriptionDe
            "ES" -> site.descriptionEs
            "NL" -> site.descriptionNl
            "IT" -> site.descriptionIt
            else -> site.description
        }
        if (explicit.isNotBlank()) return explicit

        val title = getTitle(site, code)
        return when (code) {
            "IT" -> "Scopri $title a Bourges. Un capolavoro di storia, arte e patrimonio nel cuore della Francia."
            "ES" -> "Descubra $title en Bourges. Una joya de historia, arte y patrimonio en el corazón de Francia."
            "DE" -> "Entdecken Sie $title in Bourges. Ein Juwel der Geschichte, Kunst und Kultur im Herzen Frankreichs."
            "NL" -> "Ontdek $title in Bourges. Een parel van geschiedenis, kunst en erfgoed in het hart van Frankrijk."
            "EN" -> "Discover $title in Bourges. A jewel of history, architecture, and heritage in central France."
            else -> site.description
        }
    }

    fun getNarration(site: Site, langCode: String): String {
        val code = langCode.uppercase()

        // 1. Check if an explicit non-blank translation exists in the site entity
        val explicit = when (code) {
            "FR" -> site.narrationFr
            "EN" -> site.narrationEn
            "DE" -> site.narrationDe
            "ES" -> site.narrationEs
            "NL" -> site.narrationNl
            "IT" -> site.narrationIt
            else -> ""
        }
        if (explicit.isNotBlank() && (code == "FR" || explicit != site.narrationFr)) {
            return explicit
        }

        // 2. Check for curated high-fidelity narrations for Bourges landmark sites
        val landmarkNarration = getPresetLandmarkNarration(site.id, code)
        if (landmarkNarration != null) {
            return landmarkNarration
        }

        // 3. Fallback to English if requested
        if (code == "EN" && site.narrationEn.isNotBlank()) {
            return site.narrationEn
        }

        // 4. Synthesize localized narration based on the target language and site info
        val title = getTitle(site, code)
        val baseDescription = site.descriptionFr.ifBlank { site.description }

        return when (code) {
            "IT" -> "Benvenuti a $title. Questo magnifico sito di Bourges custodisce secoli di ricca memoria e affascinanti segreti nel cuore del Berry. $baseDescription. Ammirate i dettagli storici e l'architettura circostante, testimoni del glorioso passato della capitale storica."
            "ES" -> "Bienvenidos a $title. Este emblemático enclave de Bourges encierra siglos de historia y misterio en el corazón de Berry. $baseDescription. Descubra los singulares detalles arquitectónicos que hacen de este lugar una visita imprescindible en la ciudad."
            "DE" -> "Willkommen in $title. Diese bedeutende Sehenswürdigkeit in Bourges birgt jahrhundertealte Geschichte und Geheimnisse im Herzen des Berry. $baseDescription. Bewundern Sie die architektonischen Feinheiten und den historischen Charme dieses geschichtsträchtigen Ortes."
            "NL" -> "Welkom bij $title. Deze opmerkelijke locatie in Bourges herbergt eeuwenoude geschiedenis en fascinerende verhalen in het hart van Berry. $baseDescription. Neem de tijd om de architectuur en het erfgoed van deze historische stad te bewonderen."
            "EN" -> "Welcome to $title. This remarkable site in Bourges holds centuries of rich history and intriguing heritage in the heart of Berry. $baseDescription. Take in the unique architectural details and timeless charm of this historic French landmark."
            else -> site.narrationFr.ifBlank { site.narrationText.ifBlank { site.description } }
        }
    }

    private fun getPresetLandmarkNarration(siteId: Int, langCode: String): String? {
        return when (siteId) {
            1 -> when (langCode) { // Cathédrale Saint-Étienne
                "IT" -> "Benvenuti davanti alla maestosa Cattedrale di Saint-Étienne di Bourges, capolavoro assoluto dell'arte gotica del dodicesimo secolo, dichiarata patrimonio mondiale dell'UNESCO. Ammirate la grandiosa facciata occidentale con i suoi cinque portali riccamente scolpiti e l'assenza di transetto, che crea uno slancio visivo di straordinaria armonia e luminosità. Le sue vetrate duecentesche e il Giudizio Universale sono celebri in tutto il mondo."
                "ES" -> "Bienvenidos ante la majestuosa Catedral de Saint-Étienne de Bourges, obra maestra indiscutible del arte gótico del siglo doce, declarada Patrimonio de la Humanidad por la UNESCO. Admire su fachada occidental con cinco pórticos esculpidos y su nave continua sin transepto, que crea una luminosidad y armonía espacial únicas. Sus vidrieras medievales y el tímpano del Juicio Final son reconocidos mundialmente."
                "DE" -> "Willkommen vor der majestätischen Kathedrale Saint-Étienne von Bourges, einem Meisterwerk der Hochgotik des zwölften Jahrhunderts und UNESCO-Weltkulturerbe. Bestaunen Sie die fünf monumentalen Portale der Westfassade und das durchgehende fünfschiffige Langhaus ohne Querschiff, das ein unvergleichliches Lichtspiel erzeugt. Die bunten Glasfenster aus dem dreizehnten Jahrhundert zählen zu den schönsten Frankreichs."
                "NL" -> "Welkom bij de majestueuze Kathedraal Saint-Étienne van Bourges, een gotisch meesterwerk uit de twaalfde eeuw en UNESCO-werelderfgoed. Bewonder de indrukwekkende westgevel met vijf rijk gebeeldhouwde portalen en het schip zonder dwarsschip, wat zorgt voor een ongeëvenaarde lichtinval en harmonie. De middeleeuwse glas-in-loodramen behoren tot de absolute top van Europa."
                else -> null
            }
            2 -> when (langCode) { // Palais Jacques Cœur
                "IT" -> "Benvenuti al Palazzo Jacques Cœur, la più fastosa residenza signorile in stile gotico fiammeggiante di Francia. Edificato nel quindicesimo secolo per il Grande Argentiere del re Carlo Settimo, l'edificio unisce lusso e simbolismo ermetico. Lungo la facciata e i camini si ammirano cuori scolpiti, conchiglie di San Giacomo e figure allegoriche che la leggenda associa alla scoperta della Pietra Filosofale."
                "ES" -> "Bienvenidos al Palacio Jacques Cœur, el palacio civil gótico más suntuoso de Francia. Erigido en el siglo quince para el Gran Banquero del rey Carlos Séptimo, este edificio refleja el poder y misterio de su dueño. En su fachada y salones destacan corazones esculpidos y conchas de Santiago, acompañados de leyendas que atribuyen su fortuna a la piedra filosofal."
                "DE" -> "Willkommen im Palast Jacques Cœur, dem prächtigsten gotischen Bürgerpalast Frankreichs. Erbaut im fünfzehnten Jahrhundert für den königlichen Großfinanzier Karls des Siebten, vereint das Anwesen Pracht und Alchemie. An den Fassaden und Kaminen entdecken Sie geschnitzte Herzen, Jakobsmuscheln und rätselhafte Symbole, die bis heute mit dem Stein der Weisen verknüpft werden."
                "NL" -> "Welkom bij het Paleis Jacques Cœur, het rijkste gotische burgerpaleis van Frankrijk. Gebouwd in de vijftiende eeuw voor de invloedrijke schatmeester van koning Karel de Zevende. Bewonder de rijk gebeeldhouwde gevels, binnenplaatsen en alchemistische motieven van harten en sint-jakobsschelpen."
                else -> null
            }
            3 -> when (langCode) { // Hôtel Lallemant
                "IT" -> "Benvenuti all'Hôtel Lallemant, autentico gioiello del primo Rinascimento francese e tempio della tradizione alchemica occidentale. Nel suo oratorio privato si ammira il celebre soffitto a trenta cassettoni in pietra, decifrato dall'alchimista Fulcanelli come un trattato completo della Grande Opera Ermetica."
                "ES" -> "Bienvenidos al Hôtel Lallemant, joya del primer Renacimiento francés y santuario esotérico consagrado a la alquimia. En su oratorio privado se encuentra el famoso techo con treinta casetones esculpidos, descifrado por Fulcanelli en El Misterio de las Catedrales como las etapas de la Gran Obra."
                "DE" -> "Willkommen im Hôtel Lallemant, einem Juwel der französischen Frührenaissance und Zentrum europäischer Alchemieforschung. Im privaten Oratorium verbirgt sich die Decke mit dreißig steinernen Kassetten, die als Schritt-für-Schritt-Anleitung zur Erlangung des Steins der Weisen gedeutet werden."
                "NL" -> "Welkom bij Hôtel Lallemant, een meesterwerk van de vroege renaissance en een heiligdom voor alchemisten. In de privékapel bevindt zich het beroemde plafond met dertig gebeeldhouwde panelen, door kenners beschouwd als een handboek voor het Grote Werk."
                else -> null
            }
            4 -> when (langCode) { // Horloge Astronomique
                "IT" -> "Ammirate l'Orologio Astronomico di Bourges, concepito nel 1424 da Jean Fusoris su ordine del re Carlo Settimo. È il più antico orologio astronomico conservato di Francia, capace di indicare le posizioni del Sole, le fasi della Luna e le costellazioni dello Zodiaco con eccezionale precisione."
                "ES" -> "Contemplen el Reloj Astronómico de Bourges, creado en 1424 por Jean Fusoris por encargo de Carlos Séptimo. Es el reloj astronómico más antiguo preservado en Francia, capaz de calcular las fases lunares, la posición solar y las constelaciones zodiacales con precisión milimétrica."
                "DE" -> "Blicken Sie auf die Astronomische Uhr von Bourges aus dem Jahr 1424, gefertigt von Jean Fusoris für König Karl den Siebten. Als älteste erhaltene astronomische Uhr Frankreichs zeigt sie Sonnenstand, Mondphasen und Tierkreiszeichen mit verblüffender Genauigkeit an."
                "NL" -> "Bewonder het Astronomisch Uurwerk van Bourges, ontworpen in 1424 door Jean Fusoris in opdracht van koning Karel de Zevende. Dit oudste bewaarde astronomische uurwerk van Frankrijk toont maanfasen, dierenriemtekens en de zonnecyclus."
                else -> null
            }
            5 -> when (langCode) { // Marais de Bourges
                "IT" -> "Respirate la serenità delle Paludi di Bourges, un polmone verde di centotrentacinque ettari composto da canali e orti fioriti ai piedi della cattedrale medievale. Antica barriera difensiva gallica di Avaricum, è oggi un'oasi protetta di straordinaria bellezza naturale."
                "ES" -> "Disfrute de la paz de los Pantanos de Bourges, ciento treinta y cinco hectáreas de canales y huertos fértiles al pie del centro histórico. Antaño foso natural de defensa de Avaricum, hoy es un remanso verde protegido de gran biodiversidad."
                "DE" -> "Genießen Sie die Idylle der Marais de Bourges, einer einzigartigen Oase von einhundertfünfunddreißig Hektar aus Kanälen und Nutzgärten. Früher dienten die Sümpfe dem Schutz von Avaricum, heute sind sie ein geschütztes Paradies für Flora und Fauna."
                "NL" -> "Ervaar de rust van de Marais van Bourges, een uitgestrekt netwerk van 135 hectare aan waterkanalen en volkstuinen. Vroeger een natuurlijke verdedigingsgordel van Avaricum, nu een beschermd groen paradijs."
                else -> null
            }
            6 -> when (langCode) { // Hôtel Cujas
                "IT" -> "Benvenuti all'Hôtel Cujas, capolavoro rinascimentale in mattoni rossi e pietra calcarea eretto nel 1515, oggi sede del Museo del Berry. Prende il nome dal celebre giurista umanista Jacques Cujas."
                "ES" -> "Bienvenidos al Hôtel Cujas, palacio renacentista de ladrillo rojo y piedra labrada edificado hacia 1515, hoy Museo del Berry. Debe su nombre al insigne jurisconsulto renacentista Jacques Cujas."
                "DE" -> "Willkommen im Hôtel Cujas, einem Prachtbau der Renaissance aus rotem Backstein und Kalkstein von 1515, heute Museum des Berry. Benannt nach dem berühmten Humanisten und Juristen Jacques Cujas."
                "NL" -> "Welkom bij Hôtel Cujas, een renaissancepaleis uit 1515 van rode baksteen en kalksteen, nu het Musée du Berry. Vernoemd naar de beroemde humanist Jacques Cujas."
                else -> null
            }
            7 -> when (langCode) { // Crypte
                "IT" -> "Scendete nella monumentale Cripta della Cattedrale di Bourges, la più vasta chiesa inferiore gotica di Francia. Questo capolavoro ipogeo del tredicesimo secolo custodisce il celebre gisant policromo del Duca Jean de Berry."
                "ES" -> "Descienda a la colosal Cripta de la Catedral de Bourges, la iglesia baja gótica más extensa de Francia, construida para salvar el desnivel del foso romano. Alberga la estatua yacente del Duque Jean de Berry."
                "DE" -> "Steigen Sie hinab in die gewaltige Krypta der Kathedrale von Bourges, die größte gotische Unterkirche Frankreichs. Sie birgt das berühmte marmorne Grabmal des Herzogs Jean de Berry."
                "NL" -> "Daal af in de monumentale crypte van de kathedraal, de grootste gotische benedenkerk van Frankrijk. Hier rust het praalgraf van hertog Jean de Berry."
                else -> null
            }
            8 -> when (langCode) { // Remparts
                "IT" -> "Ammirate i possenti Bastioni Gallo-Romani del quarto secolo. Con blocchi monumentali scolpiti tratti dai templi di Avaricum, difesero la città contro le invasioni barbariche per oltre un millennio."
                "ES" -> "Observe las imponentes Murallas Galo-Romanas del siglo cuarto. Construidas con sillares monumentales para defender Avaricum, estas fortificaciones resistieron durante más de mil años."
                "DE" -> "Bestaunen Sie die gallo-römische Stadtmauer aus dem vierten Jahrhundert. Erbaut zum Schutz von Avaricum, zeugen die massiven Quadersteine von der antiken Wehrkraft der Stadt."
                "NL" -> "Bewonder de imposante Gallo-Romeinse stadsmuren uit de vierde eeuw. Gebouwd ter verdediging van Avaricum, getuigen ze van het roemruchte antieke verleden."
                else -> null
            }
            9 -> when (langCode) { // Église Notre-Dame
                "IT" -> "Benvenuti alla Chiesa di Notre-Dame e al misterioso Pozzo del Grifone. Ricostruita dopo il devastante incendio del 1487, mescola gotico fiammeggiante e primo rinascimento."
                "ES" -> "Bienvenidos a la Iglesia de Notre-Dame y al legendario Pozo del Grifo. Reconstruida tras el gran incendio de 1487, combina el gótico flamígero con los primeros aires renacentistas."
                "DE" -> "Willkommen in der Kirche Notre-Dame und beim Greifenbrunnen. Nach dem Großbrand von 1487 wiederaufgebaut, verbindet sie Spätgotik und frühe Renaissance."
                "NL" -> "Welkom bij de Notre-Dame kerk en de Griffioenput. Herbouwd na de grote stadsbrand van 1487, combineert zij flamboyante gotiek en renaissance."
                else -> null
            }
            10 -> when (langCode) { // Tour des Échevins
                "IT" -> "Ammirate la Tour des Échevins, elegante torre ottagonale del quindicesimo secolo che ospitava il primo municipio di Bourges, oggi consacrata al celebre pittore Maurice Estève."
                "ES" -> "Contemple la Torre de los Échevins, esbelta torre octogonal del siglo quince que albergó el primer ayuntamiento de la ciudad, hoy dedicada al pintor abstracto Maurice Estève."
                "DE" -> "Betrachten Sie den Tour des Échevins, einen achteckigen Renaissanceturm des fünfzehnten Jahrhunderts, einst Sitz der Stadtväter und heute Museum für Maurice Estève."
                "NL" -> "Bewonder de Tour des Échevins, een sierlijke achthoekige toren uit de vijftiende eeuw en voormalig stadhuis, nu museum gewijd aan Maurice Estève."
                else -> null
            }
            else -> null
        }
    }

    private fun translateTitleToItalian(frenchTitle: String): String {
        return frenchTitle
            .replace("Cathédrale", "Cattedrale")
            .replace("Palais", "Palazzo")
            .replace("Hôtel", "Palazzo")
            .replace("Église", "Chiesa")
            .replace("Crypte", "Cripta")
            .replace("Marais", "Paludi")
            .replace("Remparts", "Bastioni")
            .replace("Jardin", "Giardino")
            .replace("Tour", "Torre")
            .replace("Rue", "Via")
            .replace("Place", "Piazza")
    }

    private fun translateTitleToSpanish(frenchTitle: String): String {
        return frenchTitle
            .replace("Cathédrale", "Catedral")
            .replace("Palais", "Palacio")
            .replace("Hôtel", "Mansión")
            .replace("Église", "Iglesia")
            .replace("Crypte", "Cripta")
            .replace("Marais", "Pantanos")
            .replace("Remparts", "Murallas")
            .replace("Jardin", "Jardín")
            .replace("Tour", "Torre")
            .replace("Rue", "Calle")
            .replace("Place", "Plaza")
    }

    private fun translateTitleToGerman(frenchTitle: String): String {
        return frenchTitle
            .replace("Cathédrale", "Kathedrale")
            .replace("Palais", "Palast")
            .replace("Église", "Kirche")
            .replace("Crypte", "Krypta")
            .replace("Marais", "Sümpfe")
            .replace("Remparts", "Stadtmauer")
            .replace("Jardin", "Garten")
            .replace("Tour", "Turm")
            .replace("Rue", "Straße")
            .replace("Place", "Platz")
    }

    private fun translateTitleToDutch(frenchTitle: String): String {
        return frenchTitle
            .replace("Cathédrale", "Kathedraal")
            .replace("Palais", "Paleis")
            .replace("Église", "Kerk")
            .replace("Crypte", "Crypte")
            .replace("Marais", "Moerassen")
            .replace("Remparts", "Stadsmuren")
            .replace("Jardin", "Tuin")
            .replace("Tour", "Toren")
            .replace("Rue", "Straat")
            .replace("Place", "Plein")
    }
}
