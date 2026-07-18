package com.peppedess.monopolybank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peppedess.monopolybank.data.SpecialIds
import com.peppedess.monopolybank.data.Txn
import com.peppedess.monopolybank.ui.BankViewModel
import com.peppedess.monopolybank.ui.components.BalanceChart
import com.peppedess.monopolybank.ui.components.PlayerAvatar
import com.peppedess.monopolybank.ui.formatMoney
import com.peppedess.monopolybank.ui.theme.PropertyColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerDetailScreen(vm: BankViewModel, playerId: Long, onBack: () -> Unit) {
    val player by remember(playerId) { vm.repo.player(playerId) }.collectAsStateWithLifecycle(null)
    val playerTxns by remember(playerId) { vm.repo.txnsFor(playerId) }.collectAsStateWithLifecycle(emptyList())
    val players by vm.players.collectAsState()
    val state by vm.state.collectAsState()
    var showBankrupt by remember { mutableStateOf(false) }

    val p = player
    if (p == null) {
        Scaffold { pad ->
            androidx.compose.foundation.layout.Box(
                Modifier.fillMaxSize().padding(pad),
                contentAlignment = Alignment.Center
            ) { LoadingIndicator() }
        }
        return
    }

    val color = PropertyColors[p.colorIndex % PropertyColors.size]
    val nameOf: (Long) -> String = { id ->
        when (id) {
            SpecialIds.BANK -> "Banca 🏦"
            SpecialIds.PARKING -> "Parcheggio 🅿️"
            else -> players.find { it.id == id }?.let { "${it.token} ${it.name}" } ?: "?"
        }
    }

    // Ricostruisce lo storico saldo dal saldo attuale, all'indietro
    val history = remember(playerTxns, p.balance) {
        val deltas = playerTxns.map { t ->
            var d = 0L
            if (t.toId == playerId) d += t.amount
            if (t.fromId == playerId) d -= t.amount
            d
        }
        val values = ArrayList<Long>(deltas.size + 1)
        var bal = p.balance
        values.add(bal)
        deltas.forEach { d -> bal -= d; values.add(bal) }
        values.reversed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlayerAvatar(p.token, color, 36)
                        Spacer(Modifier.width(10.dp))
                        Text(p.name, fontWeight = FontWeight.Black)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── Saldo + grafico ──
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Saldo attuale", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            p.balance.formatMoney(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = if (p.balance < 0) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        BalanceChart(
                            values = history,
                            lineColor = color,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }

            // ── Azioni rapide ──
            item {
                Text("Azioni rapide", style = MaterialTheme.typography.titleMedium)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val go = state?.goAmount ?: 200L
                    val quick = buildList {
                        add("➡️ VIA +${go}" to { vm.passGo(p.id) })
                        add("💸 Tassa patrimoniale 100" to { vm.payTax(p.id, 100, "Tassa patrimoniale 💸") })
                        add("🧾 Tassa di lusso 200" to { vm.payTax(p.id, 200, "Tassa di lusso 🧾") })
                        add("🚔 Cauzione prigione 50" to { vm.payTax(p.id, 50, "Uscita di prigione 🚔") })
                        if ((state?.parkingEnabled == true) && (state?.parkingPot ?: 0) > 0) {
                            add("🅿️ Riscuoti Parcheggio" to { vm.collectParking(p.id) })
                        }
                    }
                    items(quick) { (label, action) ->
                        AssistChip(onClick = action, label = { Text(label) })
                    }
                }
            }

            // ── Storico transazioni ──
            item {
                Text("Movimenti (${playerTxns.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(playerTxns, key = { it.id }) { t ->
                TxnRow(t, playerId, nameOf)
            }

            // ── Bancarotta ──
            item {
                TextButton(
                    onClick = { showBankrupt = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💀 Dichiara bancarotta", color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold)
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showBankrupt) {
        AlertDialog(
            onDismissRequest = { showBankrupt = false },
            title = { Text("Bancarotta di ${p.name}?") },
            text = { Text("Il saldo residuo tornerà alla Banca e il giocatore verrà eliminato dalla partita.") },
            confirmButton = {
                TextButton(onClick = { showBankrupt = false; vm.bankrupt(p.id); onBack() }) {
                    Text("Conferma", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showBankrupt = false }) { Text("Annulla") } }
        )
    }
}

@Composable
private fun TxnRow(t: Txn, playerId: Long, nameOf: (Long) -> String) {
    val incoming = t.toId == playerId
    val sign = if (incoming) "+" else "−"
    val other = if (incoming) nameOf(t.fromId) else nameOf(t.toId)
    val time = remember(t.id) {
        SimpleDateFormat("HH:mm", Locale.ITALY).format(Date(t.timestamp))
    }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(t.note, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                Text(
                    (if (incoming) "da " else "a ") + other + "  ·  " + time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$sign${t.amount.formatMoney()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (incoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }
    }
}
