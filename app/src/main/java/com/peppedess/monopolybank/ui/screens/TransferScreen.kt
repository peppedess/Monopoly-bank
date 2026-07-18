package com.peppedess.monopolybank.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private val QuickNotes = listOf("Affitto 🏠", "Proprietà 📜", "Casa 🏡", "Albergo 🏨", "Imprevisto ❓", "Probabilità 🎁")

private data class Party(val id: Long, val label: String, val token: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransferScreen(
    vm: BankViewModel,
    initialFrom: Long?,
    initialTo: Long?,
    onBack: () -> Unit
) {
    val players by vm.players.collectAsState()
    val state by vm.state.collectAsState()
    val recents by vm.recentAmounts.collectAsState()

    val parties = remember(players, state) {
        buildList {
            add(Party(SpecialIds.BANK, "Banca", "🏦", Color(0xFF0B6E3A)))
            if (state?.parkingEnabled == true) add(Party(SpecialIds.PARKING, "Parch.", "🅿️", Color(0xFFE0A82E)))
            players.filter { it.active }.forEach {
                add(Party(it.id, it.name, it.token, PropertyColors[it.colorIndex % PropertyColors.size]))
            }
        }
    }

    var fromId by remember { mutableStateOf(initialFrom ?: SpecialIds.BANK) }
    var toId by remember { mutableStateOf(initialTo) }
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
        },
        // ── Conferma GIGANTE sempre sotto il pollice ──
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = {
                        val to = toId ?: return@Button
                        vm.transfer(fromId, to, amount, note.ifBlank { "Trasferimento" })
                        onBack()
                    },
                    enabled = toId != null && amount > 0,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(72.dp)
                ) {
                    Text(
                        if (amount > 0) "CONFERMA  ${amount.formatMoney()}" else "SCEGLI IMPORTO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(2.dp))

            // ── Da → A compatti ──
            PartyPicker("Chi paga", parties, fromId) { fromId = it; if (toId == it) toId = null }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    val t = toId
                    if (t != null) { toId = fromId; fromId = t }
                }) { Icon(Icons.Default.SwapVert, "Inverti", tint = MaterialTheme.colorScheme.secondary) }
            }
            PartyPicker("Chi riceve", parties.filter { it.id != fromId }, toId ?: Long.MIN_VALUE) { toId = it }

            // ── Importo enorme ──
            val animAmount by animateIntAsState(amount.toInt(), tween(200), label = "amt")
            Text(
                animAmount.toLong().formatMoney(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = if (amount > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Ultimi importi: un tap e sei pronto ──
            if (recents.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recents.forEach { r ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(14.dp))
                                .clickable { amount = r },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("↺ ${r.formatMoney()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                maxLines = 1)
                        }
                    }
                }
            }

            // ── Griglia banconote: tutte visibili, grandi ──
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4,
                modifier = Modifier.fillMaxWidth()
            ) {
                Banknotes.forEach { (value, color) ->
                    Banknote(
                        value, color,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) { amount += value }
                }
                // C nella griglia, stessa mano
                Box(
                    Modifier
                        .weight(1f)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(10.dp))
                        .clickable { amount = 0L },
                    contentAlignment = Alignment.Center
                ) {
                    Text("C", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            // ── ×2 ÷2 + manuale ──
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf<Pair<String, () -> Unit>>(
                    "×2" to { amount *= 2 },
                    "÷2" to { amount /= 2 }
                ).forEach { (lbl, op) ->
                    Box(
                        Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                            .clickable { op() },
                        contentAlignment = Alignment.Center
                    ) { Text(lbl, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) }
                }
                OutlinedTextField(
                    value = if (amount == 0L) "" else amount.toString(),
                    onValueChange = { amount = it.filter(Char::isDigit).toLongOrNull() ?: 0L },
                    label = { Text("Digita importo") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // ── Causali rapide ──
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(QuickNotes) { q ->
                    Box(
                        Modifier
                            .background(
                                if (note == q) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { note = if (note == q) "" else q }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) { Text(q, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Banknote(value: Long, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, spring(), label = "note")
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(color, RoundedCornerShape(10.dp))
            .border(2.dp, MonopolyInk.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("M $value", fontWeight = FontWeight.Black, color = MonopolyInk, fontSize = 17.sp)
    }
}

@Composable
private fun PartyPicker(label: String, parties: List<Party>, selectedId: Long, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(parties) { p ->
                val selected = p.id == selectedId
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { onSelect(p.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    PlayerAvatar(p.token, p.color, 52)
                    Spacer(Modifier.height(4.dp))
                    Text(p.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                        maxLines = 1)
                }
            }
        }
    }
}
