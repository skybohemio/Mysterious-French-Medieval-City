package com.example.ui.screens

object Locales {
    private val translations = mapOf(
        "welcome" to mapOf(
            "FR" to "Bienvenue à Bourges",
            "EN" to "Welcome to Bourges",
            "DE" to "Willkommen in Bourges",
            "ES" to "Bienvenido a Bourges",
            "NL" to "Welkom in Bourges"
        ),
        "subtitle" to mapOf(
            "FR" to "Explorez les secrets médiévaux de l'ancienne capitale du Berry",
            "EN" to "Explore the medieval secrets of the ancient capital of Berry",
            "DE" to "Entdecken Sie die mittelalterlichen Geheimnisse der alten Hauptstadt des Berry",
            "ES" to "Explora los secretos medievales de la antigua capital del Berry",
            "NL" to "Ontdek de middeleeuwse geheimen van de oude hoofdstad van Berry"
        ),
        "active_route" to mapOf(
            "FR" to "Parcours Actif",
            "EN" to "Active Route",
            "DE" to "Aktive Route",
            "ES" to "Ruta Activa",
            "NL" to "Actieve Route"
        ),
        "showing_paths" to mapOf(
            "FR" to "Affichage des trajets sur la carte interactive",
            "EN" to "Showing paths on the interactive map",
            "DE" to "Pfade auf der interaktiven Karte anzeigen",
            "ES" to "Mostrando trayectos en el mapa interactivo",
            "NL" to "Paden weergeven op de interactieve kaart"
        ),
        "remove_route" to mapOf(
            "FR" to "Quitter le parcours",
            "EN" to "Remove route",
            "DE" to "Route entfernen",
            "ES" to "Quitar ruta",
            "NL" to "Route verwijderen"
        ),
        "map_prompt" to mapOf(
            "FR" to "* Appuyez sur les points d'intérêt sur la carte pour centrer et lancer l'audioguide.",
            "EN" to "* Click on the points of interest on the map to center and start the audio guide.",
            "DE" to "* Klicken Sie auf die Sehenswürdigkeiten auf der Karte, um sie zu zentrieren und den Audioguide zu starten.",
            "ES" to "* Pulsa sobre los puntos de interés en el mapa para centrar e iniciar la audioguía.",
            "NL" to "* Klik op de bezienswaardigheden op de kaart om te centreren en de audiogids te starten."
        ),
        "search_placeholder" to mapOf(
            "FR" to "Rechercher des attractions ou des histoires...",
            "EN" to "Search attractions or stories...",
            "DE" to "Suchen Sie nach Attraktionen oder Geschichten...",
            "ES" to "Buscar atracciones o historias...",
            "NL" to "Zoek attracties of verhalen..."
        ),
        "header_poi" to mapOf(
            "FR" to "POINTS D'INTÉRÊT À BOURGES",
            "EN" to "POINTS OF INTEREST IN BOURGES",
            "DE" to "SEHENSWÜRDIGKEITEN IN BOURGES",
            "ES" to "PUNTOS DE INTERÉS EN BOURGES",
            "NL" to "BEZIENSWAARDIGHEDEN IN BOURGES"
        ),
        "coords" to mapOf(
            "FR" to "Coordonnées",
            "EN" to "Coordinates",
            "DE" to "Koordinaten",
            "ES" to "Coordenadas",
            "NL" to "Coördinaten"
        ),
        "stop_voice" to mapOf(
            "FR" to "Arrêter la voix",
            "EN" to "Stop Voice",
            "DE" to "Stimme anhalten",
            "ES" to "Detener Voz",
            "NL" to "Stem stoppen"
        ),
        "start_audio" to mapOf(
            "FR" to "Lancer l'audioguide",
            "EN" to "Start Audio Guide",
            "DE" to "Audioguide starten",
            "ES" to "Iniciar Audioguía",
            "NL" to "Audiogids starten"
        ),
        "no_attractions" to mapOf(
            "FR" to "Aucune attraction trouvée pour votre recherche.",
            "EN" to "No attractions found for your search.",
            "DE" to "Keine Attraktionen für Ihre Suche gefunden.",
            "ES" to "No se encontraron atracciones para tu búsqueda.",
            "NL" to "Geen attracties gevonden voor je zoekopdracht."
        ),
        "routes_header" to mapOf(
            "FR" to "PARCOURS THÉMATIQUES",
            "EN" to "THEMATIC ROUTES",
            "DE" to "THEMATISCHE ROUTEN",
            "ES" to "RECORRIDOS TEMÁTICOS",
            "NL" to "THEMATISCHE ROUTES"
        ),
        "routes_desc" to mapOf(
            "FR" to "Visites guidées par des experts pour vous immerger dans le patrimoine de Bourges. Sélectionnez un parcours pour le projeter directement sur la carte.",
            "EN" to "Expert-guided tours to immerse yourself in Bourges' heritage. Select a route to project it directly on the guide map.",
            "DE" to "Von Experten geführte Touren, um in das Erbe von Bourges einzutauchen. Wählen Sie eine Route aus, um sie direkt auf die Karte zu projizieren.",
            "ES" to "Rutas guiadas por expertos para sumergirte en el patrimonio de Bourges. Selecciona una ruta para proyectarla directamente sobre el mapa guía.",
            "NL" to "Tours onder leiding van experts om je onder te dompelen in het erfgoed van Bourges. Selecteer een route om deze rechtstreeks op de kaart te projecteren."
        ),
        "route_stops" to mapOf(
            "FR" to "Arrêts sur ce parcours :",
            "EN" to "Stops on this itinerary:",
            "DE" to "Haltestellen auf dieser Route:",
            "ES" to "Paradas en este itinerario:",
            "NL" to "Stoppen op deze route:"
        ),
        "map_view" to mapOf(
            "FR" to "Voir sur la carte",
            "EN" to "View on Map",
            "DE" to "Auf der Karte anzeigen",
            "ES" to "Ver en el Mapa Guía",
            "NL" to "Op de kaart bekijken"
        ),
        "trace_route" to mapOf(
            "FR" to "Suivre le parcours",
            "EN" to "Trace Route on Map",
            "DE" to "Route auf Karte zeichnen",
            "ES" to "Trazar Ruta en Mapa",
            "NL" to "Route op de kaart tekenen"
        ),
        "admin_header" to mapOf(
            "FR" to "SYSTÈME D'ADMINISTRATION DES SITES",
            "EN" to "SITE ADMINISTRATION SYSTEM",
            "DE" to "VERWALTUNGSSYSTEM FÜR SEHENSWÜRDIGKEITEN",
            "ES" to "SISTEMA ADMIN DE SITIOS",
            "NL" to "BEHEERSYSTEEM VOOR BEZIENSWAARDIGHEDEN"
        ),
        "admin_desc" to mapOf(
            "FR" to "Formulaire d'administration (Backend Local). Ajoutez de nouveaux points d'intérêt ou éditez-les directement. Vos points apparaîtront sur la carte interactive ainsi que sur la liste de découverte principale.",
            "EN" to "Admin form (Local Backend). Add new points of interest or edit them directly. Your points will appear on the interactive map and the main discover list.",
            "DE" to "Admin-Formular (Lokales Backend). Fügen Sie neue Sehenswürdigkeiten hinzu oder bearbeiten Sie sie direkt. Ihre Punkte erscheinen auf der interaktiven Karte und in der Hauptentdeckungsliste.",
            "ES" to "Formulario administrativo (Backend Local). Añade nuevos puntos de interés o edítalos directamente. Tus puntos aparecerán en la carta interactiva así como en la lista de descubrimiento principal.",
            "NL" to "Beheerdersformulier (Lokale Backend). Voeg nieuwe bezienswaardigheden toe of bewerk ze direct. Je punten verschijnen op de interactieve kaart en de hoofdlijst."
        ),
        "attr_name" to mapOf(
            "FR" to "Nom de l'attraction *",
            "EN" to "Attraction Name *",
            "DE" to "Name der Attraktion *",
            "ES" to "Nombre de la Atracción *",
            "NL" to "Naam van de attractie *"
        ),
        "attr_category" to mapOf(
            "FR" to "Catégorie du monument :",
            "EN" to "Monument Category:",
            "DE" to "Kategorie der Sehenswürdigkeit:",
            "ES" to "Categoría del Monumento:",
            "NL" to "Categorie van het monument:"
        ),
        "map_location" to mapOf(
            "FR" to "Emplacement sur le plan de Bourges :",
            "EN" to "Location on Bourges Map:",
            "DE" to "Lage auf der Karte von Bourges:",
            "ES" to "Ubicación en el Plano de Bourges:",
            "NL" to "Locatie op de kaart van Bourges:"
        ),
        "lat_label" to mapOf(
            "FR" to "Latitude Nord (GPS) :",
            "EN" to "North Latitude (GPS):",
            "DE" to "Nördliche Breite (GPS):",
            "ES" to "Latitud Norte (GPS):",
            "NL" to "Noorderbreedte (GPS):"
        ),
        "lng_label" to mapOf(
            "FR" to "Longitude Est (GPS) :",
            "EN" to "East Longitude (GPS):",
            "DE" to "Östliche Länge (GPS):",
            "ES" to "Longitud Este (GPS):",
            "NL" to "Oosterlengte (GPS):"
        ),
        "zone_centre" to mapOf(
            "FR" to "Zone Centre (Cathédrale / Palais)",
            "EN" to "Center Zone (Cathedral / Palace)",
            "DE" to "Zentrumszone (Kathedrale / Palast)",
            "ES" to "Zona Centro (Catedral / Palacio)",
            "NL" to "Centrumzone (Kathedraal / Paleis)"
        ),
        "zone_marshes" to mapOf(
            "FR" to "Zone Marais (Nord-Est)",
            "EN" to "Marshes Zone (North-East)",
            "DE" to "Sumpfgebiet (Nordost)",
            "ES" to "Zona Pantanos (Nordeste)",
            "NL" to "Moeraszone (Noordoost)"
        ),
        "short_desc" to mapOf(
            "FR" to "Description touristique courte *",
            "EN" to "Short Tourist Description *",
            "DE" to "Kurze touristische Beschreibung *",
            "ES" to "Descripción Turística Corta *",
            "NL" to "Korte toeristische beschrijving *"
        ),
        "narration_script" to mapOf(
            "FR" to "Script de narration (Texte pour TTS) *",
            "EN" to "Narration Script (TTS Text) *",
            "DE" to "Sprechertext (TTS-Text) *",
            "ES" to "Guión de Narración (Texto para TTS)*",
            "NL" to "Narratiescript (TTS-tekst) *"
        ),
        "script_placeholder" to mapOf(
            "FR" to "Écrivez le texte que la voix narrera étape par étape à l'utilisateur...",
            "EN" to "Write the text that the voice will narrate step-by-step to the user...",
            "DE" to "Schreiben Sie den Text, den die Stimme Schritt für Schritt dem Benutzer vorliest...",
            "ES" to "Escribe el texto que la voz narrará paso a paso al usuario...",
            "NL" to "Schrijf de tekst die de stem stap voor stap aan de gebruiker zal voorlezen..."
        ),
        "save_poi" to mapOf(
            "FR" to "Enregistrer le point d'intérêt",
            "EN" to "Save Point of Interest",
            "DE" to "Sehenswürdigkeit speichern",
            "ES" to "Guardar Punto de Interés",
            "NL" to "Bezienswaardigheid opslaan"
        ),
        "discover_tab" to mapOf(
            "FR" to "Découvrir",
            "EN" to "Discover",
            "DE" to "Entdecken",
            "ES" to "Descubrir",
            "NL" to "Ontdekken"
        ),
        "routes_tab" to mapOf(
            "FR" to "Parcours",
            "EN" to "Routes",
            "DE" to "Routen",
            "ES" to "Rutas",
            "NL" to "Routes"
        ),
        "admin_tab" to mapOf(
            "FR" to "Admin POI",
            "EN" to "Admin POI",
            "DE" to "Admin POI",
            "ES" to "Admin POI",
            "NL" to "Beheerder POI"
        ),
        "voice_narrator" to mapOf(
            "FR" to "NARRATEUR VOCAL",
            "EN" to "VOICE NARRATOR",
            "DE" to "STRECKENSPRECHER",
            "ES" to "NARRADOR DE VOZ",
            "NL" to "STEMNARRATOR"
        ),
        "voice_narrator_sub" to mapOf(
            "FR" to "Narrateur Vocal • Bourges Guide",
            "EN" to "Voice Narrator • Bourges Guide",
            "DE" to "Sprecher • Bourges Guide",
            "ES" to "Narrador de Voz • Bourges Guide",
            "NL" to "Stemnarrator • Bourges Guide"
        ),
        "edge_tts_badge" to mapOf(
            "FR" to "Microsoft Edge Neural TTS",
            "EN" to "Microsoft Edge Neural TTS",
            "DE" to "Microsoft Edge Neural TTS",
            "ES" to "Microsoft Edge Neural TTS",
            "NL" to "Microsoft Edge Neural TTS"
        ),
        "voice_female" to mapOf(
            "FR" to "Voix féminine",
            "EN" to "Female voice",
            "DE" to "Weibliche Stimme",
            "ES" to "Voz femenina",
            "NL" to "Vrouwelijke stem"
        ),
        "voice_male" to mapOf(
            "FR" to "Voix masculine",
            "EN" to "Male voice",
            "DE" to "Männliche Stimme",
            "ES" to "Voz masculina",
            "NL" to "Mannelijke stem"
        ),
        "map_mode_google" to mapOf(
            "FR" to "Google Maps 🗺️",
            "EN" to "Google Maps 🗺️",
            "DE" to "Google Maps 🗺️",
            "ES" to "Google Maps 🗺️",
            "NL" to "Google Maps 🗺️"
        ),
        "map_mode_artistic" to mapOf(
            "FR" to "Carte Illustrée 🎨",
            "EN" to "Illustrated Map 🎨",
            "DE" to "Illustrated Map 🎨",
            "ES" to "Mapa Ilustrado 🎨",
            "NL" to "Geïllustreerde Kaart 🎨"
        ),
        "map_type_normal" to mapOf(
            "FR" to "Plan",
            "EN" to "Map",
            "DE" to "Karte",
            "ES" to "Mapa",
            "NL" to "Kaart"
        ),
        "map_type_satellite" to mapOf(
            "FR" to "Satellite 🛰️",
            "EN" to "Satellite 🛰️",
            "DE" to "Satellit 🛰️",
            "ES" to "Satélite 🛰️",
            "NL" to "Satelliet 🛰️"
        ),
        "map_type_terrain" to mapOf(
            "FR" to "Relief ⛰️",
            "EN" to "Terrain ⛰️",
            "DE" to "Gelände ⛰️",
            "ES" to "Relieve ⛰️",
            "NL" to "Terrein ⛰️"
        ),
        "official_audio" to mapOf(
            "FR" to "Audiosite Officiel",
            "EN" to "Official Audiosite",
            "DE" to "Offizieller Audioguide",
            "ES" to "Audiositio Oficial",
            "NL" to "Officiële Audiosite"
        ),
        "local_audio" to mapOf(
            "FR" to "Collaboration Locale",
            "EN" to "Local Collaboration",
            "DE" to "Lokale Zusammenarbeit",
            "ES" to "Colaboración Local",
            "NL" to "Lokale Samenwerking"
        ),
        "gps_navigation" to mapOf(
            "FR" to "Y aller (GPS)",
            "EN" to "Navigate (GPS)",
            "DE" to "Navigieren (GPS)",
            "ES" to "Navegar (GPS)",
            "NL" to "Navigeren (GPS)"
        ),
        "admin_mode" to mapOf(
            "FR" to "Mode Administrateur",
            "EN" to "Admin Mode",
            "DE" to "Admin-Modus",
            "ES" to "Modo Administrador",
            "NL" to "Beheerdersmodus"
        ),
        "admin_pin_instruction" to mapOf(
            "FR" to "Saisissez le code d'accès de sécurité pour déverrouiller la gestion interactive des sites et des parcours :",
            "EN" to "Enter the security PIN to unlock interactive management of sites and routes:",
            "DE" to "Geben Sie die Sicherheits-PIN ein, um die interaktive Verwaltung von Sehenswürdigkeiten und Routen freizuschalten:",
            "ES" to "Introduzca el PIN de seguridad para desbloquear la gestión interactiva de sitios y rutas:",
            "NL" to "Voer de beveiligings-PIN in om interactief beheer van sites en routes te ontgrendelen:"
        ),
        "invalid_pin" to mapOf(
            "FR" to "Code PIN de sécurité incorrect.",
            "EN" to "Incorrect security PIN.",
            "DE" to "Falsche Sicherheits-PIN.",
            "ES" to "PIN de seguridad incorrecto.",
            "NL" to "Onjuiste beveiligings-PIN."
        ),
        "admin_mode_unlocked" to mapOf(
            "FR" to "Mode Administrateur déverrouillé !",
            "EN" to "Admin Mode unlocked!",
            "DE" to "Admin-Modus freigeschaltet!",
            "ES" to "¡Modo Administrador desbloqueado!",
            "NL" to "Beheerdersmodus ontgrendeld!"
        ),
        "exit_admin" to mapOf(
            "FR" to "Quitter l'Admin",
            "EN" to "Exit Admin",
            "DE" to "Admin beenden",
            "ES" to "Salir de Admin",
            "NL" to "Beheerder verlaten"
        ),
        "routes_tab_predefined" to mapOf(
            "FR" to "Parcours Thématiques",
            "EN" to "Predefined Routes",
            "DE" to "Thematische Routen",
            "ES" to "Rutas Temáticas",
            "NL" to "Thematische Routes"
        ),
        "routes_tab_planner" to mapOf(
            "FR" to "Planificateur Intelligent",
            "EN" to "Smart Planner",
            "DE" to "Intelligenter Planer",
            "ES" to "Planificador Inteligente",
            "NL" to "Slimme Planner"
        ),
        "planner_title" to mapOf(
            "FR" to "Générateur de Parcours Sur-Mesure",
            "EN" to "Custom Itinerary Generator",
            "DE" to "Maßgeschneiderter Routenplaner",
            "ES" to "Generador de Itinerario a Medida",
            "NL" to "Op Maat Gemaakte Routeplanner"
        ),
        "planner_desc" to mapOf(
            "FR" to "Optimisez votre temps à Bourges ! Saisissez le temps dont vous disposez et votre point de départ pour recevoir instantanément un parcours optimisé de marche.",
            "EN" to "Optimize your time in Bourges! Enter your available time and starting point to instantly receive an optimized walking tour.",
            "DE" to "Optimieren Sie Ihre Zeit in Bourges! Geben Sie Ihre verfügbare Zeit und Ihren Ausgangspunkt ein, um sofort eine optimierte Route zu erhalten.",
            "ES" to "¡Optimice su tiempo en Bourges! Introduzca el tiempo disponible y su punto de partida para recibir al instante una ruta optimizada.",
            "NL" to "Optimaliseer uw tijd in Bourges! Voer uw beschikbare tijd en startpunt in om direct een geoptimaliseerde route te ontvangen."
        ),
        "planner_start_point" to mapOf(
            "FR" to "Point de départ :",
            "EN" to "Starting point:",
            "DE" to "Ausgangspunkt:",
            "ES" to "Punto de partida:",
            "NL" to "Startpunt:"
        ),
        "planner_gps_option" to mapOf(
            "FR" to "Ma position actuelle (GPS)",
            "EN" to "My current location (GPS)",
            "DE" to "Mein aktueller Standort (GPS)",
            "ES" to "Mi ubicación actual (GPS)",
            "NL" to "Mijn huidige locatie (GPS)"
        ),
        "planner_btn_generate" to mapOf(
            "FR" to "Suggérer le meilleur parcours",
            "EN" to "Suggest optimal route",
            "DE" to "Optimale Route vorschlagen",
            "ES" to "Sugerir ruta óptima",
            "NL" to "Optimale route voorstellen"
        ),
        "planner_no_result" to mapOf(
            "FR" to "Aucun point d'intérêt ne peut être visité dans ce délai depuis ce point de départ. Essayez d'augmenter le temps !",
            "EN" to "No points of interest can be visited within this timeframe from this starting point. Try increasing the time!",
            "DE" to "Innerhalb dieses Zeitrahmens können von diesem Ausgangspunkt aus keine Sehenswürdigkeiten besucht werden. Versuchen Sie, die Zeit zu erhöhen!",
            "ES" to "No se puede visitar ningún punto de interés en este plazo desde este punto de partida. ¡Intenta aumentar el tiempo!",
            "NL" to "Er kunnen binnen dit tijdsbestek geen bezienswaardigheden worden bezocht vanaf dit startpunt. Probeer de tijd te verhogen!"
        ),
        "planner_result_header" to mapOf(
            "FR" to "Votre parcours recommandé",
            "EN" to "Your recommended itinerary",
            "DE" to "Ihre empfohlene Route",
            "ES" to "Tu itinerario recomendado",
            "NL" to "Uw aanbevolen route"
        ),
        "planner_project_btn" to mapOf(
            "FR" to "Suivre ce parcours sur la carte",
            "EN" to "Follow this route on map",
            "DE" to "Dieser Route auf der Karte folgen",
            "ES" to "Seguir esta ruta en el mapa",
            "NL" to "Volg deze route op de kaart"
        ),
        "see_details" to mapOf(
            "FR" to "Voir la fiche détaillée",
            "EN" to "View detailed page",
            "DE" to "Detaillierte Seite anzeigen",
            "ES" to "Ver ficha detallada",
            "NL" to "Bekijk gedetailleerde pagina"
        ),
        "back_to_list" to mapOf(
            "FR" to "Retour",
            "EN" to "Back",
            "DE" to "Zurück",
            "ES" to "Volver",
            "NL" to "Terug"
        ),
        "poi_details_title" to mapOf(
            "FR" to "Fiche du Monument",
            "EN" to "Monument Details",
            "DE" to "Details zum Denkmal",
            "ES" to "Ficha del Monumento",
            "NL" to "Details van het Monument"
        ),
        "category_label" to mapOf(
            "FR" to "Catégorie",
            "EN" to "Category",
            "DE" to "Kategorie",
            "ES" to "Categoría",
            "NL" to "Categorie"
        )
    )

    fun string(key: String, lang: String): String {
        return translations[key]?.get(lang.uppercase()) ?: translations[key]?.get("FR") ?: key
    }
}
