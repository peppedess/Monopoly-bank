package com.peppedess.monopolybank.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peppedess.monopolybank.data.Player
import com.peppedess.monopolybank.data.SpecialIds
import com.peppedess.monopolybank.ui.BankViewModel
import com.peppedess.monopolybank.ui.components.PlayerAvatar
import com.peppedess.monopolybank.ui.formatMoney
import com.peppedess.monopolybank.ui.theme.MonopolyGold
import com.peppedess.monopolybank.ui.theme.MonopolyInk
import com.peppedess.monopolybank.ui.theme.PropertyColors

/** Banconote Monopoly: valore → colore */
private val Banknotes = listOf(
    1L to Color(0xFFF5F5F5),
    5L to Color(0xFFF8BBD0),
    10L to Color(0xFFFFF59D),
    20L to Color(0xFFA5D6A7),
    50L to Color(0xFF90CAF9),
    100L to Color(0xFFFFCC80),
    500L to MonopolyGold
)

private data class Party(val id: Long, val label: String, val token: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(vm: BankViewModel, onBack: () -> Unit) {
    val players by vm.players.collectAsState()
    val state by vm.state.collectAsState()

    val parties = remember(players, state) {
        buildList {
            add(Party(SpecialIds.BANK, "Banca", "🏦", Color(0xFF0B6E3A)))
            if (state?.parkingEnabled == true) add(Party(SpecialIds.PARKING, "Parcheggio", "🅿️", Color(0xFFE0A82E)))
            players.filter { it.active }.forEach {
                add(Party(it.id, it.name, it.token, PropertyColors[it.colorIndex % PropertyColors.size]))
            }
        }
    }

    var fromId by remember { mutableStateOf(SpecialIds.BANK) }
    var toId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf(0L) }
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trasferimento", fontWeight = FontWeight.Black) },
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PartyPicker("Da", parties, fromId) { fromId = it }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward, null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.CenterHorizontally).size(28.dp)
            )
            PartyPicker("A", parties.filter { it.id != fromId }, toId ?: Long.MIN_VALUE) { toId = it }

            // ── Importo con banconote ──
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Importo", style = MaterialTheme.typography.titleMedium)
                    Text(
                        amount.formatMoney(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Tocca le banconote per aggiungerle, tieni conto tu del resto 😉",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Banknotes) { (value, color) ->
                            Box(
                                Modifier
                                    .size(width = 88.dp, height = 48.dp)
                                    .background(color, RoundedCornerShape(8.dp))
                                    .border(2.dp, MonopolyInk.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { amount += value },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("M $value", fontWeight = FontWeight.Black, color = MonopolyInk, fontSize = 16.sp)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("×2" to { amount *= 2 }, "÷2" to { amount /= 2 }, "C" to { amount = 0L }).forEach { (lbl, op) ->
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                                    .clickable { op() },
                                contentAlignment = Alignment.Center
                            ) { Text(lbl, fontWeight = FontWeight.Bold) }
                        }
                        OutlinedTextField(
                            value = if (amount == 0L) "" else amount.toString(),
                            onValueChange = { amount = it.filter(Char::isDigit).toLongOrNull() ?: 0L },
                            label = { Text("Manuale") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Causale (es. Affitto Parco della Vittoria)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val to = toId ?: return@Button
                    vm.transfer(fromId, to, amount, note.ifBlank { "Trasferimento" })
                    onBack()
                },
                enabled = toId != null && amount > 0,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("CONFERMA  ·  ${amount.formatMoney()}", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PartyPicker(label: String, parties: List<Party>, selectedId: Long, onSelect: (Long) -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(parties) { p ->
                    val selected = p.id == selectedId
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelect(p.id) }
                            .padding(8.dp)
                    ) {
                        PlayerAvatar(p.token, p.color, 48)
                        Spacer(Modifier.height(4.dp))
                        Text(p.label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
            }
        }
    }
}
