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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peppedess.monopolybank.data.SpecialIds
import com.peppedess.monopolybank.ui.BankViewModel
import com.peppedess.monopolybank.ui.components.PlayerAvatar
import com.peppedess.monopolybank.ui.formatMoney
import com.peppedess.monopolybank.ui.theme.PropertyColors
import com.peppedess.monopolybank.ui.theme.onProperty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    vm: BankViewModel,
    onPlayer: (Long) -> Unit,
    onTransfer: () -> Unit,
    onNewGame: () -> Unit
) {
    val players by vm.players.collectAsState()
    val state by vm.state.collectAsState()
    val snackState = remember { SnackbarHostState() }
    val snackMsg by vm.snack.collectAsState()
    var showReset by remember { mutableStateOf(false) }

    LaunchedEffect(snackMsg) {
        snackMsg?.let { snackState.showSnackbar(it); vm.consumeSnack() }
    }

    val totalMoney = players.filter { it.active }.sumOf { it.balance }

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MONOPOLY BANK", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(
                            "In circolazione: ${totalMoney.formatMoney()}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showReset = true }) {
                        Icon(Icons.Default.RestartAlt, "Nuova partita")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            HorizontalFloatingToolbar(
                expanded = true,
                content = {
                    IconButton(onClick = { vm.undo() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, "Annulla ultima")
                    }
                    IconButton(onClick = onTransfer) {
                        Icon(Icons.Default.CurrencyExchange, "Trasferimento")
                    }
                    IconButton(onClick = onTransfer) {
                        Icon(Icons.Default.AccountBalance, "Banca")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── Pozzo Parcheggio Gratuito ──
            if (state?.parkingEnabled == true) {
                item {
                    val pot = state?.parkingPot ?: 0
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🅿️", fontSize = 32.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Parcheggio Gratuito",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer)
                                val animatedPot by animateIntAsState(pot.toInt(), tween(600), label = "pot")
                                Text(animatedPot.toLong().formatMoney(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                            Text("Tocca un giocatore\nper riscuotere",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }

            // ── Giocatori (cascade sfalsata) ──
            itemsIndexed(players, key = { _, p -> p.id }) { index, p ->
                var appeared by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 70L)
                    appeared = true
                }
                val enter by animateFloatAsState(
                    if (appeared) 1f else 0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "cascade"
                )
                val color = PropertyColors[p.colorIndex % PropertyColors.size]
                val share = if (totalMoney > 0) p.balance.toFloat() / totalMoney else 0f

                Card(
                    onClick = { onPlayer(p.id) },
                    enabled = p.active,
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = (1f - enter) * 60f
                            alpha = enter
                            scaleX = 0.95f + 0.05f * enter
                            scaleY = 0.95f + 0.05f * enter
                        }
                ) {
                    Column {
                        // banda colore proprietà in alto, come le carte Monopoly
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(color)
                        )
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayerAvatar(p.token, color, 52)
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
                                Spacer(Modifier.height(6.dp))
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
            }

            // ── Passa dal VIA rapido ──
            item {
                Text(
                    "Passaggio rapido dal VIA  ➡️  +${(state?.goAmount ?: 200L).formatMoney()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    players.filter { it.active }.forEach { p ->
                        Box(Modifier.clickable { vm.passGo(p.id) }) {
                            PlayerAvatar(p.token, PropertyColors[p.colorIndex % PropertyColors.size], 48)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
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
