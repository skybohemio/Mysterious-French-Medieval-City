package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PurchaseOrder
import com.example.data.Site
import com.example.data.TourRoute
import com.example.data.User
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.viewmodel.GuideViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardDialog(
    onDismiss: () -> Unit,
    viewModel: GuideViewModel,
    sites: List<Site>,
    tourRoutes: List<TourRoute>,
    allPurchases: List<PurchaseOrder>,
    allUsers: List<User>,
    onOpenCsvExport: () -> Unit,
    onOpenCsvImport: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val totalRevenue by viewModel.totalRevenue.collectAsState()

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .testTag("admin_dashboard_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RegalBlue)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = SandstoneGold,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Centre d'Administration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Les Mystères de Bourges à vos oreilles",
                                fontSize = 12.sp,
                                color = SandstoneGold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                // Tabs Navigation
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ventes & Stripe", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Parcours (${tourRoutes.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Comptes (${allUsers.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Données CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                // Tab Contents
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> SalesAndStripeTab(
                            totalRevenue = totalRevenue,
                            allPurchases = allPurchases,
                            dateFormatter = dateFormatter
                        )
                        1 -> RoutesManagementTab(
                            tourRoutes = tourRoutes,
                            viewModel = viewModel
                        )
                        2 -> UsersManagementTab(
                            allUsers = allUsers,
                            viewModel = viewModel
                        )
                        3 -> DataCsvTab(
                            sitesCount = sites.size,
                            onOpenCsvExport = onOpenCsvExport,
                            onOpenCsvImport = onOpenCsvImport
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesAndStripeTab(
    totalRevenue: Double,
    allPurchases: List<PurchaseOrder>,
    dateFormatter: SimpleDateFormat
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // KPI Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Revenu Stripe", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        text = "%.2f €".format(totalRevenue),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SandstoneGold
                    )
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Commandes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        text = "${allPurchases.size}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Tarif Unique", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text(
                        text = "9,00 €",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RegalBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Historique des Transactions Récentes :",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (allPurchases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune transaction pour l'instant.\nLes achats des parcours payants (9 €) apparaîtront ici.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allPurchases) { order ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.routeName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Client : ${order.userEmail}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Réf Stripe : ${order.stripePaymentIntentId}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateFormatter.format(Date(order.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2E7D32)
                            ) {
                                Text(
                                    text = "%.2f €".format(order.amount),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutesManagementTab(
    tourRoutes: List<TourRoute>,
    viewModel: GuideViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tourRoutes) { route ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = route.nameFr,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (route.isPaid) SandstoneGold else Color(0xFF2E7D32)
                        ) {
                            Text(
                                text = if (route.isPaid) "9,00 €" else "GRATUIT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (route.isPaid) RegalBlue else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = route.descriptionFr,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.adminToggleRoutePaid(route) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (route.isPaid) "Passer en Gratuit" else "Passer à 9,00 €",
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.adminGrantRouteAccess(route.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = RegalBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Débloquer", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersManagementTab(
    allUsers: List<User>,
    viewModel: GuideViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(allUsers) { user ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (user.isAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SandstoneGold,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "ADMIN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RegalBlue,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (!user.isAdmin) {
                        IconButton(onClick = { viewModel.adminDeleteUser(user) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataCsvTab(
    sitesCount: Int,
    onOpenCsvExport: () -> Unit,
    onOpenCsvImport: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Base de données Patrimoniale",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$sitesCount points d'intérêt répertoriés avec articles mystères, narrations complètes et photographies haute résolution.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = onOpenCsvExport,
            colors = ButtonDefaults.buttonColors(containerColor = RegalBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, tint = SandstoneGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Exporter tous les points en CSV (Excel compatible)")
        }

        OutlinedButton(
            onClick = onOpenCsvImport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Importer un fichier CSV personnalisé")
        }
    }
}
