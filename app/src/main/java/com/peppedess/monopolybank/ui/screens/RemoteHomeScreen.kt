package com.peppedess.monopolybank.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peppedess.monopolybank.MonopolyApp
import com.peppedess.monopolybank.data.SpecialIds
import com.peppedess.monopolybank.net.RPlayer
import com.peppedess.monopolybank.ui.components.PlayerAvatar
import com.peppedess.monopolybank.ui.formatMoney
import com.peppedess.monopolybank.ui.theme.MonopolyGold
import com.peppedess.monopolybank.ui.theme.MonopolyGreen
import com.peppedess.monopolybank.ui.theme.MonopolyGreenDark
import com.peppedess.monopolybank.ui.theme.MonopolyInk
import com.peppedess.monopolybank.ui.theme.PropertyColors

private val QuickBanknotes = listOf(
    1L to Color(0xFFF5F5F5), 5L to Color(0xFFF8BBD0), 10L to Color(0xFFFFF59D),
    20L to Color(0xFFA5D6A7), 50L to Color(0xFF90CAF9), 100L to Color(0xFFFFCC80),
    500L to MonopolyGold
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun RemoteHomeScreen(onLeave: () -> Unit) {
    val app = LocalContext.current.applicationContext as MonopolyApp
    val client = app.clientConnection
    val state by client.state.collectAsState()
    val connected by client.connected.collectAsState()
    val myId by client.myPlayerId.collectAsState()

    var payDialog by remember { mutableStateOf(false) }
    var receiveDialog by remember { mutableStateOf(false) }

    val active = state.players.filter { it.active }
    val ranked = state.players.sortedByDescending { it.balance }
    val total = active.sumOf { it.balance }
    val leaderId = ranked.firstOrNull { it.active }?.id
    val me = state.players.find { it.id == myId }

    Scaffold { pad ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = pad,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Hero: la mia carta ──
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(MonopolyGreenDark, MonopolyGreen)),
                            RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (connected) "📡 CONNESSO AL TAVOLO" else "⚠️ CONNESSIONE PERSA",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { client.disconnect(); onLeave() }) {
                                Icon(Icons.AutoMirrored.Filled.Logout, "Esci", tint = Color.White)
                            }
                        }
                        if (me != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlayerAvatar(me.token, PropertyColors[me.colorIndex % PropertyColors.size], 64)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(me.name, style = MaterialTheme.typography.titleLarge,
                                        color = Color.White, fontWeight = FontWeight.Black)
                                    val animBal by animateIntAsState(me.balance.toInt(), tween(600), label = "myBal")
                                    Text(
                                        animBal.toLong().formatMoney(),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { client.passGo(me.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = MonopolyGreenDark
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.weight(1f).height(60.dp)
                                ) {
                                    Text("➡️ VIA", style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black, maxLines = 1)
                                }
                                Button(
                                    onClick = { payDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = MonopolyGreenDark
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.weight(1f).height(60.dp)
                                ) {
                                    Text("💸 Paga", style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black, maxLines = 1)
                                }
                                Button(
                                    onClick = { receiveDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = MonopolyGreenDark
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.weight(1f).height(60.dp)
                                ) {
                                    Text("💰 Incassa", style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black, maxLines = 1)
                                }
                            }
                        }
                        // Pozzo Parcheggio
                        if (state.parkingEnabled && state.parkingPot > 0 && me != null) {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .clickable { client.collectParking(me.id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🅿️", fontSize = 22.sp)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Parcheggio: ${state.parkingPot.formatMoney()} — tocca per riscuotere",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── Classifica ──
            item {
                Text("Classifica  ·  in circolazione ${total.formatMoney()}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
            items(ranked, key = { it.id }) { p ->
                val color = PropertyColors[p.colorIndex % PropertyColors.size]
                val share = if (total > 0 && p.active) p.balance.toFloat() / total else 0f
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (p.id == myId) MaterialTheme.colorScheme.primaryContainer
                                         else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        PlayerAvatar(p.token, color, 44)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                p.name +
                                    (if (p.id == leaderId && p.active) " 👑" else "") +
                                    (if (!p.active) " 💀" else "") +
                                    (if (p.id == myId) "  (tu)" else ""),
                                style = MaterialTheme.typography.titleMedium
                            )
                            val animBal by animateIntAsState(p.balance.toInt(), tween(600), label = "bal")
                            Text(animBal.toLong().formatMoney(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary)
                            LinearWavyProgressIndicator(
                                progress = { share },
                                modifier = Modifier.fillMaxWidth(),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Ultimi movimenti ──
            item {
                Text("Ultimi movimenti", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
            items(state.txns.take(15), key = { "${it.timestamp}-${it.fromId}-${it.toId}-${it.amount}" }) { t ->
                val nameOf: (Long) -> String = { id ->
                    when (id) {
                        SpecialIds.BANK -> "🏦 Banca"
                        SpecialIds.PARKING -> "🅿️ Parcheggio"
                        else -> state.players.find { it.id == id }?.let { "${it.token} ${it.name}" } ?: "?"
                    }
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.note, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${nameOf(t.fromId)} → ${nameOf(t.toId)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(t.amount.formatMoney(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // ── Scelta identità al primo ingresso ──
    if (myId == null && state.players.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Chi sei?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    active.forEach { p ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { client.myPlayerId.value = p.id }
                                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
                                .padding(10.dp)
                        ) {
                            PlayerAvatar(p.token, PropertyColors[p.colorIndex % PropertyColors.size], 40)
                            Spacer(Modifier.width(12.dp))
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { client.disconnect(); onLeave() }) { Text("Esci") }
            }
        )
    }

    // ── Dialog Paga / Incassa ──
    val meNow = me
    if (payDialog && meNow != null) {
        AmountDialog(
            title = "Paga a…",
            targets = buildList {
                add(Triple(SpecialIds.BANK, "🏦", "Banca"))
                if (state.parkingEnabled) add(Triple(SpecialIds.PARKING, "🅿️", "Parcheggio"))
                active.filter { it.id != meNow.id }.forEach { add(Triple(it.id, it.token, it.name)) }
            },
            onDismiss = { payDialog = false },
            onConfirm = { target, amount, note ->
                client.transfer(meNow.id, target, amount, note)
                payDialog = false
            }
        )
    }
    if (receiveDialog && meNow != null) {
        AmountDialog(
            title = "Incassa da…",
            targets = buildList {
                add(Triple(SpecialIds.BANK, "🏦", "Banca"))
                active.filter { it.id != meNow.id }.forEach { add(Triple(it.id, it.token, it.name)) }
            },
            onDismiss = { receiveDialog = false },
            onConfirm = { source, amount, note ->
                client.transfer(source, meNow.id, amount, note)
                receiveDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountDialog(
    title: String,
    targets: List<Triple<Long, String, String>>,
    onDismiss: () -> Unit,
    onConfirm: (targetId: Long, amount: Long, note: String) -> Unit
) {
    var selected by remember { mutableStateOf(targets.firstOrNull()?.first) }
    var amount by remember { mutableStateOf(0L) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(targets) { (id, token, label) ->
                        val sel = id == selected
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(
                                    if (sel) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selected = id }
                                .padding(8.dp)
                        ) {
                            Text(token, fontSize = 24.sp)
                            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
                Text(
                    amount.formatMoney(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = 4,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickBanknotes.forEach { (v, c) ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(52.dp)
                                .background(c, RoundedCornerShape(8.dp))
                                .border(1.dp, MonopolyInk.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { amount += v },
                            contentAlignment = Alignment.Center
                        ) { Text("M $v", fontWeight = FontWeight.Black, color = MonopolyInk, fontSize = 15.sp) }
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                            .clickable { amount = 0L },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("C", fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 17.sp)
                    }
                }
                OutlinedTextField(
                    value = if (amount == 0L) "" else amount.toString(),
                    onValueChange = { amount = it.filter(Char::isDigit).toLongOrNull() ?: 0L },
                    label = { Text("Importo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Causale (es. Affitto)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = selected ?: return@Button
                    onConfirm(t, amount, note.ifBlank { "Trasferimento" })
                },
                enabled = selected != null && amount > 0,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text(
                    if (amount > 0) "Conferma ${amount.formatMoney()}" else "Conferma",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}
