package com.peppedess.monopolybank.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.peppedess.monopolybank.data.BankRepository
import com.peppedess.monopolybank.data.GameState
import com.peppedess.monopolybank.data.Player
import com.peppedess.monopolybank.data.SpecialIds
import com.peppedess.monopolybank.data.Txn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BankViewModel(val repo: BankRepository) : ViewModel() {

    val players: StateFlow<List<Player>> =
        repo.players.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val txns: StateFlow<List<Txn>> =
        repo.txns.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val state: StateFlow<GameState?> =
        repo.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** null = caricamento, true = partita esistente, false = nessuna partita */
    val hasGame: StateFlow<Boolean?> =
        repo.state.map { it != null }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _snack = MutableStateFlow<String?>(null)
    val snack: StateFlow<String?> = _snack
    fun consumeSnack() { _snack.value = null }
    private fun toast(msg: String) { _snack.value = msg }

    fun newGame(players: List<Triple<String, String, Int>>, startBalance: Long, goAmount: Long, parking: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.newGame(players, startBalance, goAmount, parking)
            onDone()
        }
    }

    fun transfer(fromId: Long, toId: Long, amount: Long, note: String) {
        viewModelScope.launch {
            repo.transfer(fromId, toId, amount, note)
            toast("$note · ${amount.formatMoney()}")
        }
    }

    fun passGo(playerId: Long) {
        viewModelScope.launch {
            val go = state.value?.goAmount ?: 200
            repo.transfer(SpecialIds.BANK, playerId, go, "Passaggio dal VIA ➡️")
        }
    }

    fun payTax(playerId: Long, amount: Long, note: String) {
        viewModelScope.launch {
            val parkingOn = state.value?.parkingEnabled ?: true
            val dest = if (parkingOn) SpecialIds.PARKING else SpecialIds.BANK
            repo.transfer(playerId, dest, amount, note)
        }
    }

    fun collectParking(playerId: Long) {
        viewModelScope.launch { repo.collectParking(playerId) }
    }

    fun undo() {
        viewModelScope.launch {
            val t = repo.undoLast()
            toast(if (t != null) "Annullata: ${t.note}" else "Niente da annullare")
        }
    }

    fun bankrupt(playerId: Long) {
        viewModelScope.launch { repo.bankrupt(playerId) }
    }

    companion object {
        fun factory(repo: BankRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = BankViewModel(repo) as T
        }
    }
}

fun Long.formatMoney(): String {
    val s = kotlin.math.abs(this).toString()
    val sb = StringBuilder()
    s.reversed().forEachIndexed { i, c ->
        if (i > 0 && i % 3 == 0) sb.append('.')
        sb.append(c)
    }
    return (if (this < 0) "-M " else "M ") + sb.reverse().toString()
}
