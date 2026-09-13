package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.R
import com.example.data.Site
import com.example.data.TourRoute
import com.example.ui.components.AdminDashboardDialog
import com.example.ui.components.AudioPlayerController
import com.example.ui.components.CsvExportDialog
import com.example.ui.components.CsvImportDialog
import com.example.ui.components.InteractiveBourgesMap
import com.example.ui.components.MysteryArticleSheet
import com.example.ui.components.MysteryPlayerBottomBar
import com.example.ui.components.StripeCheckoutDialog
import com.example.ui.components.TourRouteCartAndArticleSheet
import com.example.ui.components.UserAuthDialog
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.viewmodel.GuideViewModel
import com.stripe.android.paymentsheet.rememberPaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.PaymentSheet


enum class GuideTab(val title: String, val icon: ImageVector) {
    PARCOURS("Parcours", Icons.Default.Explore),
    CARTE("Carte", Icons.Default.Map),
    OPTIONS("Options", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BourgesGuideApp(
    viewModel: GuideViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(GuideTab.PARCOURS) }
    val sites by viewModel.sitesList.collectAsStateWithLifecycle()
    val tourRoutes by viewModel.tourRoutes.collectAsStateWithLifecycle()
    val selectedSite by viewModel.selectedSite.collectAsStateWithLifecycle()
    val selectedRoute by viewModel.selectedRoute.collectAsStateWithLifecycle()
    val activeTtsSite by viewModel.activeTtsSite.collectAsStateWithLifecycle()
    val isTtsPlaying by viewModel.isTtsPlaying.collectAsStateWithLifecycle()
    val adminMessage by viewModel.adminMessage.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val cachedTiles by viewModel.cachedMapTiles.collectAsStateWithLifecycle()
    val isOfflineMapEnabled by viewModel.isOfflineMapEnabled.collectAsStateWithLifecycle()

    val langCode = currentLanguage.code

    var activeTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("TODOS") }

    var isAdminUnlocked by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var showAdminDashboardDialog by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // PIN Authentication Dialog
    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminPinDialog = false
                pinValue = ""
                pinError = false
            },
            title = {
                Text(
                    text = Locales.string("admin_mode", langCode),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = Locales.string("admin_pin_instruction", langCode),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinValue,
                        onValueChange = {
                            pinValue = it
                            pinError = false
                        },
                        label = { Text("PIN (défaut: 1928)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                    )
                    if (pinError) {
                        Text(
                            text = Locales.string("invalid_pin", langCode),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinValue == "1928") {
                            isAdminUnlocked = true
                            showAdminPinDialog = false
                            showAdminDashboardDialog = true
                            pinValue = ""
                            scope.launch {
                                snackbarHostState.showSnackbar(Locales.string("admin_mode_unlocked", langCode))
                            }
                        } else {
                            pinError = true
                        }
                    },
                    modifier = Modifier.testTag("admin_pin_submit")
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAdminPinDialog = false
                    pinValue = ""
                    pinError = false
                }) {
                    Text(if (langCode == "FR") "Annuler" else "Cancel")
                }
            }
        )
    }

    // Show administrator snackbar notifications
    LaunchedEffect(adminMessage) {
        if (adminMessage != null) {
            snackbarHostState.showSnackbar(adminMessage!!)
            viewModel.clearAdminMessage()
        }
    }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allPurchases by viewModel.allPurchases.collectAsStateWithLifecycle()

    var showUserAuthDialog by remember { mutableStateOf(false) }
    var inspectRouteForCart by remember { mutableStateOf<TourRoute?>(null) }
    var stripeCheckoutRoute by remember { mutableStateOf<TourRoute?>(null) }

    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    var showExportCsvDialog by remember { mutableStateOf(false) }
    var showImportCsvDialog by remember { mutableStateOf(false) }
    var detailedMysterySite by remember { mutableStateOf<Site?>(null) }

    val currentSite = selectedSite ?: activeTtsSite ?: sites.firstOrNull()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(sites) {
        if (selectedSite == null && sites.isNotEmpty()) {
            viewModel.selectSite(sites.first())
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    showAdminPinDialog = true
                                }
                            )
                        }
                    ) {
                        Text(
                            text = "Les Mystères de Bourges",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = when (currentTab) {
                                GuideTab.PARCOURS -> "CHOIX DES PARCOURS 🧭"
                                GuideTab.CARTE -> if (selectedRoute != null) "PARCOURS EN COURS 🚶" else "CARTE INTERACTIVE 🗺️"
                                GuideTab.OPTIONS -> "OPTIONS & RÉGLAGES ⚙️"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = SandstoneGold
                        )
                    }
                },
                navigationIcon = {
                    if (currentTab == GuideTab.CARTE && selectedRoute != null) {
                        IconButton(
                            onClick = { viewModel.clearRoute() },
                            modifier = Modifier.testTag("btn_clear_active_route")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Quitter le parcours",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showUserAuthDialog = true },
                        modifier = Modifier.testTag("btn_header_user_auth")
                    ) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.PersonOutline,
                            contentDescription = "Mon Compte",
                            tint = if (currentUser != null) SandstoneGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                GuideTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SandstoneGold,
                            selectedTextColor = SandstoneGold,
                            indicatorColor = SandstoneGold.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                GuideTab.PARCOURS -> {
                    TourSelectionScreen(
                        tourRoutes = tourRoutes,
                        sites = sites,
                        selectedRoute = selectedRoute,
                        onSelectRouteOnMap = { route ->
                            viewModel.selectRoute(route)
                            currentTab = GuideTab.CARTE
                        },
                        onInspectRouteCart = { route ->
                            inspectRouteForCart = route
                        },
                        langCode = langCode,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                GuideTab.CARTE -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Quick Tour Routes selector bar
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedRoute == null,
                                        onClick = { viewModel.clearRoute() },
                                        label = { Text("Tous les sites (${sites.size})", fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.AllInclusive,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }

                                items(tourRoutes) { route ->
                                    val isSelected = selectedRoute?.id == route.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            inspectRouteForCart = route
                                        },
                                        label = {
                                            Text(
                                                text = route.getLocalizedName(langCode),
                                                maxLines = 1,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (!route.isPaid) Icons.Default.CardGiftcard else if (route.isPurchased) Icons.Default.CheckCircle else Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = if (!route.isPaid || route.isPurchased) Color(0xFF2E7D32) else SandstoneGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (!route.isPaid) Color(0xFF2E7D32).copy(alpha = 0.15f) else SandstoneGold.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = if (!route.isPaid) "Gratuit" else if (route.isPurchased) "Acquis" else "9 €",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (!route.isPaid) Color(0xFF2E7D32) else SandstoneGold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // OpenStreetMap + Floating pills
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            InteractiveBourgesMap(
                                sites = sites,
                                selectedSite = currentSite,
                                selectedRoute = selectedRoute,
                                onSiteSelected = { site ->
                                    viewModel.selectSite(site)
                                },
                                getNormalizedCoords = viewModel::getNormalizedCoords,
                                userLocation = userLocation,
                                onMyLocationClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                cachedTiles = cachedTiles,
                                isOfflineMapEnabled = isOfflineMapEnabled,
                                onCacheOfflineMapClick = { viewModel.cacheBourgesOfflineMap() },
                                onToggleOfflineMap = { viewModel.toggleOfflineMap(it) },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Floating active route badge on top of map
                            if (selectedRoute != null) {
                                Surface(
                                    color = RegalBlue.copy(alpha = 0.95f),
                                    shape = RoundedCornerShape(20.dp),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 10.dp)
                                        .clickable { inspectRouteForCart = selectedRoute }
                                        .testTag("floating_active_route_pill")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Route,
                                            contentDescription = null,
                                            tint = SandstoneGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = selectedRoute!!.getLocalizedName(langCode),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• Panier & Article 🛒",
                                            fontSize = 11.sp,
                                            color = SandstoneGold,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else if (currentSite != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 10.dp)
                                        .clickable { detailedMysterySite = currentSite }
                                        .testTag("floating_active_site_pill")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "🔮",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Text(
                                            text = currentSite.getLocalizedTitle(langCode),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (langCode == "FR") "Découvrir 📜" else "Discover 📜",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Dedicated Mystery Player Bottom Bar
                        MysteryPlayerBottomBar(
                            site = currentSite,
                            viewModel = viewModel,
                            onOpenArticle = { detailedMysterySite = it },
                            langCode = langCode,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                GuideTab.OPTIONS -> {
                    OptionsScreen(
                        viewModel = viewModel,
                        onOpenExportCsv = { showExportCsvDialog = true },
                        onOpenImportCsv = { showImportCsvDialog = true },
                        onOpenAdminPanel = {
                            if (currentUser?.isAdmin == true || isAdminUnlocked) {
                                showAdminDashboardDialog = true
                            } else {
                                showAdminPinDialog = true
                            }
                        },
                        onOpenAuthDialog = { showUserAuthDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Modal Sheet: Mystery Article & Photo
    if (detailedMysterySite != null) {
        MysteryArticleSheet(
            site = detailedMysterySite!!,
            onDismiss = { detailedMysterySite = null },
            viewModel = viewModel,
            langCode = langCode
        )
    }

    // Tour Route Cart & Curiosity Article Sheet
    if (inspectRouteForCart != null) {
        TourRouteCartAndArticleSheet(
            route = inspectRouteForCart!!,
            sites = sites,
            onDismiss = { inspectRouteForCart = null },
            onStartRoute = { route ->
                viewModel.selectRoute(route)
                inspectRouteForCart = null
            },
            onOpenStripeCheckout = { route ->
                stripeCheckoutRoute = route
                inspectRouteForCart = null
            },
            viewModel = viewModel,
            langCode = langCode
        )
    }

    // Stripe Checkout Dialog
    if (stripeCheckoutRoute != null) {
        StripeCheckoutDialog(
            route = stripeCheckoutRoute!!,
            currentUser = currentUser,
            onDismiss = { stripeCheckoutRoute = null },
            onPaymentSuccess = { route ->
                viewModel.selectRoute(route)
                stripeCheckoutRoute = null
            },
            viewModel = viewModel
        )
    }

    // User Authentication Dialog
    if (showUserAuthDialog) {
        UserAuthDialog(
            currentUser = currentUser,
            onDismiss = { showUserAuthDialog = false },
            onOpenAdmin = {
                showUserAuthDialog = false
                showAdminDashboardDialog = true
            },
            viewModel = viewModel
        )
    }

    // Admin Dashboard Dialog
    if (showAdminDashboardDialog) {
        AdminDashboardDialog(
            onDismiss = { showAdminDashboardDialog = false },
            viewModel = viewModel,
            sites = sites,
            tourRoutes = tourRoutes,
            allPurchases = allPurchases,
            allUsers = allUsers,
            onOpenCsvExport = {
                showAdminDashboardDialog = false
                showExportCsvDialog = true
            },
            onOpenCsvImport = {
                showAdminDashboardDialog = false
                showImportCsvDialog = true
            }
        )
    }

    // Excel / CSV Export Dialog
    if (showExportCsvDialog) {
        CsvExportDialog(
            onDismiss = { showExportCsvDialog = false },
            viewModel = viewModel,
            sitesCount = sites.size
        )
    }

    // CSV Import Dialog
    if (showImportCsvDialog) {
        CsvImportDialog(
            onDismiss = { showImportCsvDialog = false },
            viewModel = viewModel,
            onImportSuccess = { count ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (langCode == "FR") "$count mystères importés avec succès !" else "$count mysteries imported successfully!"
                    )
                }
            }
        )
    }
}

@Composable
fun DiscoverScreen(
    sites: List<Site>,
    selectedSite: Site?,
    selectedRoute: TourRoute?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categoryFilter: String,
    onCategoryChange: (String) -> Unit,
    onSiteSelected: (Site) -> Unit,
    onPlayClick: (Site) -> Unit,
    onPauseClick: () -> Unit,
    onClearRoute: () -> Unit,
    onDeleteSite: (Site) -> Unit,
    activeTtsSite: Site?,
    isTtsPlaying: Boolean,
    viewModel: GuideViewModel,
    langCode: String
) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val geminiImageState by viewModel.geminiImageState.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            viewModel.startLocationUpdates()
        }
    }

    val filteredSites = remember(sites, searchQuery, categoryFilter) {
        sites.filter { site ->
            val matchesSearch = site.title.contains(searchQuery, ignoreCase = true) || 
                                 site.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = categoryFilter == "TODOS" || site.category == categoryFilter
            matchesSearch && matchesCategory
        }
    }

    var detailedSite by remember { mutableStateOf<Site?>(null) }

    if (detailedSite != null) {
        SiteDetailScreen(
            site = detailedSite!!,
            onBack = { detailedSite = null },
            onPlayClick = onPlayClick,
            onPauseClick = onPauseClick,
            activeTtsSite = activeTtsSite,
            isTtsPlaying = isTtsPlaying,
            langCode = langCode,
            geminiImageState = geminiImageState,
            onShowOnMap = {
                onSiteSelected(detailedSite!!)
                detailedSite = null
            },
            onPlayArticleClick = { site ->
                viewModel.startArticleNarration(site)
            }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_bourges_hero_1783762752502),
                    contentDescription = "Bourges",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, RegalBlue.copy(alpha = 0.8f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = Locales.string("welcome", langCode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = Locales.string("subtitle", langCode),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        if (selectedRoute != null) {
            item {
                Surface(
                    color = Color(android.graphics.Color.parseColor(selectedRoute.colorHex)).copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(android.graphics.Color.parseColor(selectedRoute.colorHex)).copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = Color(android.graphics.Color.parseColor(selectedRoute.colorHex))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${Locales.string("active_route", langCode)}: ${selectedRoute.nameFr}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = Locales.string("showing_paths", langCode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        IconButton(onClick = onClearRoute) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = Locales.string("remove_route", langCode)
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                val cachedTiles by viewModel.cachedMapTiles.collectAsStateWithLifecycle()
                val isOfflineMapEnabled by viewModel.isOfflineMapEnabled.collectAsStateWithLifecycle()

                InteractiveBourgesMap(
                    sites = sites,
                    selectedSite = selectedSite,
                    selectedRoute = selectedRoute,
                    onSiteSelected = { site ->
                        onSiteSelected(site)
                        onPlayClick(site)
                    },
                    getNormalizedCoords = viewModel::getNormalizedCoords,
                    userLocation = userLocation,
                    onMyLocationClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    cachedTiles = cachedTiles,
                    isOfflineMapEnabled = isOfflineMapEnabled,
                    onCacheOfflineMapClick = { viewModel.cacheBourgesOfflineMap() },
                    onToggleOfflineMap = { viewModel.toggleOfflineMap(it) }
                )
                Text(
                    text = Locales.string("map_prompt", langCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    placeholder = { Text(Locales.string("search_placeholder", langCode)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                val categories = listOf("TODOS", "CATHEDRAL", "PALACE", "NATURE", "MUSEUM", "CUSTOM")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == categoryFilter
                        val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val chipText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .background(chipBg, RoundedCornerShape(20.dp))
                                .clickable { onCategoryChange(category) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("chip_$category")
                        ) {
                            Text(
                                text = getCategoryLabel(category, langCode),
                                style = MaterialTheme.typography.labelMedium,
                                color = chipText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = Locales.string("header_poi", langCode),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
            )
        }

        if (selectedSite != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("selected_site_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                val catIcon = when (selectedSite.category) {
                                    "CATHEDRAL" -> Icons.Default.Church
                                    "PALACE" -> Icons.Default.Castle
                                    "NATURE" -> Icons.Default.NaturePeople
                                    "MUSEUM" -> Icons.Default.Museum
                                    else -> Icons.Default.LocationOn
                                }
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedSite.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { viewModel.selectSite(null) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Deselect")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = selectedSite.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { detailedSite = selectedSite },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("selected_site_more_details"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = Locales.string("see_details", langCode),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Locales.string("see_details", langCode),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${Locales.string("coords", langCode)}: ${String.format("%.4f", selectedSite.latitude)}º N, ${String.format("%.4f", selectedSite.longitude)}º E",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // GPS Navigation action
                                FilledTonalButton(
                                    onClick = {
                                        val gmmIntentUri = android.net.Uri.parse("geo:${selectedSite.latitude},${selectedSite.longitude}?q=${android.net.Uri.encode(selectedSite.title)}")
                                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                        context.startActivity(mapIntent)
                                    },
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .testTag("gps_navigation_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = Locales.string("gps_navigation", langCode),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Locales.string("gps_navigation", langCode),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                if (!selectedSite.isPreset) {
                                    IconButton(
                                        onClick = { onDeleteSite(selectedSite) },
                                        modifier = Modifier.padding(end = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete custom site",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                val isSpeakingActive = activeTtsSite?.id == selectedSite.id && isTtsPlaying
                                Button(
                                    onClick = {
                                        if (isSpeakingActive) onPauseClick() else onPlayClick(selectedSite)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isSpeakingActive) Icons.Default.Stop else Icons.Default.Headset,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSpeakingActive) Locales.string("stop_voice", langCode) else Locales.string("start_audio", langCode),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filteredSites.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Locales.string("no_attractions", langCode),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(filteredSites) { site ->
                if (site.id != selectedSite?.id) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { 
                                onSiteSelected(site)
                                detailedSite = site
                            },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val catColor = when (site.category) {
                                "CATHEDRAL" -> Color(0xFFC5A059)
                                "PALACE" -> Color(0xFF4A154B)
                                "NATURE" -> Color(0xFF1E4D2B)
                                "MUSEUM" -> Color(0xFFE65100)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(catColor.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (site.category) {
                                    "CATHEDRAL" -> Icons.Default.Church
                                    "PALACE" -> Icons.Default.Castle
                                    "NATURE" -> Icons.Default.NaturePeople
                                    "MUSEUM" -> Icons.Default.Museum
                                    else -> Icons.Default.LocationOn
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = site.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = site.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { onPlayClick(site) },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Listen",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun RoutesScreen(
    routes: List<TourRoute>,
    selectedRoute: TourRoute?,
    onRouteSelected: (TourRoute) -> Unit,
    sites: List<Site>,
    langCode: String,
    viewModel: GuideViewModel
) {
    var routesTabSelected by remember { mutableIntStateOf(0) } // 0 = Standard, 1 = Smart Planner
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var selectedRouteToPurchase by remember { mutableStateOf<TourRoute?>(null) }
    val scope = rememberCoroutineScope()

    // Stripe SDK states
    var isProcessingStripe by remember { mutableStateOf(false) }
    var stripeSuccess by remember { mutableStateOf(false) }
    var stripeError by remember { mutableStateOf<String?>(null) }

    val paymentSheet = rememberPaymentSheet { paymentResult ->
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                stripeSuccess = true
                stripeError = null
                selectedRouteToPurchase?.let { route ->
                    viewModel.purchaseRoute(route.id)
                }
            }
            is PaymentSheetResult.Failed -> {
                stripeError = paymentResult.error.localizedMessage ?: "Payment failed"
                isProcessingStripe = false
            }
            is PaymentSheetResult.Canceled -> {
                stripeError = if (langCode == "FR") "Paiement annulé." else "Payment canceled."
                isProcessingStripe = false
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Row to toggle
        TabRow(
            selectedTabIndex = routesTabSelected,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = routesTabSelected == 0,
                onClick = { routesTabSelected = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Locales.string("routes_tab_predefined", langCode), fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("routes_tab_preset")
            )
            Tab(
                selected = routesTabSelected == 1,
                onClick = { routesTabSelected = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Locales.string("routes_tab_planner", langCode), fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("routes_tab_custom_planner")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (routesTabSelected == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    Text(
                        text = Locales.string("routes_header", langCode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = Locales.string("routes_desc", langCode),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(routes) { route ->
                    val isCurrent = selectedRoute?.id == route.id
                    val rColor = Color(android.graphics.Color.parseColor(route.colorHex))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(
                                width = if (isCurrent) 2.dp else 0.dp,
                                color = rColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = route.getLocalizedName(langCode),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = rColor
                                        )
                                        if (route.isPaid) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFE65100), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "PREMIUM",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    if (route.isPaid) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (route.isPurchased) Icons.Default.LockOpen else Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = if (route.isPurchased) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (route.isPurchased) 
                                                    (if (langCode == "FR") "Débloqué" else "Unlocked") 
                                                    else "${route.price} €",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (route.isPurchased) Color(0xFF2E7D32) else Color(0xFFC62828)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(rColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = rColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${route.durationMin} min",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = rColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = route.getLocalizedDescription(langCode),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = Locales.string("route_stops", langCode),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val routeSites = route.siteIds.mapNotNull { id -> sites.find { it.id == id } }
                                routeSites.take(5).forEachIndexed { index, site ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .background(rColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .border(1.dp, rColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${site.getLocalizedTitle(langCode)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = rColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (index < routeSites.size - 1 && index < 4) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = rColor.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (routeSites.size > 5) {
                                    Text(
                                        text = "+${routeSites.size - 5}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = rColor,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (route.isPaid && !route.isPurchased) {
                                Button(
                                    onClick = {
                                        selectedRouteToPurchase = route
                                        showPurchaseDialog = true
                                        stripeSuccess = false
                                        stripeError = null
                                        isProcessingStripe = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE65100)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("purchase_route_${route.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (langCode == "FR") "Débloquer le parcours - ${route.price}€" 
                                               else "Unlock Route - ${route.price}€",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onRouteSelected(route) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = rColor
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("select_route_${route.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Map, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = if (isCurrent) Locales.string("map_view", langCode) else Locales.string("trace_route", langCode))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: Smart Route Planner
            SmartRoutePlannerView(
                sites = sites,
                langCode = langCode,
                viewModel = viewModel,
                selectedRoute = selectedRoute,
                onRouteSelected = onRouteSelected
            )
        }
    }

    // Purchase Dialog
    if (showPurchaseDialog && selectedRouteToPurchase != null) {
        val route = selectedRouteToPurchase!!
        val context = LocalContext.current
        
        val hasRealStripeKeys = remember {
            val pubKey = com.example.BuildConfig.STRIPE_PUBLISHABLE_KEY
            val secKey = com.example.BuildConfig.STRIPE_SECRET_KEY
            pubKey.isNotBlank() && pubKey != "pk_test_placeholder_stripe_key_123" &&
            secKey.isNotBlank() && secKey != "sk_test_placeholder_stripe_key_123"
        }

        AlertDialog(
            onDismissRequest = {
                if (!isProcessingStripe) {
                    showPurchaseDialog = false
                    selectedRouteToPurchase = null
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (langCode == "FR") "Débloquer le Parcours" else "Unlock Itinerary",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!stripeSuccess) {
                        Text(
                            text = route.getLocalizedName(langCode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (langCode == "FR") 
                                "Accédez à plus de 150 points d'intérêt légendaires sur l'histoire et les mystères de Bourges (Alchimie, Sorcellerie, Templiers, Souterrains)." 
                                else "Access over 150 legendary points of interest on the history and mysteries of Bourges (Alchemy, Witchcraft, Templars, Undergrounds).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Price Tag Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (langCode == "FR") "Accès à vie" else "Lifetime Access",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = if (langCode == "FR") "Inclus audioguides HD illimités" else "Includes unlimited HD audioguides",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = "9,99 €",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (stripeError != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stripeError!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (isProcessingStripe) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Color(0xFFE65100))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (langCode == "FR") "Sécurisation de la transaction..." else "Securing transaction...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            if (!hasRealStripeKeys) {
                                // Sandbox / Mock guidance box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (langCode == "FR") "Mode Démo / Sandbox" else "Sandbox / Demo Mode",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (langCode == "FR") 
                                                "Les clés réelles Stripe ne sont pas configurées. Pour utiliser le vrai Stripe SDK, configurez STRIPE_PUBLISHABLE_KEY et STRIPE_SECRET_KEY dans le panneau Secrets d'AI Studio. En attendant, vous pouvez utiliser le bouton 'Démo' ci-dessous pour simuler le flux de paiement de Stripe."
                                                else "Real Stripe keys are not configured. To use the real Stripe SDK, configure STRIPE_PUBLISHABLE_KEY and STRIPE_SECRET_KEY in the AI Studio Secrets panel. Meanwhile, you can click 'Demo' below to simulate the Stripe checkout flow.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF5D4037)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            } else {
                                // Real Stripe integration helper text
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (langCode == "FR") "SDK Stripe officiel actif" else "Official Stripe SDK active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Text(
                                text = if (langCode == "FR") 
                                    "⚡ En débloquant ce parcours, vous soutenez directement l'association locale de sauvegarde du patrimoine de Bourges." 
                                    else "⚡ By unlocking this route, you directly support the local association for the safeguarding of Bourges heritage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    } else {
                        // Success state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (langCode == "FR") "Achat Validé avec Succès !" else "Purchase Validated Successfully!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (langCode == "FR") 
                                    "Le parcours mystérieux de Bourges est désormais débloqué. Retrouvez tous ses points d'intérêt directement intégrés sur votre carte interactive !" 
                                    else "The mysterious Bourges route is now unlocked. Find all its points of interest directly integrated on your interactive map!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (!stripeSuccess) {
                    Button(
                        onClick = {
                            if (isProcessingStripe) return@Button
                            stripeError = null
                            
                            if (hasRealStripeKeys) {
                                isProcessingStripe = true
                                viewModel.createStripePaymentIntent(
                                    amountCents = 999,
                                    currency = "eur",
                                    onSuccess = { clientSecret ->
                                        try {
                                            paymentSheet.presentWithPaymentIntent(
                                                clientSecret,
                                                PaymentSheet.Configuration(
                                                    merchantDisplayName = "Bourges Guide"
                                                )
                                            )
                                        } catch (e: Exception) {
                                            stripeError = e.localizedMessage ?: "Failed to open Stripe Sheet"
                                            isProcessingStripe = false
                                        }
                                    },
                                    onError = { errorMsg ->
                                        stripeError = errorMsg
                                        isProcessingStripe = false
                                    }
                                )
                            } else {
                                // Simulate mock payment sheet
                                isProcessingStripe = true
                                scope.launch {
                                    kotlinx.coroutines.delay(1800)
                                    isProcessingStripe = false
                                    stripeSuccess = true
                                    viewModel.purchaseRoute(route.id)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE65100)
                        ),
                        enabled = !isProcessingStripe,
                        modifier = Modifier.testTag("purchase_pay_button")
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasRealStripeKeys) {
                                if (langCode == "FR") "Payer 9,99 € par Carte" else "Pay €9.99 with Card"
                            } else {
                                if (langCode == "FR") "Paiement de démonstration" else "Demo Payment"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            showPurchaseDialog = false
                            selectedRouteToPurchase = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        )
                    ) {
                        Text(
                            text = if (langCode == "FR") "Commencer la visite" else "Start Visit",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                if (!stripeSuccess && !isProcessingStripe) {
                    TextButton(
                        onClick = {
                            showPurchaseDialog = false
                            selectedRouteToPurchase = null
                        }
                    ) {
                        Text(text = if (langCode == "FR") "Annuler" else "Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun SmartRoutePlannerView(
    sites: List<Site>,
    langCode: String,
    viewModel: GuideViewModel,
    selectedRoute: TourRoute?,
    onRouteSelected: (TourRoute) -> Unit
) {
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    var availableTimeMin by remember { mutableFloatStateOf(60f) }
    var useGpsAsStart by remember { mutableStateOf(userLocation != null) }
    var selectedStartSite by remember { mutableStateOf<Site?>(null) }
    var showStartSiteDropdown by remember { mutableStateOf(false) }
    var generatedRoute by remember { mutableStateOf<TourRoute?>(null) }

    LaunchedEffect(sites) {
        if (selectedStartSite == null && sites.isNotEmpty()) {
            selectedStartSite = sites.find { it.id == 1 } ?: sites.first()
        }
    }

    LaunchedEffect(userLocation) {
        if (userLocation != null && !useGpsAsStart) {
            useGpsAsStart = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        // Parameters Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Locales.string("planner_title", langCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Locales.string("planner_desc", langCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time Selection Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (langCode == "FR") "Temps de visite disponible :" else "Available Visit Time:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${availableTimeMin.toInt()} min",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = availableTimeMin,
                    onValueChange = { availableTimeMin = it },
                    valueRange = 15f..180f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("time_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Start point choice
                Text(
                    text = Locales.string("planner_start_point", langCode),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (userLocation != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { useGpsAsStart = !useGpsAsStart }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = useGpsAsStart,
                            onClick = { useGpsAsStart = true }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = Locales.string("planner_gps_option", langCode),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lat: ${String.format("%.4f", userLocation?.first)}, Lng: ${String.format("%.4f", userLocation?.second)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { useGpsAsStart = false }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = !useGpsAsStart,
                            onClick = { useGpsAsStart = false }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (langCode == "FR") "Choisir un monument de départ" else "Select a starting monument",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // No GPS location
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (langCode == "FR") 
                                    "GPS inactif (simulation). Choisissez un point de départ ci-dessous :" 
                                else "GPS inactive (simulation). Select a starting point below:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                if (!useGpsAsStart || userLocation == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        OutlinedButton(
                            onClick = { showStartSiteDropdown = true },
                            modifier = Modifier.fillMaxWidth().testTag("start_site_dropdown_btn")
                        ) {
                            Text(
                                text = selectedStartSite?.getLocalizedTitle(langCode) ?: (if (langCode == "FR") "Sélectionner..." else "Select..."),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        DropdownMenu(
                            expanded = showStartSiteDropdown,
                            onDismissRequest = { showStartSiteDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            sites.forEach { site ->
                                DropdownMenuItem(
                                    text = { Text(site.getLocalizedTitle(langCode)) },
                                    onClick = {
                                        selectedStartSite = site
                                        showStartSiteDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Suggest Route Button
                Button(
                    onClick = {
                        val startCoords = if (useGpsAsStart && userLocation != null) {
                            userLocation!!
                        } else {
                            val lat = selectedStartSite?.latitude ?: 47.0822
                            val lng = selectedStartSite?.longitude ?: 2.4012
                            Pair(lat, lng)
                        }

                        generatedRoute = viewModel.generateOptimalRoute(
                            startLat = startCoords.first,
                            startLng = startCoords.second,
                            availableTimeMin = availableTimeMin.toInt(),
                            langCode = langCode
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("generate_route_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Explore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Locales.string("planner_btn_generate", langCode))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result presentation
        val currentRoute = generatedRoute
        if (currentRoute != null) {
            val routeSites = currentRoute.siteIds.mapNotNull { id -> sites.find { it.id == id } }
            if (routeSites.isEmpty()) {
                Text(
                    text = Locales.string("planner_no_result", langCode),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                var totalDistanceMeters = 0.0
                val startCoords = if (useGpsAsStart && userLocation != null) {
                    userLocation!!
                } else {
                    val lat = selectedStartSite?.latitude ?: 47.0822
                    val lng = selectedStartSite?.longitude ?: 2.4012
                    Pair(lat, lng)
                }

                var currentLat = startCoords.first
                var currentLng = startCoords.second
                for (site in routeSites) {
                    totalDistanceMeters += viewModel.haversineDistance(currentLat, currentLng, site.latitude, site.longitude)
                    currentLat = site.latitude
                    currentLng = site.longitude
                }

                val walkingTimeSec = totalDistanceMeters / 1.1
                val walkingTimeMin = (walkingTimeSec / 60).toInt().coerceAtLeast(1)

                val listeningTimeSec = routeSites.sumOf { it.audioDurationSec }
                val listeningTimeMin = (listeningTimeSec / 60).coerceAtLeast(1)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("suggested_route_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Locales.string("planner_result_header", langCode),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentRoute.getLocalizedDescription(langCode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${availableTimeMin.toInt()} min",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Stats Summary row with beautiful widgets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatWidget(
                                label = if (langCode == "FR") "Arrêts" else "Stops",
                                value = "${routeSites.size}",
                                icon = Icons.Default.Place,
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatWidget(
                                label = if (langCode == "FR") "Distance" else "Distance",
                                value = if (totalDistanceMeters >= 1000) {
                                    String.format("%.1f km", totalDistanceMeters / 1000.0)
                                } else {
                                    "${totalDistanceMeters.toInt()} m"
                                },
                                icon = Icons.Default.DirectionsWalk,
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatWidget(
                                label = if (langCode == "FR") "Écoute" else "Listening",
                                value = "$listeningTimeMin min",
                                icon = Icons.Default.VolumeUp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Timeline of stops in order
                        Text(
                            text = if (langCode == "FR") "Détails du parcours :" else "Itinerary stops:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        routeSites.forEachIndexed { index, site ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = site.getLocalizedTitle(langCode),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${site.category} • ${site.audioDurationSec}s d'audio",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                IconButton(onClick = { viewModel.startAudioGuide(site) }) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Écouter l'audioguide",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trace route button
                        Button(
                            onClick = { onRouteSelected(currentRoute) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("follow_custom_route_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = Locales.string("planner_project_btn", langCode))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatWidget(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun AdminScreen(
    viewModel: GuideViewModel,
    sites: List<Site>,
    tourRoutes: List<TourRoute>,
    langCode: String
) {
    var adminTabSelected by remember { mutableIntStateOf(0) } // 0 = POIs, 1 = Routes

    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Locales.string("admin_header", langCode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Locales.string("admin_desc", langCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.exportSitesCsv(context) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.testTag("admin_export_csv_btn")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (langCode == "FR") "Exporter CSV" else "Export CSV")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab selection for Admin Mode (POIs or Routes)
        TabRow(
            selectedTabIndex = adminTabSelected,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = adminTabSelected == 0,
                onClick = { adminTabSelected = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (langCode == "FR") "Points d'Intérêt" else "Points of Interest", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("admin_tab_pois")
            )
            Tab(
                selected = adminTabSelected == 1,
                onClick = { adminTabSelected = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (langCode == "FR") "Parcours (Réseau)" else "Routes (Networks)", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("admin_tab_routes")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (adminTabSelected == 0) {
            PoiAdminForm(viewModel = viewModel, sites = sites, langCode = langCode)
        } else {
            RouteAdminForm(viewModel = viewModel, tourRoutes = tourRoutes, sites = sites, langCode = langCode)
        }
    }
}

@Composable
fun PoiAdminForm(
    viewModel: GuideViewModel,
    sites: List<Site>,
    langCode: String
) {
    val cachedTiles by viewModel.cachedMapTiles.collectAsStateWithLifecycle()
    val isOfflineMapEnabled by viewModel.isOfflineMapEnabled.collectAsStateWithLifecycle()

    // Dropdown to select existing POI to edit, or Create New
    var selectedSiteToEdit by remember { mutableStateOf<Site?>(null) }
    var showPoiDropdown by remember { mutableStateOf(false) }

    // Form inputs state
    var titleFr by remember { mutableStateOf("") }
    var titleEn by remember { mutableStateOf("") }
    var titleDe by remember { mutableStateOf("") }
    var titleEs by remember { mutableStateOf("") }
    var titleNl by remember { mutableStateOf("") }

    var descriptionFr by remember { mutableStateOf("") }
    var descriptionEn by remember { mutableStateOf("") }
    var descriptionDe by remember { mutableStateOf("") }
    var descriptionEs by remember { mutableStateOf("") }
    var descriptionNl by remember { mutableStateOf("") }

    var narrationFr by remember { mutableStateOf("") }
    var narrationEn by remember { mutableStateOf("") }
    var narrationDe by remember { mutableStateOf("") }
    var narrationEs by remember { mutableStateOf("") }
    var narrationNl by remember { mutableStateOf("") }

    // Map Coordinates
    var latitudeValue by remember { mutableDoubleStateOf(47.0845) }
    var longitudeValue by remember { mutableDoubleStateOf(2.3960) }
    var category by remember { mutableStateOf("CUSTOM") }

    fun clearPoiFields() {
        selectedSiteToEdit = null
        titleFr = ""; titleEn = ""; titleDe = ""; titleEs = ""; titleNl = ""
        descriptionFr = ""; descriptionEn = ""; descriptionDe = ""; descriptionEs = ""; descriptionNl = ""
        narrationFr = ""; narrationEn = ""; narrationDe = ""; narrationEs = ""; narrationNl = ""
        latitudeValue = 47.0845
        longitudeValue = 2.3960
        category = "CUSTOM"
    }

    fun populatePoiFields(site: Site) {
        selectedSiteToEdit = site
        titleFr = site.titleFr
        titleEn = site.titleEn
        titleDe = site.titleDe
        titleEs = site.titleEs
        titleNl = site.titleNl

        descriptionFr = site.descriptionFr
        descriptionEn = site.descriptionEn
        descriptionDe = site.descriptionDe
        descriptionEs = site.descriptionEs
        descriptionNl = site.descriptionNl

        narrationFr = site.narrationFr
        narrationEn = site.narrationEn
        narrationDe = site.narrationDe
        narrationEs = site.narrationEs
        narrationNl = site.narrationNl

        latitudeValue = site.latitude
        longitudeValue = site.longitude
        category = site.category
    }

    // Markdown file selection state & parser
    var markdownMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val text = inputStream.bufferedReader().use { it.readText() }
                    val parsed = parseMarkdownToLanguages(text)
                    if (parsed.isNotEmpty()) {
                        parsed["FR"]?.get("title")?.let { titleFr = it }
                        parsed["EN"]?.get("title")?.let { titleEn = it }
                        parsed["DE"]?.get("title")?.let { titleDe = it }
                        parsed["ES"]?.get("title")?.let { titleEs = it }
                        parsed["NL"]?.get("title")?.let { titleNl = it }

                        parsed["FR"]?.get("description")?.let { descriptionFr = it }
                        parsed["EN"]?.get("description")?.let { descriptionEn = it }
                        parsed["DE"]?.get("description")?.let { descriptionDe = it }
                        parsed["ES"]?.get("description")?.let { descriptionEs = it }
                        parsed["NL"]?.get("description")?.let { descriptionNl = it }

                        parsed["FR"]?.get("narration")?.let { narrationFr = it }
                        parsed["EN"]?.get("narration")?.let { narrationEn = it }
                        parsed["DE"]?.get("narration")?.let { narrationDe = it }
                        parsed["ES"]?.get("narration")?.let { narrationEs = it }
                        parsed["NL"]?.get("narration")?.let { narrationNl = it }

                        markdownMessage = when (langCode) {
                            "FR" -> "Fichier Markdown importé avec succès !"
                            "EN" -> "Markdown file successfully imported!"
                            "DE" -> "Markdown-Datei erfolgreich importiert!"
                            "NL" -> "Markdown-bestand succesvol geïmporteerd!"
                            else -> "¡Archivo Markdown importado con éxito!"
                        }
                    } else {
                        markdownMessage = "Format non reconnu ou vide."
                    }
                }
            } catch (e: Exception) {
                markdownMessage = "Erreur: ${e.message}"
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // POI selection box
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showPoiDropdown = true },
                    modifier = Modifier.fillMaxWidth().testTag("select_poi_to_edit_btn")
                ) {
                    Text(
                        text = if (selectedSiteToEdit == null) {
                            if (langCode == "FR") "➕ Créer Nouveau Point d'Intérêt" else "➕ Create New Point of Interest"
                        } else {
                            "✏️ ${selectedSiteToEdit!!.title}"
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = showPoiDropdown,
                    onDismissRequest = { showPoiDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (langCode == "FR") "➕ Créer Nouveau" else "➕ Create New", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            clearPoiFields()
                            showPoiDropdown = false
                        }
                    )
                    Divider()
                    sites.forEach { site ->
                        DropdownMenuItem(
                            text = { Text("${if (site.isPreset) "🏛️ [Preset] " else "👤 "} ${site.title}") },
                            onClick = {
                                populatePoiFields(site)
                                showPoiDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Location & Map Placement
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (langCode == "FR") "🗺️ ÉDITEUR CARTE INTERACTIF (Tapez pour placer)" else "🗺️ INTERACTIVE MAP PLACEMENT (Tap to position)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Temporary site structure with current edited coordinates
                    val tempSitesList = remember(sites, selectedSiteToEdit, latitudeValue, longitudeValue) {
                        val filtered = sites.filter { it.id != selectedSiteToEdit?.id }
                        filtered + Site(
                            id = 999999,
                            title = if (titleFr.isNotBlank()) titleFr else "Temp Marker",
                            description = "",
                            narrationText = "",
                            latitude = latitudeValue,
                            longitude = longitudeValue,
                            category = category,
                            isPreset = false
                        )
                    }

                    InteractiveBourgesMap(
                        sites = tempSitesList,
                        selectedSite = tempSitesList.last(),
                        selectedRoute = null,
                        onSiteSelected = { site ->
                            if (site.id != 999999) {
                                populatePoiFields(site)
                            }
                        },
                        getNormalizedCoords = viewModel::getNormalizedCoords,
                        modifier = Modifier.height(180.dp),
                        onMapTapped = { lat, lng ->
                            latitudeValue = lat
                            longitudeValue = lng
                        },
                        cachedTiles = cachedTiles,
                        isOfflineMapEnabled = isOfflineMapEnabled
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Latitude: ${String.format("%.5f", latitudeValue)}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Longitude: ${String.format("%.5f", longitudeValue)}", style = MaterialTheme.typography.bodySmall)
                        }

                        // Presets shortcut zones
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AssistChip(
                                onClick = { latitudeValue = 47.0825; longitudeValue = 2.3980 },
                                label = { Text("Cathédrale") }
                            )
                            AssistChip(
                                onClick = { latitudeValue = 47.0865; longitudeValue = 2.4010 },
                                label = { Text("Marais") }
                            )
                        }
                    }
                }
            }
        }

        // Markdown text parsing import
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (langCode == "FR") "📝 Importation Markdown" else "📝 Markdown Import",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (langCode == "FR") "Remplissez les 5 langues en un clic !" else "Fill all 5 languages instantly!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { fileLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Importer")
                    }
                }
            }

            if (markdownMessage != null) {
                Text(
                    text = markdownMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
            }
        }

        // Fields inputs with Multi-language tabs selector
        item {
            var activeLangTab by remember { mutableStateOf("FR") }
            val langTabs = listOf("FR", "EN", "DE", "ES", "NL")
            val langFlags = mapOf("FR" to "🇫🇷", "EN" to "🇬🇧", "DE" to "🇩🇪", "ES" to "🇪🇸", "NL" to "🇳🇱")

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (langCode == "FR") "🌐 SAISIE DES TEXTES MULTILINGUES" else "🌐 MULTILINGUAL TEXT ENTRIES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Sub-tabs languages
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    langTabs.forEach { tab ->
                        val isSelected = tab == activeLangTab
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val fg = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable { activeLangTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${langFlags[tab]} $tab",
                                fontWeight = FontWeight.Bold,
                                color = fg,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Conditionally display inputs based on active language tab
                when (activeLangTab) {
                    "FR" -> LanguageFields(
                        title = titleFr, onTitleChange = { titleFr = it },
                        desc = descriptionFr, onDescChange = { descriptionFr = it },
                        narr = narrationFr, onNarrChange = { narrationFr = it },
                        labelSuffix = "FR 🇫🇷", langCode = langCode
                    )
                    "EN" -> LanguageFields(
                        title = titleEn, onTitleChange = { titleEn = it },
                        desc = descriptionEn, onDescChange = { descriptionEn = it },
                        narr = narrationEn, onNarrChange = { narrationEn = it },
                        labelSuffix = "EN 🇬🇧", langCode = langCode
                    )
                    "DE" -> LanguageFields(
                        title = titleDe, onTitleChange = { titleDe = it },
                        desc = descriptionDe, onDescChange = { descriptionDe = it },
                        narr = narrationDe, onNarrChange = { narrationDe = it },
                        labelSuffix = "DE 🇩🇪", langCode = langCode
                    )
                    "ES" -> LanguageFields(
                        title = titleEs, onTitleChange = { titleEs = it },
                        desc = descriptionEs, onDescChange = { descriptionEs = it },
                        narr = narrationEs, onNarrChange = { narrationEs = it },
                        labelSuffix = "ES 🇪🇸", langCode = langCode
                    )
                    "NL" -> LanguageFields(
                        title = titleNl, onTitleChange = { titleNl = it },
                        desc = descriptionNl, onDescChange = { descriptionNl = it },
                        narr = narrationNl, onNarrChange = { narrationNl = it },
                        labelSuffix = "NL 🇳🇱", langCode = langCode
                    )
                }
            }
        }

        // Monument category and Action buttons
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = Locales.string("attr_category", langCode),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val categories = listOf("CATHEDRAL", "PALACE", "NATURE", "MUSEUM", "CUSTOM")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { category = cat }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getCategoryLabel(cat, langCode),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save Button
                Button(
                    onClick = {
                        viewModel.saveSite(
                            id = selectedSiteToEdit?.id ?: 0,
                            isPreset = selectedSiteToEdit?.isPreset ?: false,
                            category = category,
                            latitude = latitudeValue,
                            longitude = longitudeValue,
                            titleFr = titleFr, titleEn = titleEn, titleDe = titleDe, titleEs = titleEs, titleNl = titleNl,
                            descriptionFr = descriptionFr, descriptionEn = descriptionEn, descriptionDe = descriptionDe, descriptionEs = descriptionEs, descriptionNl = descriptionNl,
                            narrationFr = narrationFr, narrationEn = narrationEn, narrationDe = narrationDe, narrationEs = narrationEs, narrationNl = narrationNl
                        )
                        clearPoiFields()
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("admin_save_site_btn")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (selectedSiteToEdit == null) "Créer" else "Enregistrer")
                }

                // Delete Button (Only for custom points or non-preset existing sites)
                if (selectedSiteToEdit != null && !selectedSiteToEdit!!.isPreset) {
                    Button(
                        onClick = {
                            viewModel.deleteCustomSite(selectedSiteToEdit!!)
                            clearPoiFields()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.height(48.dp).testTag("admin_delete_site_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageFields(
    title: String, onTitleChange: (String) -> Unit,
    desc: String, onDescChange: (String) -> Unit,
    narr: String, onNarrChange: (String) -> Unit,
    labelSuffix: String,
    langCode: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("${Locales.string("attr_name", langCode)} [$labelSuffix]") },
            modifier = Modifier.fillMaxWidth().testTag("admin_title_$labelSuffix"),
            singleLine = true
        )

        OutlinedTextField(
            value = desc,
            onValueChange = onDescChange,
            label = { Text("${Locales.string("short_desc", langCode)} [$labelSuffix]") },
            modifier = Modifier.fillMaxWidth().height(70.dp).testTag("admin_desc_$labelSuffix"),
            maxLines = 2
        )

        OutlinedTextField(
            value = narr,
            onValueChange = onNarrChange,
            label = { Text("${Locales.string("narration_script", langCode)} [$labelSuffix]") },
            placeholder = { Text(Locales.string("script_placeholder", langCode)) },
            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("admin_script_$labelSuffix"),
            maxLines = 5
        )
    }
}

@Composable
fun RouteAdminForm(
    viewModel: GuideViewModel,
    tourRoutes: List<TourRoute>,
    sites: List<Site>,
    langCode: String
) {
    var selectedRouteToEdit by remember { mutableStateOf<TourRoute?>(null) }
    var showRouteDropdown by remember { mutableStateOf(false) }

    // Route states
    var nameFr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var nameDe by remember { mutableStateOf("") }
    var nameEs by remember { mutableStateOf("") }
    var nameNl by remember { mutableStateOf("") }

    var descFr by remember { mutableStateOf("") }
    var descEn by remember { mutableStateOf("") }
    var descDe by remember { mutableStateOf("") }
    var descEs by remember { mutableStateOf("") }
    var descNl by remember { mutableStateOf("") }

    var colorHex by remember { mutableStateOf("#C5A059") }
    var durationValue by remember { mutableIntStateOf(45) }
    var selectedSiteIds = remember { mutableStateListOf<Int>() }

    fun clearRouteFields() {
        selectedRouteToEdit = null
        nameFr = ""; nameEn = ""; nameDe = ""; nameEs = ""; nameNl = ""
        descFr = ""; descEn = ""; descDe = ""; descEs = ""; descNl = ""
        colorHex = "#C5A059"
        durationValue = 45
        selectedSiteIds.clear()
    }

    fun populateRouteFields(route: TourRoute) {
        selectedRouteToEdit = route
        nameFr = route.nameFr
        nameEn = route.nameEn
        nameDe = route.nameDe
        nameEs = route.nameEs
        nameNl = route.nameNl

        descFr = route.descriptionFr
        descEn = route.descriptionEn
        descDe = route.descriptionDe
        descEs = route.descriptionEs
        descNl = route.descriptionNl

        colorHex = route.colorHex
        durationValue = route.durationMin
        selectedSiteIds.clear()
        selectedSiteIds.addAll(route.siteIds)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showRouteDropdown = true },
                    modifier = Modifier.fillMaxWidth().testTag("select_route_to_edit_btn")
                ) {
                    Text(
                        text = if (selectedRouteToEdit == null) {
                            if (langCode == "FR") "➕ Créer Nouveau Parcours" else "➕ Create New Route"
                        } else {
                            "✏️ ${selectedRouteToEdit!!.nameFr}"
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = showRouteDropdown,
                    onDismissRequest = { showRouteDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (langCode == "FR") "➕ Créer Nouveau" else "➕ Create New", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            clearRouteFields()
                            showRouteDropdown = false
                        }
                    )
                    Divider()
                    tourRoutes.forEach { route ->
                        DropdownMenuItem(
                            text = { Text("🛣️ ${route.nameFr}") },
                            onClick = {
                                populateRouteFields(route)
                                showRouteDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Multi-language names and descriptions
        item {
            var activeRouteLangTab by remember { mutableStateOf("FR") }
            val langTabs = listOf("FR", "EN", "DE", "ES", "NL")
            val langFlags = mapOf("FR" to "🇫🇷", "EN" to "🇬🇧", "DE" to "🇩🇪", "ES" to "🇪🇸", "NL" to "🇳🇱")

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (langCode == "FR") "🌐 TEXTES DU PARCOURS MULTILINGUES" else "🌐 MULTILINGUAL ROUTE TEXTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    langTabs.forEach { tab ->
                        val isSelected = tab == activeRouteLangTab
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val fg = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable { activeRouteLangTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${langFlags[tab]} $tab",
                                fontWeight = FontWeight.Bold,
                                color = fg,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val labelSuff = when (activeRouteLangTab) {
                    "FR" -> "FR 🇫🇷"
                    "EN" -> "EN 🇬🇧"
                    "DE" -> "DE 🇩🇪"
                    "ES" -> "ES 🇪🇸"
                    else -> "NL 🇳🇱"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = when (activeRouteLangTab) {
                            "FR" -> nameFr
                            "EN" -> nameEn
                            "DE" -> nameDe
                            "ES" -> nameEs
                            else -> nameNl
                        },
                        onValueChange = { text ->
                            when (activeRouteLangTab) {
                                "FR" -> nameFr = text
                                "EN" -> nameEn = text
                                "DE" -> nameDe = text
                                "ES" -> nameEs = text
                                else -> nameNl = text
                            }
                        },
                        label = { Text("Nom du Parcours [$labelSuff]") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = when (activeRouteLangTab) {
                            "FR" -> descFr
                            "EN" -> descEn
                            "DE" -> descDe
                            "ES" -> descEs
                            else -> descNl
                        },
                        onValueChange = { text ->
                            when (activeRouteLangTab) {
                                "FR" -> descFr = text
                                "EN" -> descEn = text
                                "DE" -> descDe = text
                                "ES" -> descEs = text
                                else -> descNl = text
                            }
                        },
                        label = { Text("Description Touristique [$labelSuff]") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                }
            }
        }

        // Color and Duration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Color selection presets
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Couleur Ligne", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val colors = listOf("#C5A059", "#4A154B", "#1E4D2B", "#E65100", "#D32F2F")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            colors.forEach { col ->
                                val isSelected = colorHex.uppercase() == col.uppercase()
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(col)))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                        .clickable { colorHex = col }
                                )
                            }
                        }
                    }

                    // Duration Slider
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Durée", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(text = "$durationValue min", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = durationValue.toFloat(),
                            onValueChange = { durationValue = it.toInt() },
                            valueRange = 10f..180f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Stops selector checklist (POIs)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🏛️ SÉLECTION DES ÉTAPES SUR LE PARCOURS :",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    sites.forEach { site ->
                        val isChecked = selectedSiteIds.contains(site.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedSiteIds.remove(site.id)
                                    else selectedSiteIds.add(site.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked == true) selectedSiteIds.add(site.id)
                                    else selectedSiteIds.remove(site.id)
                                },
                                modifier = Modifier.testTag("route_stop_chk_${site.id}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = site.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveRoute(
                            id = selectedRouteToEdit?.id ?: 0,
                            colorHex = colorHex,
                            durationMin = durationValue,
                            siteIds = selectedSiteIds.toList(),
                            nameFr = nameFr, nameEn = nameEn, nameDe = nameDe, nameEs = nameEs, nameNl = nameNl,
                            descriptionFr = descFr, descriptionEn = descEn, descriptionDe = descDe, descriptionEs = descEs, descriptionNl = descNl
                        )
                        clearRouteFields()
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("admin_save_route_btn")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (selectedRouteToEdit == null) "Créer" else "Enregistrer")
                }

                if (selectedRouteToEdit != null) {
                    Button(
                        onClick = {
                            viewModel.deleteRoute(selectedRouteToEdit!!)
                            clearRouteFields()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.height(48.dp).testTag("admin_delete_route_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}

fun parseMarkdownToLanguages(markdownText: String): Map<String, Map<String, String>> {
    val result = mutableMapOf<String, MutableMap<String, String>>()
    var currentLang: String? = null
    var currentKey: String? = null
    val currentBuffer = StringBuilder()

    fun flushBuffer() {
        val lang = currentLang
        val key = currentKey
        val text = currentBuffer.toString().trim()
        if (lang != null && key != null && text.isNotEmpty()) {
            if (!result.containsKey(lang)) {
                result[lang] = mutableMapOf()
            }
            result[lang]!![key] = text.removePrefix(":").trim()
        }
        currentBuffer.clear()
    }

    markdownText.lines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.startsWith("#") && !trimmed.startsWith("##")) {
            flushBuffer()
            val langCandidate = trimmed.removePrefix("#").trim().uppercase()
            currentLang = when {
                langCandidate.contains("FR") || langCandidate.contains("FRENCH") || langCandidate.contains("FRAN") -> "FR"
                langCandidate.contains("EN") || langCandidate.contains("ENGLISH") || langCandidate.contains("ANGL") -> "EN"
                langCandidate.contains("DE") || langCandidate.contains("GERMAN") || langCandidate.contains("DEUT") -> "DE"
                langCandidate.contains("ES") || langCandidate.contains("SPANISH") || langCandidate.contains("ESPA") -> "ES"
                langCandidate.contains("NL") || langCandidate.contains("DUTCH") || langCandidate.contains("NEER") || langCandidate.contains("HOLL") -> "NL"
                else -> null
            }
            currentKey = null
        } else if (trimmed.startsWith("##")) {
            flushBuffer()
            val fieldCandidate = trimmed.removePrefix("##").trim().uppercase()
            currentKey = when {
                fieldCandidate.contains("TITLE") || fieldCandidate.contains("TITRE") || fieldCandidate.contains("TÍTULO") || fieldCandidate.contains("NAAM") -> "title"
                fieldCandidate.contains("DESC") || fieldCandidate.contains("SHORT") -> "description"
                fieldCandidate.contains("NARR") || fieldCandidate.contains("SCRIPT") || fieldCandidate.contains("TEXT") || fieldCandidate.contains("TEXTE") -> "narration"
                else -> null
            }
        } else {
            if (currentLang != null && currentKey != null) {
                if (currentBuffer.isNotEmpty()) {
                    currentBuffer.append("\n")
                }
                currentBuffer.append(line)
            }
        }
    }
    flushBuffer()
    return result
}

private fun getCategoryLabel(category: String, lang: String): String {
    return when (category.uppercase()) {
        "TODOS" -> when (lang) {
            "FR" -> "TOUT"
            "EN" -> "ALL"
            "DE" -> "ALLE"
            "NL" -> "ALLES"
            else -> "TODOS"
        }
        "CATHEDRAL" -> when (lang) {
            "FR" -> "CATHÉDRALE"
            "EN" -> "CATHEDRAL"
            "DE" -> "KATHEDRALE"
            "NL" -> "KATHEDRAAL"
            else -> "CATEDRAL"
        }
        "PALACE" -> when (lang) {
            "FR" -> "PALAIS"
            "EN" -> "PALACE"
            "DE" -> "PALAST"
            "NL" -> "PALEIS"
            else -> "PALACIO"
        }
        "NATURE" -> when (lang) {
            "FR" -> "NATURE"
            "EN" -> "NATURE"
            "DE" -> "NATUR"
            "NL" -> "NATUUR"
            else -> "NATURALEZA"
        }
        "MUSEUM" -> when (lang) {
            "FR" -> "MUSÉE"
            "EN" -> "MUSEUM"
            "DE" -> "MUSEUM"
            "NL" -> "MUSEUM"
            else -> "MUSEO"
        }
        "CUSTOM" -> when (lang) {
            "FR" -> "PERSO"
            "EN" -> "CUSTOM"
            "DE" -> "EIGENE"
            "NL" -> "EIGEN"
            else -> "PERSONALIZADOS"
        }
        else -> category
    }
}

