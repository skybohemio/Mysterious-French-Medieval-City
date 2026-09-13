package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TourRoute
import com.example.data.User
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.viewmodel.GuideViewModel

@Composable
fun StripeCheckoutDialog(
    route: TourRoute,
    currentUser: User?,
    onDismiss: () -> Unit,
    onPaymentSuccess: (TourRoute) -> Unit,
    viewModel: GuideViewModel
) {
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var cardNumber by remember { mutableStateOf("4242 4242 4242 4242") }
    var expiry by remember { mutableStateOf("12/28") }
    var cvc by remember { mutableStateOf("123") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successTxId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SandstoneGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Paiement Sécurisé Stripe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Powered by Stripe 💳",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Route recap card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = route.nameFr,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 2
                            )
                            Text(
                                text = "Accès illimité à vie avec audio-guide",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "9,00 €",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SandstoneGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (successTxId != null) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Paiement réussi !",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Identifiant Stripe : $successTxId",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Votre parcours est maintenant débloqué sur la carte !",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Form fields
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email pour reçu de paiement") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stripe_email_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Numéro de carte bancaire") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stripe_card_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = expiry,
                            onValueChange = { expiry = it },
                            label = { Text("MM/AA") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stripe_expiry_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = cvc,
                            onValueChange = { cvc = it },
                            label = { Text("CVC") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stripe_cvc_input")
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🔒 Cryptage SSL 256 bits conforme PCI-DSS par Stripe.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (successTxId != null) {
                Button(
                    onClick = {
                        onPaymentSuccess(route)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.testTag("stripe_success_btn")
                ) {
                    Text("Démarrer le parcours 🗺️")
                }
            } else {
                Button(
                    onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Veuillez entrer une adresse email valide."
                            return@Button
                        }
                        isProcessing = true
                        errorMessage = null
                        viewModel.processStripePayment(
                            route = route,
                            cardNumber = cardNumber,
                            expiry = expiry,
                            cvc = cvc,
                            emailInput = email
                        ) { success, result ->
                            isProcessing = false
                            if (success) {
                                successTxId = result
                            } else {
                                errorMessage = result
                            }
                        }
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = SandstoneGold),
                    modifier = Modifier.testTag("stripe_pay_btn")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = RegalBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Traitement...", color = RegalBlue)
                    } else {
                        Text("Payer 9,00 €", color = RegalBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (successTxId == null) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isProcessing
                ) {
                    Text("Annuler")
                }
            }
        }
    )
}
