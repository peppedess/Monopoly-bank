package com.peppedess.monopolybank.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import android.content.Context
import kotlinx.coroutines.flow.Flow

/** ID speciali per la Banca e il Parcheggio Gratuito */
object SpecialIds {
    const val BANK = -1L
    const val PARKING = -2L
}

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val token: String,        // emoji segnalino
    val colorIndex: Int,      // indice colore gruppo proprietà Monopoly
    val balance: Long,
    val active: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "txns")
data class Txn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val fromId: Long,         // SpecialIds.BANK / PARKING oppure id giocatore
    val toId: Long,
    val amount: Long,
    val note: String
)

@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey val id: Int = 1,
    val parkingPot: Long = 0,
    val parkingEnabled: Boolean = true,
    val goAmount: Long = 200,
    val startedAt: Long = System.currentTimeMillis()
)

@Dao
interface BankDao {
    @Query("SELECT * FROM players ORDER BY sortOrder, id")
    fun players(): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE id = :id")
    fun player(id: Long): Flow<Player?>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun playerOnce(id: Long): Player?

    @Insert
    suspend fun insertPlayer(p: Player): Long

    @Query("UPDATE players SET balance = balance + :delta WHERE id = :id")
    suspend fun addBalance(id: Long, delta: Long)

    @Query("UPDATE players SET active = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("SELECT * FROM txns ORDER BY id DESC")
    fun txns(): Flow<List<Txn>>

    @Query("SELECT * FROM txns WHERE fromId = :id OR toId = :id ORDER BY id DESC")
    fun txnsFor(id: Long): Flow<List<Txn>>

    @Query("SELECT * FROM txns ORDER BY id DESC LIMIT 1")
    suspend fun lastTxn(): Txn?

    @Insert
    suspend fun insertTxn(t: Txn): Long

    @Query("DELETE FROM txns WHERE id = :id")
    suspend fun deleteTxn(id: Long)

    @Query("SELECT * FROM game_state WHERE id = 1")
    fun state(): Flow<GameState?>

    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun stateOnce(): GameState?

    @Insert
    suspend fun insertState(s: GameState)

    @Query("UPDATE game_state SET parkingPot = parkingPot + :delta WHERE id = 1")
    suspend fun addParking(delta: Long)

    @Query("UPDATE game_state SET parkingPot = 0 WHERE id = 1")
    suspend fun clearParking()

    @Query("DELETE FROM players")
    suspend fun clearPlayers()

    @Query("DELETE FROM txns")
    suspend fun clearTxns()

    @Query("DELETE FROM game_state")
    suspend fun clearState()

    @Transaction
    suspend fun resetGame() {
        clearPlayers(); clearTxns(); clearState()
    }
}

@Database(entities = [Player::class, Txn::class, GameState::class], version = 1, exportSchema = false)
abstract class BankDb : RoomDatabase() {
    abstract fun dao(): BankDao

    companion object {
        @Volatile private var instance: BankDb? = null
        fun get(context: Context): BankDb =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, BankDb::class.java, "monopoly_bank.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}

class BankRepository(private val dao: BankDao) {

    val players = dao.players()
    val txns = dao.txns()
    val state = dao.state()

    fun player(id: Long) = dao.player(id)
    fun txnsFor(id: Long) = dao.txnsFor(id)

    suspend fun newGame(names: List<Triple<String, String, Int>>, startBalance: Long, goAmount: Long, parkingEnabled: Boolean) {
        dao.resetGame()
        dao.insertState(GameState(parkingEnabled = parkingEnabled, goAmount = goAmount))
        names.forEachIndexed { i, (name, token, color) ->
            dao.insertPlayer(Player(name = name, token = token, colorIndex = color, balance = startBalance, sortOrder = i))
        }
    }

    /** Trasferimento universale: gestisce banca, parcheggio e giocatori */
    suspend fun transfer(fromId: Long, toId: Long, amount: Long, note: String) {
        if (amount <= 0) return
        when (fromId) {
            SpecialIds.BANK -> Unit
            SpecialIds.PARKING -> dao.addParking(-amount)
            else -> dao.addBalance(fromId, -amount)
        }
        when (toId) {
            SpecialIds.BANK -> Unit
            SpecialIds.PARKING -> dao.addParking(amount)
            else -> dao.addBalance(toId, amount)
        }
        dao.insertTxn(Txn(timestamp = System.currentTimeMillis(), fromId = fromId, toId = toId, amount = amount, note = note))
    }

    /** Annulla l'ultima transazione applicandola al contrario */
    suspend fun undoLast(): Txn? {
        val last = dao.lastTxn() ?: return null
        when (last.fromId) {
            SpecialIds.BANK -> Unit
            SpecialIds.PARKING -> dao.addParking(last.amount)
            else -> dao.addBalance(last.fromId, last.amount)
        }
        when (last.toId) {
            SpecialIds.BANK -> Unit
            SpecialIds.PARKING -> dao.addParking(-last.amount)
            else -> dao.addBalance(last.toId, -last.amount)
        }
        dao.deleteTxn(last.id)
        return last
    }

    suspend fun collectParking(playerId: Long) {
        val pot = dao.stateOnce()?.parkingPot ?: 0
        if (pot > 0) transfer(SpecialIds.PARKING, playerId, pot, "Parcheggio Gratuito 🅿️")
    }

    suspend fun bankrupt(playerId: Long) {
        val p = dao.playerOnce(playerId) ?: return
        if (p.balance > 0) transfer(playerId, SpecialIds.BANK, p.balance, "Bancarotta 💀")
        dao.setActive(playerId, false)
    }
}
