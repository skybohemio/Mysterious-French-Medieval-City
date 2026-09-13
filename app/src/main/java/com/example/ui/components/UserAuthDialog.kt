package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.viewmodel.GuideViewModel

@Composable
fun UserAuthDialog(
    currentUser: User?,
    onDismiss: () -> Unit,
    onOpenAdmin: () -> Unit,
    viewModel: GuideViewModel
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = SandstoneGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (currentUser != null) "Mon Espace Voyageur" else if (isRegisterMode) "Créer un Compte" else "Connexion",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (currentUser != null) {
                    // Logged-in State
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(SandstoneGold, RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.fullName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = RegalBlue
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = currentUser.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = currentUser.email,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (currentUser.isAdmin) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = SandstoneGold,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "⚡ ADMINISTRATEUR",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = RegalBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentUser.isAdmin) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenAdmin()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SandstoneGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_admin_panel_btn")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RegalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ouvrir l'Administration", color = RegalBlue, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            message = "Déconnexion effectuée."
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Se déconnecter")
                    }
                } else {
                    // Login / Register Form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        FilterChip(
                            selected = !isRegisterMode,
                            onClick = { isRegisterMode = false },
                            label = { Text("Se connecter") }
                        )
                        FilterChip(
                            selected = isRegisterMode,
                            onClick = { isRegisterMode = true },
                            label = { Text("Créer un compte") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nom complet") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adresse email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    // Quick login helpers for testing
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Comptes prédéfinis :",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                email = "admin@bourges.fr"
                                password = "admin"
                            },
                            label = { Text("Admin (bourges.fr)", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = {
                                email = "radiobourges@gmail.com"
                                password = "bourges2026"
                            },
                            label = { Text("Radio Bourges", fontSize = 11.sp) }
                        )
                    }

                    if (message != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message ?: "",
                            color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (currentUser == null) {
                Button(
                    onClick = {
                        if (isRegisterMode) {
                            viewModel.register(email, password, fullName) { success, msg ->
                                isError = !success
                                message = msg
                                if (success) {
                                    onDismiss()
                                }
                            }
                        } else {
                            viewModel.login(email, password) { success, msg ->
                                isError = !success
                                message = msg
                                if (success) {
                                    onDismiss()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SandstoneGold),
                    modifier = Modifier.testTag("auth_submit_btn")
                ) {
                    Text(
                        text = if (isRegisterMode) "Créer mon compte" else "Se connecter",
                        color = RegalBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Fermer")
                }
            }
        },
        dismissButton = {
            if (currentUser == null) {
                TextButton(onClick = onDismiss) {
                    Text("Annuler")
                }
            }
        }
    )
}
