package com.goldofficepro.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

private val Gold = Color(0xFFD6AE45)
private val Black = Color(0xFF101010)
private val Panel = Color(0xFF202020)

class MainActivity : ComponentActivity() {
    private val pickTextFile = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                importedText = reader.readText()
            }
        }
    }

    private var importedText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldOfficeApp(
                importedText = importedText,
                onPickFile = { pickTextFile.launch(arrayOf("text/*", "application/octet-stream")) }
            )
        }
    }
}

@Composable
fun GoldOfficeApp(importedText: String, onPickFile: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("gold_office", 0) }
    var message by remember { mutableStateOf(importedText) }
    var records by remember {
        mutableStateOf(
            runCatching {
                val arr = JSONArray(prefs.getString("records", "[]"))
                (0 until arr.length()).map { arr.getString(it) }
            }.getOrDefault(emptyList())
        )
    }

    LaunchedEffect(importedText) { if (importedText.isNotBlank()) message = importedText }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, background = Black, surface = Panel,
        onPrimary = Black, onBackground = Color.White, onSurface = Color.White
    )) {
        Column(
            Modifier.fillMaxSize().background(Black).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("♛ GOLD OFFICE PRO", color = Gold, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text("WHATSAPP RECORD ORGANIZER", color = Color.LightGray, fontSize = 11.sp)
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Import message", color = Gold, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 130.dp),
                        placeholder = { Text("Paste a message or import a .txt chat export") },
                        minLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold, unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            cursorColor = Gold
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onPickFile, colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                            Text("Import TXT", color = Black)
                        }
                        OutlinedButton(onClick = { message = "" }) { Text("Clear", color = Gold) }
                    }
                    Button(
                        onClick = {
                            val cleaned = message.trim()
                            if (cleaned.isNotEmpty()) {
                                records = listOf(cleaned) + records
                                val arr = JSONArray()
                                records.forEach { arr.put(it) }
                                prefs.edit().putString("records", arr.toString()).apply()
                                message = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) { Text("SAVE RECORD", color = Black, fontWeight = FontWeight.Bold) }
                }
            }
            Text("SAVED RECORDS  •  ${records.size}", color = Gold, fontWeight = FontWeight.Bold)
            if (records.isEmpty()) {
                Text("No records yet. Paste a message or import a text file.", color = Color.LightGray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(records) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                            Text(item, Modifier.fillMaxWidth().padding(14.dp), color = Color.White)
                        }
                    }
                }
            }
            Text(
                "Private local storage • General message organization only",
                color = Color.Gray, fontSize = 10.sp
            )
        }
    }
}
