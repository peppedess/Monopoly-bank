package com.peppedess.monopolybank.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peppedess.monopolybank.data.SpecialIds
import com.peppedess.monopolybank.ui.BankViewModel
import com.peppedess.monopolybank.ui.components.PlayerAvatar
import com.peppedess.monopolybank.ui.formatMoney
import com.peppedess.monopolybank.ui.theme.MonopolyGreen
import com.peppedess.monopolybank.ui.theme.MonopolyGreenDark
import com.peppedess.monopolybank.ui.theme.PropertyColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    vm: BankViewModel,
    onPlayer: (Long) -> Unit,
    onTransfer: (fromId: Long?, toId: Long?) -> Unit,
    onHistory: () -> Unit,
    onNewGame: () -> Unit
) {
    val players by vm.players.collectAsState()
    val state by vm.state.collectAsState()
    val snackState = remember { SnackbarHostState() }
    val snackMsg by vm.snack.collectAsState()
    var showReset by remember { mutableStateOf(false) }
    var showParkingPicker by remember { mutableStateOf(false) }

    LaunchedEffect(snackMsg) {
        snackMsg?.let { snackState.showSnackbar(it); vm.consumeSnack() }
    }

    val active = players.filter { it.active }
    val ranked = remember(players) { players.sortedWith(compareByDescending { it.balance }) }
    val totalMoney = active.sumOf { it.balance }
    val leaderId = ranked.firstOrNull { it.active }?.id
    val pot = state?.parkingPot ?: 0

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) },
        floatingActionButton = {
            HorizontalFloatingToolbar(
                expanded = true,
                content = {
                    IconButton(onClick = { vm.undo() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, "Annulla ultima")
                    }
                    IconButton(onClick = { onTransfer(null, null) }) {
                        Icon(Icons.Default.CurrencyExchange, "Trasferimento")
                    }
                    IconButton(onClick = onHistory) {
                        Icon(Icons.Default.History, "Storico")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = pad,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Hero verde tabellone ──
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(MonopolyGreenDark, MonopolyGreen)),
                            RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "MONOPOLY BANK",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 3.sp,
                                    color = Color.White
                                )
                                Text(
                                    "${active.size} giocatori in gara",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            IconButton(onClick = { showReset = true }) {
                                Icon(Icons.Default.RestartAlt, "Nuova partita", tint = Color.White)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Denaro in circolazione
                            Column(
                                Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(14.dp)
                            ) {
                                Text("In circolazione", style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f))
                                val animTotal by animateIntAsState(totalMoney.toInt(), tween(600), label = "tot")
                                Text(animTotal.toLong().formatMoney(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black, color = Color.White)
                            }
                            // Pozzo Parcheggio — tappabile per riscuotere
                            if (state?.parkingEnabled == true) {
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                        .clickable(enabled = pot > 0) { showParkingPicker = true }
                                        .padding(14.dp)
                                ) {
                                    Text("🅿️ Parcheggio", style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f))
                                    val animPot by animateIntAsState(pot.toInt(), tween(600), label = "pot")
                                    Text(animPot.toLong().formatMoney(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black, color = Color.White)
                                    if (pot > 0) {
                                        Text("Tocca per riscuotere",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Classifica giocatori con azioni inline ──
            itemsIndexed(ranked, key = { _, p -> p.id }) { index, p ->
                var appeared by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 60L)
                    appeared = true
                }
                val enter by animateFloatAsState(
                    if (appeared) 1f else 0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "cascade"
                )
                val color = PropertyColors[p.colorIndex % PropertyColors.size]
                val share = if (totalMoney > 0 && p.active) p.balance.toFloat() / totalMoney else 0f

                Card(
                    onClick = { onPlayer(p.id) },
                    enabled = p.active,
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            translationY = (1f - enter) * 60f
                            alpha = enter
                            scaleX = 0.95f + 0.05f * enter
                            scaleY = 0.95f + 0.05f * enter
                        }
                ) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(color)
                        )
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box {
                                    PlayerAvatar(p.token, color, 56)
                                    if (p.id == leaderId && p.active) {
                                        Text("👑", fontSize = 20.sp,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .graphicsLayer {
                                                    translationX = 8f; translationY = -12f
                                                })
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        p.name + if (!p.active) "  💀" else "",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.alpha(if (p.active) 1f else 0.4f)
                                    )
                                    val animatedBalance by animateIntAsState(
                                        p.balance.toInt(), tween(700), label = "bal"
                                    )
                                    Text(
                                        animatedBalance.toLong().formatMoney(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (p.balance < 0) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.primary
                                    )
                                }
                                Box(
                                    Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${index + 1}°", style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearWavyProgressIndicator(
                                progress = { share },
                                modifier = Modifier.fillMaxWidth(),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            if (p.active) {
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = { vm.passGo(p.id) },
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("➡️ VIA", maxLines = 1) }
                                    FilledTonalButton(
                                        onClick = { onTransfer(p.id, null) },
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("💸 Paga", maxLines = 1) }
                                    FilledTonalButton(
                                        onClick = { onTransfer(SpecialIds.BANK, p.id) },
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("💰 Incassa", maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
    }

    // ── Dialog: chi riscuote il Parcheggio? ──
    if (showParkingPicker) {
        AlertDialog(
            onDismissRequest = { showParkingPicker = false },
            title = { Text("Chi riscuote ${pot.formatMoney()}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    active.forEach { p ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showParkingPicker = false
                                    vm.collectParking(p.id)
                                }
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
                TextButton(onClick = { showParkingPicker = false }) { Text("Annulla") }
            }
        )
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Nuova partita?") },
            text = { Text("La partita corrente e tutte le transazioni verranno cancellate.") },
            confirmButton = {
                TextButton(onClick = { showReset = false; onNewGame() }) {
                    Text("Nuova partita", color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Annulla") }
            }
        )
    }
}
