package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CsvManager
import com.example.ui.viewmodel.GuideViewModel

@Composable
fun CsvExportDialog(
    onDismiss: () -> Unit,
    viewModel: GuideViewModel,
    sitesCount: Int
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📊", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = "Export Excel des Mystères",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cet export génère un fichier CSV au format standard (séparateur point-virgule) directement ouvrable dans Microsoft Excel, LibreOffice ou Google Sheets.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Contenu exporté :",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• $sitesCount points d'intérêt avec coordonnées GPS\n• Textes de descriptions et narrations\n• Nouveaux articles mystères détaillés\n• Liens d'images Web associées",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.exportAndShareCsv(context)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_share_csv_excel"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Partager / Ouvrir dans Excel")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val csv = viewModel.exportCsvString()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Mysteres Bourges CSV", csv)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "CSV copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_copy_csv"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copier le texte CSV")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun CsvImportDialog(
    onDismiss: () -> Unit,
    viewModel: GuideViewModel,
    onImportSuccess: (Int) -> Unit
) {
    var csvText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📥", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = "Importer des Mystères (CSV)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Collez ici vos lignes CSV modifiées depuis Excel pour enrichir ou actualiser les textes des points d'intérêt :",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = csvText,
                    onValueChange = {
                        csvText = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = "${CsvManager.CSV_HEADER}\n1;Cathédrale;47.0805;2.3991;CATHEDRAL;...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("input_import_csv"),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val item = clipboard.primaryClip?.getItemAt(0)
                        val text = item?.text?.toString() ?: ""
                        if (text.isNotBlank()) {
                            csvText = text
                        } else {
                            Toast.makeText(context, "Presse-papiers vide", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Coller depuis le presse-papiers")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (csvText.isBlank()) {
                        errorMessage = "Veuillez coller le contenu CSV."
                        return@Button
                    }
                    val count = viewModel.importCsv(csvText)
                    if (count > 0) {
                        onImportSuccess(count)
                        onDismiss()
                    } else {
                        errorMessage = "Format non reconnu ou aucun point d'intérêt valide."
                    }
                },
                modifier = Modifier.testTag("btn_confirm_import_csv")
            ) {
                Text("Importer & Mettre à jour")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
