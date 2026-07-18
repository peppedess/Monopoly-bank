package com.peppedess.monopolybank.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peppedess.monopolybank.ui.BankViewModel
import com.peppedess.monopolybank.ui.components.PlayerAvatar
import com.peppedess.monopolybank.ui.theme.PropertyColors

private val Tokens = listOf("🎩", "🚗", "🐕", "👢", "🚢", "🐈", "🦖", "🛞", "🧵", "🏇")

data class DraftPlayer(val name: String, val token: String, val colorIndex: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(vm: BankViewModel, onStarted: () -> Unit) {
    val drafts = remember { mutableStateListOf<DraftPlayer>() }
    var name by remember { mutableStateOf("") }
    var tokenIdx by remember { mutableStateOf(0) }
    var colorIdx by remember { mutableStateOf(0) }
    var startBalance by remember { mutableStateOf("1500") }
    var goAmount by remember { mutableStateOf("200") }
    var parking by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MONOPOLY BANK", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("Nuova partita", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
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

            // ── Nuovo giocatore ──
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Aggiungi giocatore", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Text("Segnalino", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Tokens.size) { i ->
                                val selected = i == tokenIdx
                                Box(
                                    Modifier
                                        .size(48.dp)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainer,
                                            CircleShape
                                        )
                                        .border(
                                            width = if (selected) 2.dp else 0.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { tokenIdx = i },
                                    contentAlignment = Alignment.Center
                                ) { Text(Tokens[i], fontSize = 24.sp) }
                            }
                        }
                        Text("Colore proprietà", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PropertyColors.size) { i ->
                                val selected = i == colorIdx
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .background(PropertyColors[i], CircleShape)
                                        .border(
                                            width = if (selected) 3.dp else 0.dp,
                                            color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { colorIdx = i }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    drafts.add(DraftPlayer(name.trim(), Tokens[tokenIdx], colorIdx))
                                    name = ""
                                    tokenIdx = (tokenIdx + 1) % Tokens.size
                                    colorIdx = (colorIdx + 1) % PropertyColors.size
                                }
                            },
                            enabled = name.isNotBlank() && drafts.size < 8,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Aggiungi")
                        }
                    }
                }
            }

            // ── Lista giocatori ──
            items(drafts, key = { it.name + it.token }) { d ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(animationSpec = spring()) { it / 2 }
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayerAvatar(d.token, PropertyColors[d.colorIndex], 40)
                            Spacer(Modifier.width(12.dp))
                            Text(d.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { drafts.remove(d) }) {
                                Icon(Icons.Default.Delete, "Rimuovi", tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            // ── Impostazioni partita ──
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Regole", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startBalance,
                                onValueChange = { startBalance = it.filter(Char::isDigit) },
                                label = { Text("Saldo iniziale") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            OutlinedTextField(
                                value = goAmount,
                                onValueChange = { goAmount = it.filter(Char::isDigit) },
                                label = { Text("Premio VIA") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Parcheggio Gratuito 🅿️", style = MaterialTheme.typography.bodyLarge)
                                Text("Le tasse finiscono nel pozzo", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = parking, onCheckedChange = { parking = it })
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(1000L, 1500L, 2000L, 2500L)) { v ->
                                FilterChip(
                                    selected = startBalance == v.toString(),
                                    onClick = { startBalance = v.toString() },
                                    label = { Text("M $v") }
                                )
                            }
                        }
                    }
                }
            }

            // ── Avvia ──
            item {
                Button(
                    onClick = {
                        vm.newGame(
                            drafts.map { Triple(it.name, it.token, it.colorIndex) },
                            startBalance.toLongOrNull() ?: 1500L,
                            goAmount.toLongOrNull() ?: 200L,
                            parking
                        ) { onStarted() }
                    },
                    enabled = drafts.size >= 2,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("INIZIA LA PARTITA", letterSpacing = 1.sp, fontWeight = FontWeight.Black)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
