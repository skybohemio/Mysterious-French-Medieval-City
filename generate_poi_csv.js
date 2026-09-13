const fs = require('fs');
const path = require('path');

const escapeCsv = (val) => {
  if (val === undefined || val === null) return '';
  const str = String(val);
  if (str.includes(';') || str.includes('"') || str.includes('\n') || str.includes('\r')) {
    return '"' + str.replace(/"/g, '""') + '"';
  }
  return str;
};

const sitesData = [
  {
    id: 1,
    name: "Cathédrale Saint-Étienne - L'Athanor de Pierre",
    lat: 47.0805,
    lng: 2.3991,
    cat: "CATHEDRAL",
    descFr: "Chef-d'œuvre gothique majeur classé à l'UNESCO, célèbre pour l'absence de transept, sa verticalité saisissante et ses mystères hermétiques.",
    descEn: "UNESCO World Heritage Gothic masterpiece known for its unique transept-less design, immense verticality, and hermetic mysteries.",
    narrFr: "Vous voici devant la majestueuse Cathédrale Saint-Étienne de Bourges. Levez les yeux vers ses cinq portails uniques. Dans l'ésotérisme médiéval, cette nef continue sans transept représente l'élévation directe de l'âme humaine vers la lumière divine. Remarquez les détails sculptés : alchimistes et philosophes ont vu ici la métaphore absolue du Grand Œuvre, où la pierre brute se transmute en pure splendeur.",
    narrEn: "You stand before the majestic Saint-Étienne Cathedral of Bourges. Look up at its five unique portals. In medieval esotericism, this continuous nave without a transept symbolizes the soul's ascent toward divine illumination. For centuries, hermetic scholars have read this architecture as a monumental vessel of transmutation.",
    mystery: "La cathédrale de Bourges n'est pas seulement un temple de dévotion chrétienne : elle est considérée par l'ésotérisme comme un immense athanor de pierre. Érigée au tournant des XIIe et XIIIe siècles sans transept – cas unique pour une cathédrale de cette envergure – elle offre une nef ininterrompue créant un couloir d'ondes vibratoires singulier. Au portail du Jugement Dernier, saint Michel pèse les âmes avec une balance dont l'équilibre défie les lois terrestres. Sous la rose occidentale, les alchimistes du Berry lisaient les étapes de la transmutation du plomb en or spirituel. Dans la pénombre du déambulatoire, des motifs géométriques secrets cachés dans les vitraux du XIIIe siècle contiendraient la clé de l'harmonisation tellurique de la colline sacrée d'Avaricum.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Bourges_Cathedrale_Saint-Etienne_01.jpg/800px-Bourges_Cathedrale_Saint-Etienne_01.jpg"
  },
  {
    id: 2,
    name: "Palais Jacques Cœur - L'Énigme de l'Argentier Alchimiste",
    lat: 47.0844,
    lng: 2.3929,
    cat: "PALACE",
    descFr: "Le plus fastueux logis civil gothique de France, érigé par le grand trésorier de Charles VII, recelant de secrets symboles alchimiques.",
    descEn: "France's most luxurious Gothic civil palace, built by King Charles VII's financier, full of secret alchemical symbols.",
    narrFr: "Bienvenue au Palais Jacques Cœur. « À vaillans cuers riens impossible » : cette devise énigmatique orne les galeries de pierre. Comment Jacques Cœur, simple fils de pelletier, a-t-il pu accumuler une fortune colossale en quelques années ? La légende urbaine persiste : l'Argentier aurait découvert la Pierre Philosophale et le secret de la transmutation des métaux dans les laboratoires secrets de son palais.",
    narrEn: "Welcome to the Jacques Cœur Palace. 'To valiant hearts, nothing is impossible.' How did a humble furrier's son become the wealthiest man in Europe within years? Legend claims Jacques Cœur held the secret of the Philosopher's Stone.",
    mystery: "Jacques Cœur est l'une des figures les plus fascinantes de l'histoire de France. Grand Argentier du roi Charles VII, maître des monnaies, diplomate et armateur, il finance la reconquête du royaume contre les Anglais. Mais d'où provenait son or inépuisable ? La légende tenace veut qu'il fût initié aux arcanes de l'alchimie orientale lors de ses voyages au Levant. En observant attentivement la façade et les cheminées monumentales du palais, on découvre des arbres aux fruits d'or, des coquilles Saint-Jacques couplées à des cœurs enflammés (symboles du creuset et de la matière première), ainsi que des représentations du pélican nourrissant ses petits de son sang, allégorie hermétique de la Pierre au rouge. De nombreux souterrains partaient des caves vers les remparts, utilisés lors de sa disgrâce soudaine en 1451.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Palais_Jacques_C%C5%93ur_Bourges_FR18_01.jpg/800px-Palais_Jacques_C%C5%93ur_Bourges_FR18_01.jpg"
  },
  {
    id: 3,
    name: "Hôtel Lallemant - Le Chef-d'œuvre de l'Alchimie Universelle",
    lat: 47.0847,
    lng: 2.3967,
    cat: "PALACE",
    descFr: "Hôtel particulier Renaissance renfermant le célèbre plafond sculpté aux 30 caissons ésotériques, immortalisé par Fulcanelli.",
    descEn: "Renaissance mansion containing the famous coffered ceiling of 30 esoteric caissons, deciphered by Fulcanelli.",
    narrFr: "Vous pénétrez dans le saint des saints de l'hermétisme européen : l'Hôtel Lallemant. Ce chef-d'œuvre de la Renaissance précoce dissimule un véritable traité alchimique taillé dans le calcaire. Le plafond de l'oratoire présente trente caissons énigmatiques : un livre aux sept sceaux brûlant dans un brasier, un ange tenant un rébus cabalistique, et l'enfant soufflant sur l'athanor. Écoutez le silence de ce lieu habité par les mystères.",
    narrEn: "Enter the sanctum of European hermeticism: the Hôtel Lallemant. This early Renaissance jewel hides an alchemical codex carved in limestone. Its private oratory ceiling contains 30 sculpted caissons decoded by modern esoteric masters.",
    mystery: "L'Hôtel Lallemant est sans conteste le monument alchimique le plus réputé d'Europe occidentale. En 1926, le mystérieux maître Fulcanelli lui consacra plusieurs chapitres entiers de son livre 'Le Mystère des Cathédrales'. Construit par les frères Jean et Robert Lallemant à la suite du grand incendie de 1487, l'édifice est un hommage aux adeptes du Grand Œuvre. Dans le minuscule oratoire privé, le plafond sculpté de 30 caissons révèle la suite exacte des opérations philosophiques : la colombe de l'esprit, le lion céleste, l'épervier attaché à un cep de vigne, la ruche bourdonnante et surtout la figure d'un enfant qui souffle sur un athanor tout en tenant une coquille. Les historiens continuent de débattre : quelle fraternité secrète se réunissait dans ces salles voûtées ?",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/2/25/H%C3%B4tel_Lallemant_Bourges_facade.jpg/800px-H%C3%B4tel_Lallemant_Bourges_facade.jpg"
  },
  {
    id: 4,
    name: "L'Horloge Astronomique de Bourges (1424)",
    lat: 47.0807,
    lng: 2.3986,
    cat: "CATHEDRAL",
    descFr: "La plus ancienne horloge astronomique de France conservée intacte, conçue par le chanoine Jean Fusoris sur ordre de Charles VII.",
    descEn: "France's oldest preserved astronomical clock, designed by canon Jean Fusoris in 1424 by royal order of Charles VII.",
    narrFr: "Cette merveille mécanique a été offerte par le jeune roi Charles VII pour célébrer le baptême du futur Louis XI en 1424. Observez son cadran polychrome : il indique la position du Soleil, les phases précises de la Lune à travers une sphère d'argent, et la constellation du Zodiaque qui domine le ciel de Bourges. Un condensé de savoir astronomique et divinatoire vieux de six siècles.",
    narrEn: "This mechanical masterpiece was presented in 1424 by Charles VII. Observe its dial calculating lunar phases, solar coordinates, and astrological constellations, reflecting the medieval universe's harmonic secrets.",
    mystery: "Conçue par le mathématicien, médecin et astronome Jean Fusoris en 1424, l'Horloge Astronomique de Bourges est un prodige technologique qui a traversé les siècles. Mais au-delà de sa prouesse d'horlogerie, elle servait de guide astrologique à la cour de Charles VII réfugiée à Bourges durant la guerre de Cent Ans. À cette époque, aucun acte politique ou militaire n'était entrepris sans consulter les augures et l'alignement des corps célestes. L'aiguille solaire à visage d'or pointe les 24 heures médiévales tandis qu'une aiguille lunaire tourne dans un disque étoilé gravé selon les traditions ptolémaïques. Une légende raconte que Fusoris y aurait caché un calendrier perpétuel prédisant les grandes crises de la monarchie.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Bourges_horloge_astronomique_01.jpg/800px-Bourges_horloge_astronomique_01.jpg"
  },
  {
    id: 5,
    name: "Les Marais de Bourges - Le Territoire de la Vouivre",
    lat: 47.0885,
    lng: 2.4045,
    cat: "NATURE",
    descFr: "135 hectares de parcelles potagères entourées d'eau en plein cœur de ville, berceau de mythes et de créatures féeriques.",
    descEn: "135 hectares of idyllic marshland plots wrapped in shimmering canals, fertile ground for folklore and mythical creatures.",
    narrFr: "Laissez le tumulte de la ville derrière vous pour entrer dans le labyrinthe vert des marais de Bourges. Traversés par l'Yèvre et la Voiselle, ces canaux servaient autrefois de douves infranchissables pour protéger la cité. Au crépuscule, lorsque la brume monte des eaux, les anciens du Berry racontaient qu'on y apercevait la Vouivre, serpent fabuleux arborant une escarbille étincelante sur son front.",
    narrEn: "Step into the green labyrinth of Bourges' marshes. Carved by canals that once shielded ancient Avaricum, this watery realm is steeped in Berry folklore, home of the Vouivre serpent creature.",
    mystery: "Les Marais de Bourges constituent un écosystème unique en Europe. À l'origine zone marécageuse stratégique qui tint en échec les légions de Jules César en 52 avant J.-C., ils furent asséchés et aménagés au Moyen Âge par les moines et les maraîchers locaux. Ce monde de canaux et de 'plaques' (barques traditionnelles à fond plat) possède son panthéon de légendes populaires berrichonnes. Les légendes orales rapportent la présence d'esprits des eaux, de feux follets mystificateurs guidant les ivrognes vers la vase, et de la célèbre Vouivre, créature ailée mi-femme mi-dragon qui dépose son œil de rubis sur la berge avant de plonger.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Marais_de_Bourges_-_Canal.jpg/800px-Marais_de_Bourges_-_Canal.jpg"
  },
  {
    id: 6,
    name: "Hôtel Cujas & Musée du Berry - Secrets d'Avaricum",
    lat: 47.0838,
    lng: 2.3948,
    cat: "MUSEUM",
    descFr: "Somptueux hôtel particulier en briques rouges et calcaire, abritant les stèles gauloises et trésors archéologiques du Berry.",
    descEn: "Splendid red brick and limestone mansion home to Gallo-Roman stelae and archaeological treasures of ancient Avaricum.",
    narrFr: "L'Hôtel Cujas, bâti au début du XVIe siècle pour un riche marchand florentin, porte le nom du grand juriste Jacques Cujas. Il renferme aujourd'hui les vestiges de la puissante cité gauloise des Bituriges Cubes. Observez ces stèles funéraires gravées il y a deux mille ans : les dieux celtes et romains y cohabitent dans une étrange symbiose spirituelle.",
    narrEn: "Hôtel Cujas houses the archaeological soul of Bourges. Wander through collections featuring rare Celtic votive relics, sacred roman bronzes, and artifacts from pre-Roman Bituriges sanctuaries.",
    mystery: "L'Hôtel Cujas est bâti sur l'emplacement même de sanctuaires antiques d'Avaricum, la capitale des Bituriges Cubes ('les rois du monde' en langue celtique). Lors des fouilles du XIXe siècle dans les fondations de l'hôtel et des rues environnantes, les archéologues ont mis au jour une quantité impressionnante de stèles sculptées représentant des artisans défunts, mais aussi d'étranges divinités sylvestres et des ex-voto anatomiques en bronze. Le musée conserve également une mystérieuse collection d'amulettes de protection et les pleurants d'albâtre du tombeau de Jean de Berry, témoins du deuil royal et des rituels nécrophores médiévaux.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/H%C3%B4tel_Cujas_Bourges.jpg/800px-H%C3%B4tel_Cujas_Bourges.jpg"
  },
  {
    id: 7,
    name: "La Crypte de la Cathédrale - L'Église Basse Oubliée",
    lat: 47.0809,
    lng: 2.3995,
    cat: "CATHEDRAL",
    descFr: "La plus vaste crypte gothique d'Europe, chef-d'œuvre de substruction soutenant le chevet au-dessus des anciens fossés romains.",
    descEn: "The largest Gothic crypt in Europe, supporting the cathedral's apse over ancient Gallo-Roman defensive ditches.",
    narrFr: "Descendez les marches de pierre pour pénétrer dans les entrailles de la cathédrale : l'Église basse. Bâtie pour rattraper un dénivelé de plus de six mètres sur le rempart gallo-romain, cette forêt de piliers massifs dégage une puissance tellurique impressionnante. C'est ici que repose le gisant du grand duc Jean de Berry, sculpté dans le marbre blanc.",
    narrEn: "Descend into the underground foundations of the cathedral: the lower church. Spanning colossal stone vaults designed to span the Roman ramparts, it shelters the tomb of Duke Jean de Berry.",
    mystery: "Cette 'crypte' n'en est pas une au sens liturgique strict : c'est une église basse entièrement voûtée, inondée par une lumière tamisée venue de meurtrières ouvertes sur le jardin. Sa construction audacieuse a permis d'étendre la cathédrale au-delà de l'enceinte romaine fortifiée d'Avaricum. Au centre trône le gisant de marbre blanc de Jean de France, duc de Berry, mécène des célèbres 'Très Riches Heures'. Les récits anciens mentionnent que sous les dalles de l'église basse courait une source sacrée druidique, christianisée sous le vocable de Notre-Dame-de-Sous-Terre, dont l'eau était réputée guérir les fièvres et chasser les possessions démoniaques.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Crypte_Cathedrale_Bourges.jpg/800px-Crypte_Cathedrale_Bourges.jpg"
  },
  {
    id: 8,
    name: "Remparts Gallo-Romains & Tour des Fiefs",
    lat: 47.0818,
    lng: 2.4012,
    cat: "SOUTERRAINS",
    descFr: "L'impressionnante muraille du IVe siècle bâtie en pierres de taille antiques de réemploi pour fortifier le haut plateau berruyer.",
    descEn: "4th-century Roman fortress walls constructed with repurposed monumental antique stonework across Bourges ridge.",
    narrFr: "Regardez attentivement ces assises de pierre colossales : ce sont les remparts tardo-romains de Bourges, édifiés au IVe siècle pour résister aux invasions barbares. Plus de cinquante tours semi-circulaires défendaient autrefois ce périmètre. Les maçons de l'Antiquité tardive y ont encastré des centaines de pierres sculptées de temples païens démolis, transformant la muraille en un gigantesque puzzle mystique.",
    narrEn: "Gaze upon these colossal 4th-century Roman battlements. Over fifty defensive bastions guarded this crest. Builders re-used carved stones from demolished pagan temples, turning the wall into a cryptic stone archive.",
    mystery: "L'enceinte gallo-romaine de Bourges mesure près de deux kilomètres de périmètre et atteint par endroits plus de cinq mètres d'épaisseur. Son secret le plus stupéfiant réside dans ses fondations : menacés par les vagues d'invasions germaniques, les habitants d'Avaricum démontèrent à la hâte les temples, théâtres et mausolées de la plaine pour les sceller dans le mortier de la muraille. On peut encore distinguer des corniches sculptées, des bustes de déesses de la fécondité et des inscriptions latines incrustées à l'envers, comme pour désacraliser les anciens cultes ou créer un bouclier magique protecteur contre les assaillants.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Rempart_gallo-romain_Bourges.jpg/800px-Rempart_gallo-romain_Bourges.jpg"
  },
  {
    id: 9,
    name: "Église Notre-Dame & Le Puits du Griffon",
    lat: 47.0852,
    lng: 2.3961,
    cat: "CATHEDRAL",
    descFr: "L'église paroissiale des marchands drapiers et le puits séculaire ayant miraculeusement abreuvé la population assiégée.",
    descEn: "Parish church of the wool merchants next to the historic Griffin Well that saved the city during past sieges.",
    narrFr: "L'église Notre-Dame a été reconstruite après le tragique grand incendie de la Madeleine en 1487. Tout près de son porche se dresse l'emplacement de l'ancien Puits du Griffon. La légende raconte que ce puits puisait directement dans les réserves d'eau souterraines les plus pures du Berry, gardé par la figure chimérique d'un griffon ailé garantissant la fidélité de ses eaux.",
    narrEn: "Notre-Dame church was lovingly rebuilt after the catastrophic Great Fire of 1487. Outside stands the legendary Griffin Well, whose subterranean spring supplied cool water throughout historic sieges.",
    mystery: "L'église Notre-Dame conserve de sublimes verrières Renaissance et une chaire en chêne ouvragée ornée d'énigmes iconographiques. Mais c'est le Puits du Griffon attenant qui hante la mémoire locale : selon les chroniques du Berry, pendant le siège terrible mené en 1152 par le roi Louis VII, tous les puits de la ville haute avaient été empoisonnés ou asséchés sauf celui-ci, alimenté par une résurgence mystérieuse descendant des forêts de Menetou. Les compagnons bâtisseurs y avaient sculpté un griffon – créature mi-aigle mi-lion symbolisant la double nature du Christ et la garde des trésors cachés.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Eglise_Notre-Dame_Bourges.jpg/800px-Eglise_Notre-Dame_Bourges.jpg"
  },
  {
    id: 10,
    name: "Tour des Échevins (Musée Estève)",
    lat: 47.0841,
    lng: 2.3958,
    cat: "MUSEUM",
    descFr: "Ancien hôtel de ville de la Renaissance tardive doté d'une vertigineuse tourelle d'escalier hélicoïdale aux motifs cabalistiques.",
    descEn: "Late Gothic & Renaissance Town Hall boasting an exquisite spiral stair tower inscribed with curious guild motifs.",
    narrFr: "Voici l'Hôtel des Échevins, bâti en 1489 pour abriter les magistrats municipaux de Bourges. Admirez sa superbe tourelle octogonale percée de baies flamboyantes. L'escalier intérieur en vis de saint Gilles est un modèle de géométrie sacrée, permettant de monter vers le sommet sans jamais croiser le regard de celui qui descend.",
    narrEn: "Behold the Échevins Mansion, built in 1489 as the seat of municipal magistrates. Its octagonal stone spiral tower stands as an archetype of sacred stonemason geometry.",
    mystery: "Construite immédiatement après l'incendie dévastateur de 1487, la Tour des Échevins était le centre nerveux du pouvoir civil berruyer face au pouvoir tout-puissant des archevêques. Les maîtres maçons de la corporation y ont inséré des symboles de compagnonnage hermétique : coquilles, feuilles d'acanthe tournoyantes, gargouilles protectrices et clés de voûte étoilées. Aujourd'hui consacrée à l'œuvre du peintre abstrait Maurice Estève, elle offre une résonance unique entre les formes médiévales et la géométrie picturale moderne.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Bourges_hotel_des_echevins_tour.jpg/800px-Bourges_hotel_des_echevins_tour.jpg"
  },
  {
    id: 11,
    name: "Rue Bourbonnoux & La Maison des Trois Flacons",
    lat: 47.0831,
    lng: 2.3995,
    cat: "ALCHIMIE",
    descFr: "Rue médiévale emblématique aux maisons à pans de bois, refuge des alchimistes, herboristes et apothicaires de la cour royale.",
    descEn: "Iconic cobblestone medieval street lined with half-timbered houses, former home to apothecaries and alchemists.",
    narrFr: "Parcourez la rue Bourbonnoux, l'une des artères les plus pittoresques et chargées d'histoire de Bourges. À l'ombre des encorbellements du XVe siècle, les herboristes préparaient leurs élixirs tandis que les orfèvres battaient le métal précieux. Chaque poutre de chêne sculptée semble vous observer avec malice.",
    narrEn: "Stroll down Rue Bourbonnoux, among the most evocative preserved medieval alleyways in France. Under timber jetties, historical apothecaries brewed herbal elixirs and alchemical waters.",
    mystery: "La rue Bourbonnoux suit le tracé exact des anciennes courtines médiévales. Aux XIVe et XVe siècles, elle accueillait les ateliers des fabricants d'élixirs, maîtres-vitriers de la cathédrale et apothicaires de Jean de Berry. Au numéro 27 se tenait selon la tradition 'L'Officine aux Trois Flacons' : trois récipients de verre rouge, vert et doré symbolisant les trois phases du Grand Œuvre alchimique (Œuvre au Noir, au Blanc et au Rouge). Des inscriptions cabalistiques découvertes sur les linteaux de chêne rappellent que la recherche de la santé universelle et de la transmutation animait quotidiennement ce quartier.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/7/73/Bourges_Rue_Bourbonnoux.jpg/800px-Bourges_Rue_Bourbonnoux.jpg"
  },
  {
    id: 12,
    name: "Place Gordaine - Théâtre des Sabbats et Mystères",
    lat: 47.0858,
    lng: 2.3989,
    cat: "SORCELLERIE",
    descFr: "Le cœur vibrant du vieux Bourges, bordé de maisons à colombages du XVe siècle et théâtre des légendes berrichonnes.",
    descEn: "Vibrant medieval square ringed with fairy-tale timbered homes, scene of market assemblies and midnight lore.",
    narrFr: "Vous êtes au centre de la place Gordaine. Sous les toits pointus et les colombages bariolés, cette place commerçante accueillait les foires médiévales, les jongleurs et les crieurs publics. Mais la nuit venue, les contes berrichons affirmaient que des feux follets dansaient sur les pavés et que les esprits des marais venaient y rôder.",
    narrEn: "Stand in the middle of Place Gordaine, framed by towering timber-framed houses. By day a bustling medieval trade square; by night the legendary haunt of Berry wanderers and spectral lore.",
    mystery: "La place Gordaine tire son nom de 'Gordina', lié au marché de la laine et du bétail. Ses maisons aux façades ouvragées témoignent de l'art des charpentiers après le sinistre de 1487. Mais dans l'imaginaire du Berry, terre réputée pour ses sorciers et jeteurs de sorts, la place Gordaine était considérée comme le carrefour des quatre vents : tout ce qui se disait ici était instantanément entendu par les êtres de l'invisible. Plusieurs procès en sorcellerie instruits à Bourges au XVIe siècle évoquent des réunions nocturnes à la lueur des torches au carrefour Gordaine avant les sabbats des marais.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Bourges_Place_Gordaine.jpg/800px-Bourges_Place_Gordaine.jpg"
  },
  {
    id: 13,
    name: "Jardin de l'Archevêché & Le Méridien Secret",
    lat: 47.0801,
    lng: 2.4008,
    cat: "NATURE",
    descFr: "Jardin classique dessiné selon les plans des élèves de Le Nôtre, offrant une perspective époustouflante sur l'abside de la cathédrale.",
    descEn: "Classical French gardens designed in Le Nôtre's tradition, providing a breathtaking view of the cathedral's apse.",
    narrFr: "Promenez-vous dans les allées rectilignes du jardin de l'Archevêché. Dessiné au XVIIe siècle, ce jardin à la française offre la vue la plus spectaculaire sur l'extraordinaire chevet pyramidal de la cathédrale. Remarquez la géométrie parfaite des parterres de broderies de buis : ils s'alignent précisément sur les cours d'eau souterrains qui irriguent la colline sacrée.",
    narrEn: "Walk through the serene pathways of the Archbishop's Garden. Laid out in the seventeenth century, this classical park gives the most staggering perspective of Saint-Étienne's flying buttresses.",
    mystery: "Au cœur du jardin de l'Archevêché, une fontaine centrale et des axes géométriques parfaits masquent une réalité plus ancienne : ce plateau abritait les thermes et les casernes romaines de la garnison d'Avaricum. Au XIXe siècle, les géomètres ont découvert qu'une ligne méridienne invisible relie le point culminant du jardin directement au centre de l'autel de la cathédrale et au puits de la crypte. Ce tracé sacré correspond aux courants telluriques majeurs traversant le Berry, réputés apaiser l'esprit de ceux qui y méditent au lever du soleil.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Bourges_Jardin_Archeveche.jpg/800px-Bourges_Jardin_Archeveche.jpg"
  },
  {
    id: 14,
    name: "Halle au Blé & Le Trésor des Dîmes",
    lat: 47.0872,
    lng: 2.3952,
    cat: "PALAIS",
    descFr: "Monumentale halle métallique et pierre du XIXe siècle, érigée sur l'ancienne place du marché aux grains du Berry.",
    descEn: "Monumental grain hall combining stone and iron architecture, standing on the historic grain trading grounds.",
    narrFr: "La Halle au Blé de Bourges témoigne de la richesse agricole du Berry, surnommé le grenier de la France. Bâtie sous le Second Empire avec une superbe charpente de fonte et de verre inspirée de Baltard, elle se dresse sur un sol qui vit défiler des siècles de transactions céréalières, de révoltes frumentaires et de secrets corporatifs.",
    narrEn: "Bourges' Corn Hall embodies the agricultural bounty of the Berry province. Its striking 19th-century glass and iron structure occupies grounds where grain was bartered since feudal times.",
    mystery: "Sous les pavés de la Halle au Blé reposent de profondes caves voûtées où étaient entreposées les réserves de grains de la ville en cas de siège. Durant les guerres de religion au XVIe siècle, ces celliers souterrains servirent de cachette aux protestants pourchassés, puis de dépôt secret pour les monnaies frappées à l'atelier royal de Bourges. Des légendes tenaces rapportent que des sacs de florins et d'écus d'or y auraient été murés lors des émeutes de 1562 et n'auraient jamais été retrouvés.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Halle_au_bl%C3%A9_Bourges.jpg/800px-Halle_au_bl%C3%A9_Bourges.jpg"
  },
  {
    id: 15,
    name: "Château d'Eau & Le Réservoir des Souterrains",
    lat: 47.0782,
    lng: 2.3934,
    cat: "SOUTERRAINS",
    descFr: "Élégante rotonde néoclassique du XIXe siècle couronnant le système hydraulique et les galeries secrètes de la ville.",
    descEn: "Neoclassical 19th-century water tower rotunda presiding over Bourges' ancient underground aqueduct network.",
    narrFr: "Ce château d'eau cylindrique en pierre calcaire de Charly ressemble à un temple gréco-romain. Conçu en 1865 pour distribuer l'eau courante dans toute la cité, il est relié au réseau d'anciennes carrières souterraines qui sillonnent le sous-sol de Bourges sur des kilomètres.",
    narrEn: "This neoclassical stone rotunda looks like a Roman temple. Erected in 1865 to supply drinking water, it intersects the vast subterranean quarries and tunnels running beneath Bourges.",
    mystery: "Bourges repose sur un véritable gruyère de calcaire lacustre. Depuis l'époque gauloise et romaine, les hommes ont creusé sous la cité pour en extraire la belle pierre blanche des monuments. Ces carrières et galeries, reliées lors de la construction du château d'eau, constituent un dédale souterrain de plus de 40 kilomètres. Durant la Seconde Guerre mondiale, la Résistance berruyère y installa des imprimeries clandestines et des caches d'armes indétectables par l'occupant allemand.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/Chateau_eau_Bourges.jpg/800px-Chateau_eau_Bourges.jpg"
  },
  {
    id: 16,
    name: "Maison de la Sorcière du Berry (Rue Mirebeau)",
    lat: 47.0862,
    lng: 2.3941,
    cat: "SORCELLERIE",
    descFr: "Maison médiévale préservée associée aux contes d'Inquisition, herbiers de guérisseuses et croyances occultes du Berry.",
    descEn: "Preserved medieval timber house linked to Berry folklore, herbal healing traditions, and inquisitorial trials.",
    narrFr: "Arrêtez-vous devant cette étroite demeure de la rue Mirebeau. Le Berry a longtemps été surnommé la terre des sorciers. Les 'birettes' et 'rebouteux' y soignaient bêtes et gens par des prières secrètes, des infusions de jusquiame et des formules transmises à l'oreille au clair de lune.",
    narrEn: "Stop by this narrow medieval dwelling. The Berry province was historically known as the heartland of French folk magic and herbalists who passed healing charms through generations.",
    mystery: "Dans la mémoire rurale berrichonne immortalisée par George Sand, le 'panseux de secrets' et la 'sorcière' étaient indispensables à l'équilibre des villages. Cette maison de ville aurait abrité au XVIIe siècle une herboriste réputée capable de calmer le 'mauvais œil' et de prédire les naissances par la lecture des braises de cheminée. Condamnée par le tribunal ecclésiastique lors des grandes paniques de sorcellerie de 1605, elle disparut mystérieusement de son cachot la veille de son exécution, nourrissant la légende de son envol vers les marais.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/7/73/Bourges_Rue_Bourbonnoux.jpg/800px-Bourges_Rue_Bourbonnoux.jpg"
  },
  {
    id: 17,
    name: "Prieuré Saint-Martin & La Dalle des Templiers",
    lat: 47.0881,
    lng: 2.3962,
    cat: "TEMPLIERS",
    descFr: "Vestiges monastiques dissimulant des marques de tâcherons templiers et la croix pattée de la commanderie du Berry.",
    descEn: "Monastic remains concealing Templar stonemason carvings and the pattée cross of the Berry commandery.",
    narrFr: "Vous êtes à proximité des vestiges de l'ancien prieuré Saint-Martin. Les ordres chevaleresques, notamment les Hospitaliers et les Templiers, possédaient à Bourges de solides relais financiers et spirituels pour acheminer les dons vers la Terre Sainte.",
    narrEn: "Stand near the vestiges of the ancient Priory of Saint-Martin, historic crossroads for Templar and Hospitaller knights who maintained strong ties across the Berry province.",
    mystery: "L'Ordre du Temple entretenait des liens étroits avec les chanoines de Bourges au XIIe et XIIIe siècles. Sur certaines pierres de seuil de ce secteur, on peut encore repérer des croix pattées gravées à la pointe et des monogrammes mariaux inversés. Lors de l'arrestation tragique des Templiers le vendredi 13 octobre 1307 ordonnée par Philippe le Bel, les chevaliers de Bourges auraient évacué leurs archives secrètes et leurs reliques à travers le réseau souterrain vers la forêt de Tronçais.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Prieure_Saint_Martin_Bourges.jpg/800px-Prieure_Saint_Martin_Bourges.jpg"
  },
  {
    id: 18,
    name: "Grange des Dîmes & Entrée des Galeries Célestines",
    lat: 47.0906,
    lng: 2.3865,
    cat: "SOUTERRAINS",
    descFr: "Ancien entrepôt ecclésiastique fortifié marquant l'un des accès secrets aux galeries souterraines militaires de Bourges.",
    descEn: "Fortified tithe barn marking one of the historic entry points into Bourges' secret subterranean military tunnels.",
    narrFr: "Cette grange médiévale fortifiée servait à entreposer la dîme perçue par le clergé. Mais sa cave profonde recèle une porte de fer forgé donnant accès aux fameuses Galeries Célestines, creusées pour acheminer discrètement troupes et vivres sous les bastions du roi.",
    narrEn: "This fortified medieval tithe barn sheltered church dues. Deep inside its cellar lies an iron door opening into the legendary Celestine military gallery network.",
    mystery: "Les Galeries Célestines constituent la partie la plus spectaculaire des souterrains militaires de Bourges. Longues de plusieurs centaines de mètres, taillées à coup de pic dans le roc dur, elles permettaient aux arquebusiers de Charles VII de surprendre les assiégeants à revers. Des niches taillées dans la roche témoignent encore de l'emplacement des lampes à huile et des dépôts de poudre noire.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Crypte_Cathedrale_Bourges.jpg/800px-Crypte_Cathedrale_Bourges.jpg"
  },
  {
    id: 19,
    name: "Église Saint-Bonnet & La Flamme Perpétuelle",
    lat: 47.0869,
    lng: 2.4011,
    cat: "CATHEDRAL",
    descFr: "Édifice gothique flamboyant aux somptueux vitraux du XVIe siècle attribués au maître verrier Jean Lecuyer.",
    descEn: "Flamboyant Gothic church adorned with magnificent 16th-century stained glass windows by master Jean Lecuyer.",
    narrFr: "L'église Saint-Bonnet honore saint Bonnet, évêque de Clermont né au VIIe siècle. Reconstruite au XVIe siècle, elle abrite une série exceptionnelle de verrières dont la célèbre verrière des pèlerins d'Emmaüs, célèbre pour ses rouges rubis et ses bleus d'alchimiste.",
    narrEn: "Saint-Bonnet Church showcases sensational 16th-century Flamboyant Gothic stained glass crafted by master artisan Jean Lecuyer, celebrated for ruby-red and luminous cobalt hues.",
    mystery: "Les maîtres-verriers de Bourges possédaient une formule de coloration secrète pour obtenir un rouge pourpre inaltérable au soleil, obtenue par l'adjonction d'or colloïdal en fusion dans le verre en fusion. Cette technique, héritée directement des alchimistes arabes, était jalousement protégée sous serment corporatif. On disait que regarder le lever du soleil à travers le vitrail de saint Bonnet conférait l'apaisement des tourments de l'âme.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Bourges_Eglise_Saint-Bonnet.jpg/800px-Bourges_Eglise_Saint-Bonnet.jpg"
  },
  {
    id: 20,
    name: "Porte des Étoiles & L'Alignement Solsticial",
    lat: 47.0790,
    lng: 2.4009,
    cat: "ALCHIMIE",
    descFr: "Portique en pierre sculpté d'astres et de symboles cosmiques marquant l'alignement solaire du solstice d'hiver à Bourges.",
    descEn: "Historic stone portal carved with star constellations marking the winter solstice alignment across Bourges.",
    narrFr: "Vous êtes à la Porte des Étoiles, vestige d'un ancien hôtel d'astronomes médiévaux. Au solstice d'hiver, le premier rayon de soleil levant passe exactement au centre de cette ogive pour illuminer la rose sud de la cathédrale.",
    narrEn: "Welcome to the Gate of Stars, a portal aligned with the winter solstice sunrise that projects light directly across to Saint-Étienne's southern rose window.",
    mystery: "Les bâtisseurs médiévaux de Bourges étaient d'immenses géomètres. Comme à Stonehenge ou à Chartres, les axes majeurs de la ville ont été conçus en résonance avec les cycles cosmiques. La Porte des Étoiles témoigne de cette science oubliée où l'architecture terrestre devenait le miroir fidèle des constellations célestes.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Bourges_Cathedrale_Saint-Etienne_01.jpg/800px-Bourges_Cathedrale_Saint-Etienne_01.jpg"
  }
];

const header = "ID;Nom;Latitude;Longitude;Catégorie;Description (FR);Description (EN);Narration Text (FR);Narration Text (EN);Article Mystere;Image URL";

const rows = [header];
for (const s of sitesData) {
  const line = [
    s.id,
    escapeCsv(s.name),
    s.lat,
    s.lng,
    escapeCsv(s.cat),
    escapeCsv(s.descFr),
    escapeCsv(s.descEn),
    escapeCsv(s.narrFr),
    escapeCsv(s.narrEn),
    escapeCsv(s.mystery),
    escapeCsv(s.imageUrl)
  ].join(';');
  rows.push(line);
}

const csvOutput = "\uFEFF" + rows.join('\n'); // with UTF-8 BOM

// Write to app/src/main/assets/poi_export.csv
const assetPath = path.resolve(__dirname, 'app/src/main/assets/poi_export.csv');
fs.writeFileSync(assetPath, csvOutput, 'utf8');
console.log(`Successfully generated ${sitesData.length} POIs at ${assetPath}`);

// Also write to root directory /bourges_points_interet.csv
const rootCsvPath = path.resolve(__dirname, 'bourges_points_interet.csv');
fs.writeFileSync(rootCsvPath, csvOutput, 'utf8');
console.log(`Successfully created root export file at ${rootCsvPath}`);
