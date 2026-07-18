package com.peppedess.monopolybank.net

import com.peppedess.monopolybank.data.GameState
import com.peppedess.monopolybank.data.Player
import com.peppedess.monopolybank.data.Txn
import org.json.JSONArray
import org.json.JSONObject

/** Protocollo di rete: messaggi JSON delimitati da newline su TCP. */
object NetProtocol {
    const val PORT = 8765
    const val SERVICE_TYPE = "_monopolybank._tcp."
    const val SERVICE_NAME = "MonopolyBank"
}

// ─── Modelli lato client (stato remoto ricevuto dall'host) ───
data class RPlayer(
    val id: Long,
    val name: String,
    val token: String,
    val colorIndex: Int,
    val balance: Long,
    val active: Boolean
)

data class RTxn(
    val note: String,
    val fromId: Long,
    val toId: Long,
    val amount: Long,
    val timestamp: Long
)

data class RemoteState(
    val players: List<RPlayer> = emptyList(),
    val parkingPot: Long = 0,
    val parkingEnabled: Boolean = true,
    val goAmount: Long = 200,
    val txns: List<RTxn> = emptyList()
)

// ─── Serializzazione host → client ───
fun buildStateMessage(players: List<Player>, state: GameState?, txns: List<Txn>): String {
    val o = JSONObject()
    o.put("type", "state")
    o.put("parkingPot", state?.parkingPot ?: 0L)
    o.put("parkingEnabled", state?.parkingEnabled ?: true)
    o.put("goAmount", state?.goAmount ?: 200L)
    val pa = JSONArray()
    players.forEach { p ->
        pa.put(JSONObject().apply {
            put("id", p.id); put("name", p.name); put("token", p.token)
            put("color", p.colorIndex); put("balance", p.balance); put("active", p.active)
        })
    }
    o.put("players", pa)
    val ta = JSONArray()
    txns.take(40).forEach { t ->
        ta.put(JSONObject().apply {
            put("note", t.note); put("from", t.fromId); put("to", t.toId)
            put("amount", t.amount); put("ts", t.timestamp)
        })
    }
    o.put("txns", ta)
    return o.toString()
}

fun parseStateMessage(json: JSONObject): RemoteState {
    val players = buildList {
        val pa = json.optJSONArray("players") ?: JSONArray()
        for (i in 0 until pa.length()) {
            val p = pa.getJSONObject(i)
            add(
                RPlayer(
                    id = p.getLong("id"),
                    name = p.getString("name"),
                    token = p.getString("token"),
                    colorIndex = p.getInt("color"),
                    balance = p.getLong("balance"),
                    active = p.getBoolean("active")
                )
            )
        }
    }
    val txns = buildList {
        val ta = json.optJSONArray("txns") ?: JSONArray()
        for (i in 0 until ta.length()) {
            val t = ta.getJSONObject(i)
            add(
                RTxn(
                    note = t.getString("note"),
                    fromId = t.getLong("from"),
                    toId = t.getLong("to"),
                    amount = t.getLong("amount"),
                    timestamp = t.getLong("ts")
                )
            )
        }
    }
    return RemoteState(
        players = players,
        parkingPot = json.optLong("parkingPot", 0L),
        parkingEnabled = json.optBoolean("parkingEnabled", true),
        goAmount = json.optLong("goAmount", 200L),
        txns = txns
    )
}

// ─── Azioni client → host ───
fun actionTransfer(fromId: Long, toId: Long, amount: Long, note: String): String =
    JSONObject().apply {
        put("type", "action"); put("action", "transfer")
        put("from", fromId); put("to", toId); put("amount", amount); put("note", note)
    }.toString()

fun actionPassGo(playerId: Long): String =
    JSONObject().apply {
        put("type", "action"); put("action", "passGo"); put("player", playerId)
    }.toString()

fun actionCollectParking(playerId: Long): String =
    JSONObject().apply {
        put("type", "action"); put("action", "collectParking"); put("player", playerId)
    }.toString()
