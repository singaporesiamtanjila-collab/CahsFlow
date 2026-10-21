package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BackupManager
import com.example.ui.CncViewModel
import com.example.ui.components.CadGridBackground
import com.example.ui.theme.CncBorder
import com.example.ui.theme.CncBorderBright
import com.example.ui.theme.CncLaserCyan
import com.example.ui.theme.CncPrimary
import com.example.ui.theme.CncProfit
import com.example.ui.theme.CncSurface
import com.example.ui.theme.CncSurfaceVariant
import com.example.ui.theme.CncTextPrimary
import com.example.ui.theme.CncTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: CncViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val securityManager = viewModel.securityManager
    val allDesigns by viewModel.allDesigns.collectAsState()
    val currentCurrency by viewModel.currencySymbol.collectAsState()

    var isBiometricEnabled by remember { mutableStateOf(securityManager.isBiometricEnabled) }
    var showPinChangeDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showSeedConfirmDialog by remember { mutableStateOf(false) }

    // Export JSON launcher
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val json = BackupManager.exportToJsonString(allDesigns)
                val success = withContext(Dispatchers.IO) {
                    BackupManager.writeStringToUri(context, uri, json)
                }
                if (success) {
                    Toast.makeText(context, "Backup exported successfully (${allDesigns.size} designs)!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Export failed. Please check storage permissions.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Export CSV launcher
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val csv = BackupManager.exportToCsvString(allDesigns)
                val success = withContext(Dispatchers.IO) {
                    BackupManager.writeStringToUri(context, uri, csv)
                }
                if (success) {
                    Toast.makeText(context, "CSV spreadsheet exported successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "CSV export failed.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Import JSON launcher
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    BackupManager.readStringFromUri(context, uri)
                }
                if (text != null) {
                    try {
                        val imported = BackupManager.parseJsonBackup(text)
                        if (imported.isNotEmpty()) {
                            viewModel.restoreBackup(imported, replaceExisting = false)
                            Toast.makeText(context, "Imported ${imported.size} designs successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "No valid CNC design entries found in file.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to parse backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val dateStamp = remember { SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("settings_screen")
    ) {
        CadGridBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "SECURITY & DATA BACKUP",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = CncTextPrimary
                    )
                    Text(
                        text = "100% Offline SQLite storage & Biometric Authentication",
                        style = MaterialTheme.typography.bodySmall,
                        color = CncTextSecondary
                    )
                }
            }

            // Biometric Authentication Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CncSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CncPrimary.copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = CncPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "BIOMETRIC VAULT LOCK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = CncTextPrimary
                                )
                                Text(
                                    text = "Fingerprint / Face ID & PIN Security",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CncTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Toggle Biometric Lock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CncSurfaceVariant, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Lock on Launch",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CncTextPrimary
                                )
                                Text(
                                    text = if (isBiometricEnabled) "Biometric and PIN protection active" else "App opens directly without lock screen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CncTextSecondary
                                )
                            }

                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { checked ->
                                    isBiometricEnabled = checked
                                    securityManager.isBiometricEnabled = checked
                                    Toast.makeText(
                                        context,
                                        if (checked) "Biometric Lock enabled!" else "Lock disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CncPrimary
                                ),
                                modifier = Modifier.testTag("toggle_biometric_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Buttons for PIN and Testing Lock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    newPinInput = ""
                                    pinError = null
                                    showPinChangeDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("change_pin_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Change PIN", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = { viewModel.lockApp() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CncPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lock_now_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lock App Now", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // Offline Backup & Export Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CncSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CncProfit.copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = CncProfit,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "OFFLINE DATA BACKUP & RESTORE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = CncTextPrimary
                                )
                                Text(
                                    text = "Export and import database backups to local storage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CncTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Export JSON to storage
                        Button(
                            onClick = {
                                exportJsonLauncher.launch("CNC_Craft_Backup_$dateStamp.json")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CncPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_backup_json_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export JSON Backup to File (${allDesigns.size} Designs)")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Import JSON from storage
                        OutlinedButton(
                            onClick = {
                                importJsonLauncher.launch(arrayOf("application/json", "text/*"))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_backup_json_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = CncPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore Database from JSON Backup", color = CncPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Export CSV spreadsheet
                        OutlinedButton(
                            onClick = {
                                exportCsvLauncher.launch("CNC_Designs_Ledger_$dateStamp.csv")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_csv_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = CncTextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export CSV Spreadsheet (Excel / Bookkeeping)", color = CncTextPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Share
                        OutlinedButton(
                            onClick = {
                                val json = BackupManager.exportToJsonString(allDesigns)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share CNC Craft Backup")
                                context.startActivity(shareIntent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = CncTextSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Backup via WhatsApp / Drive / Email", color = CncTextSecondary)
                        }
                    }
                }
            }

            // Shop Preferences (Currency & Profile)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CncSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "CURRENCY & REGIONAL SETTINGS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = CncTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val currencyOptions = listOf("$", "₹", "৳", "€", "£", "AED", "SAR", "¥")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currencyOptions.forEach { symbol ->
                                val isSelected = currentCurrency == symbol
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) CncPrimary else CncSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.updateCurrency(symbol) }
                                ) {
                                    Text(
                                        text = symbol,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = if (isSelected) Color.White else CncTextPrimary,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reset / Demo Presets
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CncSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "DEMO DATA & DIAGNOSTICS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = CncTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Reload sample CNC door and jali designs to explore analytics and charts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CncTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showSeedConfirmDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset to CNC Sample Data")
                        }
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (showPinChangeDialog) {
        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                Column {
                    Text("Enter a 4-digit numeric passcode for vault protection.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                newPinInput = it
                                pinError = null
                            }
                        },
                        label = { Text("New 4-Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError != null,
                        supportingText = {
                            if (pinError != null) Text(pinError!!, color = MaterialTheme.colorScheme.error)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            securityManager.pinCode = newPinInput
                            Toast.makeText(context, "PIN successfully updated!", Toast.LENGTH_SHORT).show()
                            showPinChangeDialog = false
                        } else {
                            pinError = "PIN must be exactly 4 digits"
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showSeedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSeedConfirmDialog = false },
            title = { Text("Reset to Sample Data?") },
            text = { Text("This will reload sample CNC door, jali screen, and wall panel projects.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDemoData()
                        showSeedConfirmDialog = false
                        Toast.makeText(context, "Sample data reloaded!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
