package com.peppedess.monopolybank.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peppedess.monopolybank.MonopolyApp
import com.peppedess.monopolybank.net.TableDiscovery

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JoinScreen(onBack: () -> Unit, onConnected: () -> Unit) {
    val app = LocalContext.current.applicationContext as MonopolyApp
    val client = app.clientConnection
    val discovery = remember { TableDiscovery(app) }
    val tables by discovery.tables.collectAsState()
    val connected by client.connected.collectAsState()
    val error by client.error.collectAsState()
    var manualIp by remember { mutableStateOf("") }
    var connecting by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        discovery.start()
        onDispose { discovery.stop() }
    }

    LaunchedEffect(connected) {
        if (connected) {
            connecting = false
            onConnected()
        }
    }
    LaunchedEffect(error) { if (error != null) connecting = false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unisciti a un tavolo", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { client.disconnect(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📡", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Connettiti allo stesso Wi-Fi del telefono che fa da Banca. I tavoli aperti appaiono qui sotto automaticamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Text("Tavoli trovati", style = MaterialTheme.typography.titleMedium)

            if (tables.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LoadingIndicator()
                        Spacer(Modifier.width(14.dp))
                        Text("Ricerca in corso…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            tables.forEach { t ->
                Card(
                    onClick = {
                        connecting = true
                        client.connect(t.host, t.port)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Wifi, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name, style = MaterialTheme.typography.titleMedium)
                            Text("${t.host}:${t.port}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Entra →", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Oppure inserisci l'IP a mano", style = MaterialTheme.typography.titleMedium)
            Text(
                "Lo trovi sul telefono-Banca nella schermata principale dopo aver aperto il tavolo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = manualIp,
                    onValueChange = { manualIp = it.trim() },
                    label = { Text("es. 192.168.1.42") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
                Button(
                    onClick = { connecting = true; client.connect(manualIp) },
                    enabled = manualIp.count { it == '.' } == 3 && !connecting,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp)
                ) { Text("Vai") }
            }

            if (connecting) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoadingIndicator()
                    Spacer(Modifier.width(12.dp))
                    Text("Connessione…")
                }
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
