using Microsoft.EntityFrameworkCore;
using BourgesAdminBackend.Models;
using Newtonsoft.Json;

namespace BourgesAdminBackend.Data
{
    public class BourgesDataContext : DbContext
    {
        public BourgesDataContext(DbContextOptions<BourgesDataContext> options) : base(options)
        {
        }

        public DbSet<Site> Sites { get; set; }
        public DbSet<TourRoute> Routes { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Seed Initial Sites
            modelBuilder.Entity<Site>().HasData(
                new Site
                {
                    Id = 1,
                    Title = "Cathédrale Saint-Étienne",
                    Description = "La cathédrale Saint-Étienne de Bourges, construite entre la fin du XIIe et le XIIIe siècle, est un chef-d'œuvre de l'art gothique français et classée au patrimoine mondial de l'UNESCO. Elle se distingue par sa conception unique sans transept, créant un espace intérieur ouvert et continu d'une hauteur impressionnante. Ses vitraux médiévaux sont parmi les plus colorés et les mieux préservés d'Europe.",
                    NarrationText = "Bienvenue dans la magnifique cathédrale Saint-Étienne de Bourges. En contemplant ce joyau médiéval, remarquez l'absence de transept, une conception architecturale révolutionnaire qui permet une nef continue et un double bas-côté d'une extrême fluidité spatiale. Cette imposante structure gothique s'élève majestueusement avec des arcs-boutants qui semblent flotter dans les airs. Ses vitraux extraordinaires du XIIIe siècle racontent des paraboles médiévales qui plongent le visiteur dans un kaléidoscope de lumière et de couleur.",
                    Latitude = 47.0822,
                    Longitude = 2.4012,
                    Category = "CATHEDRAL",
                    IsPreset = true,
                    AudioDurationSec = 60,
                    TitleFr = "Cathédrale Saint-Étienne",
                    TitleEn = "Saint-Étienne Cathedral",
                    TitleDe = "Kathedrale Saint-Étienne",
                    TitleEs = "Catedral de San Esteban",
                    TitleNl = "Sint-Stefanuskathedraal",
                    DescriptionFr = "La cathédrale Saint-Étienne de Bourges, construite entre la fin du XIIe et le XIIIe siècle, est un chef-d'œuvre de l'art gothique français et classée au patrimoine mondial de l'UNESCO. Elle se distingue par sa conception unique sans transept, créant un espace intérieur ouvert et continu d'une hauteur impressionnante. Ses vitraux médiévaux sont parmi les plus colorés et les mieux préservés d'Europe.",
                    DescriptionEn = "The Saint-Étienne Cathedral of Bourges, built between the late 12th and 13th centuries, is a masterpiece of French Gothic art and a UNESCO World Heritage site. It stands out for its unique design without a transept, which creates a continuous, wide-open interior space of impressive height. Its medieval stained glass windows are among the most colorful and best-preserved in Europe.",
                    DescriptionDe = "Die Kathedrale Saint-Étienne von Bourges, erbaut zwischen dem späten 12. und 13. Jahrhundert, ist ein Meisterwerk der französischen Gotik und gehört zum UNESCO-Weltkulturerbe. Sie zeichnet sich durch ihren einzigartigen Entwurf ohne Querschiff aus, wodurch ein durchgehender, offener Innenraum von beeindruckender Höhe entsteht. Ihre mittelalterlichen Glasfenster gehören zu den farbenprächtigsten und am besten erhaltenen in Europa.",
                    DescriptionEs = "La Catedral de San Esteban de Bourges, construida entre finales del siglo XII y el XIII, es una obra maestra del arte gótico francés y Patrimonio de la Humanidad por la UNESCO. Destaca por su diseño único sin transepto, lo que crea un espacio interior diáfano y continuo de impresionante altura. Sus vidrieras medievales son de las más coloridas y mejor conservadas de Europa.",
                    DescriptionNl = "De Sint-Stefanuskathedraal van Bourges, gebouwd tussen het einde van de 12e en de 13e eeuw, is een meesterwerk van de Franse gotische kunst en staat op de Werelderfgoedlijst van UNESCO. Ze valt op door haar unieke ontwerp zonder transept, wat zorgt voor een doorlopende, open binnenruimte van indrukwekkende hoogte. De middeleeuwse glas-in-loodramen behoren tot de meest kleurrijke en best bewaarde van Europa.",
                    NarrationFr = "Bienvenue dans la magnifique cathédrale Saint-Étienne de Bourges. En contemplant ce joyau médiéval, remarquez l'absence de transept, une conception architecturale révolutionnaire qui permet une nef continue et un double bas-côté d'une extrême fluidité spatiale. Cette imposante structure gothique s'élève majestueusement avec des arcs-boutants qui semblent flotter dans les airs. Ses vitraux extraordinaires du XIIIe siècle racontent des paraboles médiévales qui plongent le visiteur dans un kaléidoscope de lumière et de couleur.",
                    NarrationEn = "Welcome to the magnificent Saint-Étienne Cathedral of Bourges. As you contemplate this medieval jewel, notice the absence of a transept, a revolutionary architectural design that allows for a continuous nave and double aisles of extreme spatial fluidity. This imposing Gothic structure rises majestically with flying buttresses that seem to float in the air. Its extraordinary thirteenth-century stained glass windows tell medieval parables, immersing the visitor in a kaleidoscope of light and color.",
                    NarrationDe = "Willkommen in der herrlichen Kathedrale Saint-Étienne von Bourges. Wenn Sie dieses mittelalterliche Juwel betrachten, bemerken Sie das Fehlen eines Querschiffs, ein revolutionärer architektonischer Entwurf, der ein durchgehendes Hauptschiff und doppelte Seitenschiffe von extremer räumlicher Fluidität ermöglicht. Diese imposante gotische Struktur erhebt sich majestätisch mit Strebepfeilern, die in der Luft zu schweben scheinen. Ihre außergewöhnlichen Glasfenster aus dem dreizehnten Jahrhundert erzählen mittelalterliche Parabeln und tauchen den Besucher in ein Kaleidoskop aus Licht und Farbe.",
                    NarrationEs = "Bienvenido a la magnífica Catedral de San Esteban de Bourges. Al contemplar esta joya medieval, observe la ausencia de transepto, un diseño arquitectónico revolucionario que permite una nave continua y doble pasillo de extrema fluidez espacial. Esta imponente estructura gótica se eleva majestuosamente con arbotantes que parecen flotar en el aire. Sus extraordinarias vidrieras del siglo trece relatan parábolas medievales que sumergen al visitante en un caleidoscopio de luz y color.",
                    NarrationNl = "Welkom in de prachtige Sint-Stefanuskathedraal van Bourges. Let bij het aanschouwen van dit middeleeuwse juweel op de afwezigheid van un transept, een revolutionair architectonisch ontwerp dat zorgt voor een doorlopend schip en dubbele zijbeuken van extreme ruimtelijke vloeiendheid. Deze imposante gotische structuur verrijst majestueus met luchtbogen die in de lucht lijken te zweven. Haar buitengewone dertiende-eeuwse glas-in-loodramen vertellen middeleeuwse gelijkenissen die de bezoeker onderdompelen in een caleidoscoop van licht en kleur."
                },
                new Site
                {
                    Id = 2,
                    Title = "Palais Jacques Cœur",
                    Description = "Chef-d'œuvre de l'architecture civile du XVe siècle. Construit par Jacques Cœur, banquier royal et argentier de Charles VII, ce palais reflète l'incroyable opulence du commerce médiéval. Le bâtiment se distingue par sa façade richement décorée, sa cour intérieure sculptée et ses cheminées détaillées montrant des scènes de la vie bourgeoise.",
                    NarrationText = "Vous êtes devant le Palais Jacques Cœur, le monument civil gothique le plus étonnant de France. Jacques Cœur, marchand de génie devenu argentier du roi Charles VII, fit bâtir cette demeure comme symbole de son immense succès. Observez les détails sculptés de la façade montrant des navires et des coquilles Saint-Jacques, emblèmes de ses voyages maritimes. Dans la cour intérieure, admirez le raffinement de la vie pré-Renaissance française, où le confort bourgeois se mêlait à l'art palatial.",
                    Latitude = 47.0844,
                    Longitude = 2.3926,
                    Category = "PALACE",
                    IsPreset = true,
                    AudioDurationSec = 60,
                    TitleFr = "Palais Jacques Cœur",
                    TitleEn = "Jacques Cœur Palace",
                    TitleDe = "Palast Jacques Cœur",
                    TitleEs = "Palacio Jacques Cœur",
                    TitleNl = "Paleis Jacques Cœur",
                    DescriptionFr = "Chef-d'œuvre de l'architecture civile du XVe siècle. Construit par Jacques Cœur, banquier royal et argentier de Charles VII, ce palais reflète l'incroyable opulence du commerce médiéval. Le bâtiment se distingue par sa façade richement décorée, sa cour intérieure sculptée et ses cheminées détaillées montrant des scènes de la vie bourgeoise.",
                    DescriptionEn = "Masterpiece of 15th-century civil architecture. Built by Jacques Cœur, royal banker and treasurer of Charles VII, this palace reflects the incredible opulence of medieval trade. The building is notable for its richly decorated facade, sculpted inner courtyard, and detailed fireplaces showing scenes of bourgeois life.",
                    DescriptionDe = "Meisterwerk der profanen Architektur des 15. Jahrhunderts. Erbaut von Jacques Cœur, dem königlichen Bankier und Schatzmeister von Karl VII., spiegelt dieser Palast den unglaublichen Reichtum des mittelalterlichen Handels wider. Das Gebäude zeichnet sich durch seine reich verzierte Fassade, seinen skulptierten Innenhof und seine detaillierten Kamine aus, die Szenen aus dem bürgerlichen Leben zeigen.",
                    DescriptionEs = "Obra cumbre de la arquitectura civil del siglo XV. Edificado por Jacques Cœur, el banquero real y tesorero de Carlos VII, este palacio refleja la increíble opulencia del comercio medieval. El edificio destaca por su fachada profusamente decorada, su patio interior esculpido y sus chimeneas detalladas que muestran escenas de la vida burguesa.",
                    DescriptionNl = "Meesterwerk van 15e-eeuwse burgerlijke architectuur. Gebouwd door Jacques Cœur, koninklijk bankier en penningmeester van Karel VII, weerspiegelt dit paleis de ongelooflijke weelde van de middeleeuwse handel. Het gebouw valt op door de rijkelijk versierde gevel, de gebeeldhouwde binnenplaats en gedetailleerde schouwen met scènes uit het burgerlijke leven.",
                    NarrationFr = "Vous êtes devant le Palais Jacques Cœur, le monument civil gothique le plus étonnant de France. Jacques Cœur, marchand de génie devenu argentier du roi Charles VII, fit bâtir cette demeure comme symbole de son immense succès. Observez les détails sculptés de la façade montrant des navires et des coquilles Saint-Jacques, emblèmes de ses voyages maritimes. Dans la cour intérieure, admirez le raffinement de la vie pré-Renaissance française, où le confort bourgeois se mêlait à l'art palatial.",
                    NarrationEn = "You are in front of the Palace of Jacques Cœur, the most amazing Gothic civil monument in France. Jacques Cœur, a visionary merchant who became treasurer to King Charles VII, built this mansion as a symbol of his immense success. Notice the sculpted details on the facade showing ships and pilgrim shells, emblems of his maritime journeys. In the inner courtyard, admire the refinement of French pre-Renaissance life, where bourgeois comfort blended with palatial art.",
                    NarrationDe = "Sie stehen vor dem Palast von Jacques Cœur, dem erstaunlichsten gotischen Profanbau Frankreichs. Jacques Cœur, ein visionärer Kaufmann, der zum Schatzmeister des Königs Karl VII. aufstieg, baute diese Residenz als Symbol seines immensen Erfolgs. Achten Sie auf die skulptierten Details an der Fassade, die Schiffe und Pilgermuscheln zeigen, Sinnbilder seiner Seereisen. Bewundern Sie im Innenhof den Raffinement des französischen Vorrenaissance-Lebens, in dem sich bürgerlicher Komfort mit palastartiger Kunst vermischte.",
                    NarrationEs = "Se encuentra frente al Palacio de Jacques Cœur, el monumento civil gótico más asombroso de Francia. Jacques Cœur, un visionario comerciante que llegó a ser tesorero del rey Carlos séptimo, construyó esta mansión como símbolo de su inmenso éxito. Fíjese en los detalles esculpidos de la fachada que muestran barcos y conchas de peregrino, emblemas de sus viajes marítimos. En el patio interior, admire el refinamiento de la vida pre-renacentista francesa, donde la comodidad burguesa se mezclaba con el arte palaciego.",
                    NarrationNl = "U bevindt zich voor het Paleis van Jacques Cœur, het meest verbazingwekkende gotische burgermonument van Frankrijk. Jacques Cœur, een visionaire koopman die penningmeester werd van koning Karel VII, bouwde dit herenhuis als symbool van zijn immense succes. Let op de gebeeldhouwde details op de gevel met schepen en pelgrimsschelpen, emblemen van zijn zeereizen. Bewonder op de binnenplaats de verfijning van het Franse leven van vóór de renaissance, waar burgerlijk comfort zich vermengde met paleiskunst."
                },
                new Site
                {
                    Id = 3,
                    Title = "Les Marais de Bourges",
                    Description = "Une zone marécageuse de 135 hectares située au pied de la vieille ville. Cultivée depuis l'époque romaine par des maraîchers, c'est aujourd'hui un havre de paix verdoyant avec des canaux sinueux et de magnifiques jardins qui offrent des promenades idylliques au milieu de la nature et de la faune locale.",
                    NarrationText = "Entrez dans les légendaires Marais de Bourges, une oasis écologique qui entoure la vieille ville. À l'origine une défense naturelle contre les envahisseurs à l'époque gallo-romaine, ce réseau de canaux de cent trente-cinq hectares fut transformé au XVIIe siècle par les pères jésuites en de fertiles jardins potagers. Aujourd'hui, c'est un précieux labyrinthe de jardins de maraîchers où le murmure de l'eau et le chant des oiseaux offrent une promenade pleine de tranquillité et de poésie.",
                    Latitude = 47.0880,
                    Longitude = 2.4030,
                    Category = "NATURE",
                    IsPreset = true,
                    AudioDurationSec = 60,
                    TitleFr = "Les Marais de Bourges",
                    TitleEn = "The Marshes of Bourges",
                    TitleDe = "Die Sümpfe von Bourges",
                    TitleEs = "Los Pantanos de Bourges",
                    TitleNl = "De Moerassen van Bourges",
                    DescriptionFr = "Une zone marécageuse de 135 hectares située au pied de la vieille ville. Cultivée depuis l'époque romaine par des maraîchers, c'est aujourd'hui un havre de paix verdoyant avec des canaux sinueux et de magnifiques jardins qui offrent des promenades idylliques au milieu de la nature et de la faune locale.",
                    DescriptionEn = "A swampy area of 135 hectares located at the foot of the old town. Cultivated since Roman times by market gardeners, today it is a green haven of peace with winding canals and beautiful gardens offering idyllic walks among nature and local wildlife.",
                    DescriptionDe = "Ein Sumpfgebiet von 135 Hektar am Fuße der Altstadt. Seit der Römerzeit von Gemüsebauern bewirtschaftet, ist es heute eine grüne Oase des Friedens mit verschlungenen Kanälen und wunderschönen Gärten, die idyllische Spaziergänge inmitten der Natur und der lokalen Tierwelt bieten.",
                    DescriptionEs = "Una zona pantanosa de 135 hectáreas situada a los pies del casco antiguo. Cultivada desde la época romana por hortelanos, hoy en día es un remaso verde de paz con canales serpenteantes y hermosos jardines que ofrecen paseos idílicos entre la naturaleza y la vida salvaje local.",
                    DescriptionNl = "Een moerasgebied van 135 hectare aan de voet van de oude stad. Sinds de Romeinse tijd bewerkt door tuinders, is het nu een groene oase van rust met kronkelende kanalen en prachtige tuinen die idyllische wandelingen bieden te midden van de natuur en de lokale fauna.",
                    NarrationFr = "Entrez dans les légendaires Marais de Bourges, une oasis écologique qui entoure la vieille ville. À l'origine une défense naturelle contre les envahisseurs à l'époque gallo-romaine, ce réseau de canaux de cent trente-cinq hectares fut transformé au XVIIe siècle par les pères jésuites en de fertiles jardins potagers. Aujourd'hui, c'est un précieux labyrinthe de jardins de maraîchers où le murmure de l'eau et le chant des oiseaux offrent une promenade pleine de tranquillité et de poésie.",
                    NarrationEn = "Step into the legendary Marshes of Bourges, an ecological oasis surrounding the city's old town. Originally a natural defense against invaders in Gallo-Roman times, this 135-hectare network of canals was transformed in the 17th century by Jesuit monks into fertile vegetable gardens. Today, it is a cherished labyrinth of floating gardens where the whisper of water and birdsong offer a peaceful, poetic stroll.",
                    NarrationDe = "Treten Sie ein in die legendären Sümpfe von Bourges, eine ökologische Oase, die die Altstadt umgibt. Ursprünglich in gallorömischer Zeit eine natürliche Verteidigung gegen Eindringlinge, wurde dieses 135 Hektar große Netz von Kanälen im 17. Jahrhundert von Jesuitenmönchen in fruchtbare Gemüsegärten verwandelt. Heute ist es ein geschätztes Labyrinth aus schwimmenden Gärten, in denen das Rauschen des Wassers und der Gesang der Vögel einen friedlichen und poetischen Spaziergang bieten.",
                    NarrationEs = "Adéntrese en los legendarios Pantanos de Bourges, un oasis ecológico que rodea el casco antiguo de la ciudad. Originalmente una defensa natural contra invasores en la época galorromana, esta red de canales de ciento treinta y cinco hectáreas fue transformada en el siglo diecisiete por los monjes jesuítas en fértiles huertas. Hoy en día, es un preciado laberinto de jardines flotantes donde el susurro del agua y el canto de las aves ofrecen un paseo lleno de tranquilidad y poesía.",
                    NarrationNl = "Stap binnen in de legendarische Moerassen van Bourges, een ecologische oase die de oude stad omringt. Oorspronkelijk een natuurlijke verdediging tegen indringers in de gallo-romeinse tijd, werd dit netwerk van kanalen van 135 hectare in de 17e eeuw door jezuïetenmonniken omgevormd tot vruchtbare moestuinen. Tegenwoordig is het een gekoesterd labyrint van drijvende tuinen waar het gefluister van het water en het gezang van vogels een vredige, poëtische wandeling bieden."
                },
                new Site
                {
                    Id = 4,
                    Title = "Jardin de l'Archevêché",
                    Description = "Conçu au XVIIe siècle par les élèves du célèbre paysagiste royal André Le Nôtre, ce jardin classique à la française entoure l'ancien palais archiépiscopal. Il propose des allées géométriques parfaites, des parterres de fleurs symétriques et une vue exceptionnelle sur la façade sud de la cathédrale.",
                    NarrationText = "Profitez de la simétrie impeccable du Jardin de l'Archevêché. Cet espace vert de style classique français a été tracé au XVIIe siècle selon la grande tradition paysagère d'André Le Nôtre, le créateur des jardins de Versailles. En flânant dans ses allées symétriques bordées de tilleuls centenaires, vous pourrez immortaliser l'une des vues photographiques les plus spectaculaires du flanc sud de la majestueuse cathédrale gothique.",
                    Latitude = 47.0815,
                    Longitude = 2.4025,
                    Category = "NATURE",
                    IsPreset = true,
                    AudioDurationSec = 60,
                    TitleFr = "Jardin de l'Archevêché",
                    TitleEn = "Archbishop's Garden",
                    TitleDe = "Garten des Erzbistums",
                    TitleEs = "Jardín del Arzobispado",
                    TitleNl = "Aartsbisschoppelijke Tuin",
                    DescriptionFr = "Conçu au XVIIe siècle par les élèves du célèbre paysagiste royal André Le Nôtre, ce jardin classique à la française entoure l'ancien palais archiépiscopal. Il propose des allées géométriques parfaites, des parterres de fleurs symétriques et une vue exceptionnelle sur la façade sud de la cathédrale.",
                    DescriptionEn = "Designed in the 17th century by students of the famous royal landscape architect André Le Nôtre, this classic French garden surrounds the former archbishop's palace. It offers perfect geometric paths, symmetrical flower beds, and an exceptional view of the Cathedral's south facade.",
                    DescriptionDe = "Im 17. Jahrhundert von Schülern des berühmten königlichen Landschaftsarchitekten André Le Nôtre entworfen, umgibt dieser klassische französische Garten das ehemalige erzbischöfliche Palais. Er bietet perfekte geometrische Wege, symmetrische Blumenbeete und einen außergewöhnlichen Blick auf die Südfassade der Kathedrale.",
                    DescriptionEs = "Diseñado en el siglo XVII por los alumnos del célebre paisajista real André Le Nôtre, este jardín clásico a la francesa rodea el antiguo palacio arzobispal. Ofrece senderos geométricos perfectos, parterres de flores simétricos y una vista excepcional de la fachada sur de la Catedral.",
                    DescriptionNl = "Ontworpen in de 17e eeuw door leerlingen van de beroemde koninklijke landschapsarchitect André Le Nôtre, omringt deze klassieke Franse tuin het voormalige aartsbisschoppelijk paleis. Het biedt perfecte geometrische paden, symmetrische bloembedden en een uitzonderlijk uitzicht op de zuidgevel van de kathedraal.",
                    NarrationFr = "Profitez de la simétrie impeccable du Jardin de l'Archevêché. Cet espace vert de style classique français a été tracé au XVIIe siècle selon la grande tradition paysagère d'André Le Nôtre, le créateur des jardins de Versailles. En flânant dans ses allées symétriques bordées de tilleuls centenaires, vous pourrez immortaliser l'une des vues photographiques les plus spectaculaires du flanc sud de la majestueuse cathédrale gothique.",
                    NarrationEn = "Enjoy the impeccable symmetry of the Archbishop's Garden. This classic French-style green space was laid out in the 17th century following the grand landscaping tradition of André Le Nôtre, the creator of the Gardens of Versailles. As you stroll along its symmetrical paths lined with hundred-year-old linden trees, you can capture one of the most spectacular photographic views of the south flank of the majestic Gothic cathedral.",
                    NarrationDe = "Genießen Sie die makellose Symmetrie des Gartens des Erzbistums. Diese Grünfläche im klassischen französischen Stil wurde im 17. Jahrhundert in der großen landschaftlichen Tradition von André Le Nôtre, dem Schöpfer der Gärten von Versailles, angelegt. Bei einem Spaziergang auf den symmetrischen Wegen, die von jahrhundertealten Linden gesäumt sind, können Sie eine der spektakulärsten fotografischen Ansichten der Südflanke der majestätischen gotischen Kathedrale einfangen.",
                    NarrationEs = "Disfrute de la simetría impecable del Jardin del Arzobispado. Este espacio verde de estilo clásico francés fue trazado en el siglo diecisiete siguiendo la gran tradición paisajista de André Le Nôtre, el creador de los jardines de Versalles. Mientras pasea por sus simétricos senderos bordeados de tilos centenarios, podrá capturar una de las vistas fotográficas más espectaculares del flanc sur de la majestuosa catedral gótica.",
                    NarrationNl = "Geniet van de onberispelijke symmetrie van de Aartsbisschoppelijke Tuin. Deze groene ruimte in klassieke Franse stijl werd in de 17e eeuw aangelegd volgens de grote landschapstraditie van André Le Nôtre, de maker van de tuinen van Versailles. Terwijl u over de symmetrische paden wandelt, omzoomd met honderd jaar oude lindebomen, kunt u een van de meest spectaculaire fotografische uitzichten vastleggen op de zuidflank van de majestueuze gotische kathedraal."
                },
                new Site
                {
                    Id = 5,
                    Title = "Musée du Berry",
                    Description = "Situé dans l'Hôtel Cujas, l'un des plus beaux hôtels particuliers de la Renaissance en brique et pierre construit en 1515. Le musée abrite d'importantes collections archéologiques gallo-romaines, des peintures médiévales et de précieuses reliques historiques locales.",
                    NarrationText = "Vous êtes devant l'Hôtel Cujas, qui abrite le Musée du Berry. Ce somptueux hôtel particulier de la Renaissance a été construit en mille prés du XVe siècle. Le bâtiment se distingue par sa combinaison de briques bicolores rouges et noires formant un motif géométrique unique sur sa façade. À l'intérieur, le musée conserve une inestimable collection gallo-romaine, notamment des bustes et des bas-reliefs funéraires sculptés qui racontent les origines ancestrales de l'ancienne capitale du peuple des Bituriges.",
                    Latitude = 47.0850,
                    Longitude = 2.3930,
                    Category = "MUSEUM",
                    IsPreset = true,
                    AudioDurationSec = 60,
                    TitleFr = "Musée du Berry",
                    TitleEn = "Berry Museum",
                    TitleDe = "Berry-Museum",
                    TitleEs = "Museo del Berry",
                    TitleNl = "Berry Museum",
                    DescriptionFr = "Situé dans l'Hôtel Cujas, l'un des plus beaux hôtels particuliers de la Renaissance en brique et pierre construit en 1515. Le musée abrite d'importantes collections archéologiques gallo-romaines, des peintures médiévales et de précieuses reliques historiques locales.",
                    DescriptionEn = "Located in the Hôtel Cujas, one of the most beautiful brick-and-stone Renaissance mansions in the Berry region, built in 1515. The museum contains important Gallo-Roman archaeological collections, medieval paintings, and valuable local historical relics.",
                    DescriptionDe = "Untergebracht im Hôtel Cujas, einem der schönsten Renaissance-Häuser aus Backstein und Stein der Region Berry, erbaut im Jahr 1515. Das Museum beherbergt bedeutende gallorömische archäologische Sammlungen, mittelalterliche Gemälde und wertvolle lokale historische Relikte.",
                    DescriptionEs = "Ubicado en el Hotel Cujas, una de las mansiones renacentistas de ladrillo y piedra más hermosas de la región de Berry, construida en 1515. El museum contiene importantes colecciones arqueológicas galorromanas, pinturas medievales y valiosas reliquias históricas locales.",
                    DescriptionNl = "Gevestigd in het Hôtel Cujas, een van de mooiste renaissance-herenhuizen van baksteen en natuursteen in de regio Berry, gebouwd in 1515. Het museum herbergt belangrijke gallo-romeinse archeologische collecties, middeleeuwse schilderijen en waardevolle lokale historische relikwieën.",
                    NarrationFr = "Vous êtes devant l'Hôtel Cujas, qui abrite le Musée du Berry. Ce somptueux hôtel particulier de la Renaissance a été construit en mille cent quinze. Le bâtiment se distingue par sa combinaison de briques bicolores rouges et noires formant un motif géométrique unique sur sa façade. À l'intérieur, le musée conserve une inestimable collection gallo-romaine, notamment des bustes et des bas-reliefs funéraires sculptés qui racontent les origines ancestrales de l'ancienne capitale du peuple des Bituriges.",
                    NarrationEn = "You are in front of the Hôtel Cujas, home to the Berry Museum. This sumptuous Renaissance mansion was built in 1515. The building stands out for its combination of bi-color red and black bricks forming a unique geometric pattern on its facade. Inside, the museum houses an invaluable Gallo-Roman collection, including sculpted busts and funerary bas-reliefs detailing the ancient origins of the former capital of the Bituriges people.",
                    NarrationDe = "Sie befinden sich vor dem Hôtel Cujas, der Residenz, die das Berry-Museum beherbergt. Dieses prächtige Renaissance-Stadthaus wurde im Jahr 1515 erbaut. Das Gebäude besticht durch die Kombination aus roten und schwarzen zweifarbigen Ziegeln, die auf der Fassade ein einzigartiges geometrisches Muster bilden. Im Inneren bewahrt das Museum eine unschätzbare gallorömische Sammlung auf, darunter skulptierte Büsten und Grabreliefs, die von den uralten Ursprüngen der ehemaligen Hauptstadt des Stammes der Biturigen erzählen.",
                    NarrationEs = "Se sitúa frente al Hotel Cujas, residencia que alberga el Museo del Berry. Este suntuoso palacete renacentista fue mandado a construir en el año mil quinientos quince. El edificio destaca por su combinación de ladrillos bicolores rojos y negros que forman un patrón geométrico único en su fachada. En su interior, el museo custodia una invaluable colección galorromana, incluidos bustos y bajorrelieves funerarios esculpidos que narran los orígenes ancestrales de la antigua capital del pueblo de los Bituriges.",
                    NarrationNl = "U bevindt zich voor het Hôtel Cujas, waarin het Berry Museum is gevestigd. Dit weelderige herenhuis uit de renaissance werd gebouwd in vijftienhonderdvijftien. Het gebouw valt op door de combinatie van rode en zwarte tweekleurige bakstenen die uniek geometrisch patroon op de gevel vormen. Binnen bewaart het museum een onschatbare gallo-romeinse collectie, waaronder gebeeldhouwde bustes en grafreliëfs die vertellen over de voorouderlijke oorsprong van de oude hoofdstad van de Bituriges."
                }
            );

            // Seed Initial Routes
            modelBuilder.Entity<TourRoute>().HasData(
                new TourRoute
                {
                    Id = 1,
                    NameFr = "Parcours du Gothique Royal",
                    NameEn = "Royal Gothic Route",
                    NameDe = "Route der königlichen Gotik",
                    NameEs = "Ruta del Gótico Real",
                    NameNl = "Koninklijke Gotiek Route",
                    DescriptionFr = "Un voyage à travers l'apogée de l'art gothique médiéval français. Visitez la majestueuse cathédrale, l'incroyable palais gothique civil de Jacques Cœur et les jardins attenants.",
                    DescriptionEn = "A journey through the height of medieval French Gothic art. Visit the majestic Cathedral, the amazing civil Gothic palace of Jacques Cœur, and the adjoining gardens.",
                    DescriptionDe = "Eine Reise durch die Blütezeit der mittelalterlichen französischen Gotik. Besuchen Sie die majestätische Kathedrale, den erstaunlichen gotischen Palast von Jacques Cœur und die angrenzenden Gärten.",
                    DescriptionEs = "Un viaje por el auge del gótico francés medieval. Visite la majestuosa Catedral, el asombroso palacio gótico-civil de Jacques Cœur y los clásicos jardines colindantes.",
                    DescriptionNl = "Een reis door het hoogtepunt van de Franse middeleeuwse gotische kunst. Bezoek de majestueuze kathedraal, het verbazingwekkende gotische burgerpaleis van Jacques Cœur en de aangrenzende tuinen.",
                    SiteIdsString = "[1,2,4]",
                    ColorHex = "#C5A059",
                    DurationMin = 45
                },
                new TourRoute
                {
                    Id = 2,
                    NameFr = "Balade des Eaux et des Marais",
                    NameEn = "Waters and Marshes Walk",
                    NameDe = "Wasser- und Sumpfspaziergang",
                    NameEs = "Paseo de las Aguas y Marismas",
                    NameNl = "Water- en Moerassenwandeling",
                    DescriptionFr = "Découvrez le poumon vert de Bourges en vous promenant le long des canaux pittoresques des marais et du jardin classique de l'Archevêché.",
                    DescriptionEn = "Discover the green lung of Bourges by walking along the picturesque canals of the marshes and the classic Archbishop's Garden.",
                    DescriptionDe = "Entdecken Sie die grüne Lunge von Bourges bei einem Spaziergang entlang der malerischen Kanäle der Sümpfe und des klassischen Gartens des Erzbistums.",
                    DescriptionEs = "Descubra el pulmón ecológico verde de Bourges paseando por sus pintorescos jardines flotantes romanos y el jardín arzobispal clásico.",
                    DescriptionNl = "Ontdek de groene long van Bourges door langs de schilderachtige kanalen van de moerassen en de klassieke Aartsbisschoppelijke Tuin te wandelen.",
                    SiteIdsString = "[3,4]",
                    ColorHex = "#1E4D2B",
                    DurationMin = 30
                },
                new TourRoute
                {
                    Id = 3,
                    NameFr = "Parcours d'Art et d'Archéologie",
                    NameEn = "Art and Archaeology Route",
                    NameDe = "Route für Kunst und Archäologie",
                    NameEs = "Ruta del Arte y Arqueología",
                    NameNl = "Kunst en Archeologie Route",
                    DescriptionFr = "Parfait pour les amateurs d'histoire gallo-romaine et de la Renaissance. Comprend la cathédrale, le magnifique Musée du Berry et la riche architecture civile du centre.",
                    DescriptionEn = "Perfect for lovers of Gallo-Roman and Renaissance history. Covers the Cathedral, the magnificent Berry Museum, and the rich civil architecture of the city center.",
                    DescriptionDe = "Perfekt für Liebhaber der gallorömischen und Renaissance-Geschichte. Umfasst die Kathedrale, das herrliche Berry-Museum und die reiche profane Architektur der Altstadt.",
                    DescriptionEs = "Perfecta para los amantes de la historia galorromana y renacentista. Abarca la Catedral, el magnífico Museo del Berry y la rica arquitectura civil del centro.",
                    DescriptionNl = "Perfect voor liefhebbers van gallo-romeinse en renaissancegeschiedenis. Omvat de kathedraal, het schitterende Berry Museum en de rijke burgerlijke architectuur van het centrum.",
                    SiteIdsString = "[1,5,2]",
                    ColorHex = "#4A154B",
                    DurationMin = 50
                }
            );
        }
    }
}
