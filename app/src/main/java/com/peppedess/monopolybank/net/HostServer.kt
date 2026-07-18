package com.peppedess.monopolybank.net

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.peppedess.monopolybank.data.BankRepository
import com.peppedess.monopolybank.data.SpecialIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Il telefono host è la Banca autorevole: possiede il database Room,
 * pubblica il tavolo via NSD e trasmette lo stato a ogni variazione.
 */
class HostServer(
    private val repo: BankRepository,
    private val scope: CoroutineScope,
    private val context: Context
) {
    val running = MutableStateFlow(false)
    val clientCount = MutableStateFlow(0)

    private var serverSocket: ServerSocket? = null
    private val clientWriters = CopyOnWriteArrayList<BufferedWriter>()
    private val jobs = mutableListOf<Job>()
    private var regListener: NsdManager.RegistrationListener? = null
    @Volatile private var lastState: String = ""

    fun start() {
        if (running.value) return
        try {
            val ss = ServerSocket(NetProtocol.PORT)
            serverSocket = ss
            running.value = true
            registerNsd()

            // Broadcast dello stato a ogni variazione del DB
            jobs += scope.launch {
                combine(repo.players, repo.state, repo.txns) { p, s, t ->
                    buildStateMessage(p, s, t)
                }.collect { msg ->
                    lastState = msg
                    broadcast(msg)
                }
            }

            // Accetta connessioni
            jobs += scope.launch(Dispatchers.IO) {
                while (running.value) {
                    val socket = try { ss.accept() } catch (e: Exception) { break }
                    handleClient(socket)
                }
            }
        } catch (e: Exception) {
            stop()
        }
    }

    fun stop() {
        running.value = false
        unregisterNsd()
        jobs.forEach { it.cancel() }
        jobs.clear()
        clientWriters.forEach { runCatching { it.close() } }
        clientWriters.clear()
        clientCount.value = 0
        runCatching { serverSocket?.close() }
        serverSocket = null
    }

    private fun handleClient(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            clientWriters.add(writer)
            clientCount.value = clientWriters.size
            // Stato immediato al nuovo arrivato
            runCatching {
                if (lastState.isNotEmpty()) {
                    writer.write(lastState); writer.newLine(); writer.flush()
                }
            }
            try {
                while (true) {
                    val line = reader.readLine() ?: break
                    handleAction(line)
                }
            } catch (_: Exception) {
            } finally {
                clientWriters.remove(writer)
                clientCount.value = clientWriters.size
                runCatching { socket.close() }
            }
        }
    }

    private suspend fun handleAction(line: String) {
        val json = runCatching { JSONObject(line) }.getOrNull() ?: return
        if (json.optString("type") != "action") return
        when (json.optString("action")) {
            "transfer" -> repo.transfer(
                json.getLong("from"), json.getLong("to"),
                json.getLong("amount"), json.optString("note", "Trasferimento")
            )
            "passGo" -> {
                val go = repo.stateOnce()?.goAmount ?: 200L
                repo.transfer(SpecialIds.BANK, json.getLong("player"), go, "Passaggio dal VIA ➡️")
            }
            "collectParking" -> repo.collectParking(json.getLong("player"))
        }
    }

    private fun broadcast(msg: String) {
        scope.launch(Dispatchers.IO) {
            val dead = mutableListOf<BufferedWriter>()
            clientWriters.forEach { w ->
                try {
                    synchronized(w) { w.write(msg); w.newLine(); w.flush() }
                } catch (e: Exception) {
                    dead.add(w)
                }
            }
            if (dead.isNotEmpty()) {
                clientWriters.removeAll(dead.toSet())
                clientCount.value = clientWriters.size
            }
        }
    }

    private fun registerNsd() {
        val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        val info = NsdServiceInfo().apply {
            serviceName = "${NetProtocol.SERVICE_NAME}-${android.os.Build.MODEL}".take(60)
            serviceType = NetProtocol.SERVICE_TYPE
            port = NetProtocol.PORT
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(i: NsdServiceInfo) {}
            override fun onRegistrationFailed(i: NsdServiceInfo, e: Int) {}
            override fun onServiceUnregistered(i: NsdServiceInfo) {}
            override fun onUnregistrationFailed(i: NsdServiceInfo, e: Int) {}
        }
        regListener = listener
        runCatching { nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener) }
    }

    private fun unregisterNsd() {
        val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        regListener?.let { runCatching { nsd.unregisterService(it) } }
        regListener = null
    }

    companion object {
        /** IP locale del telefono host, per il join manuale */
        fun localIp(): String? = runCatching {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { !it.isLoopbackAddress && it is Inet4Address && it.isSiteLocalAddress }
                ?.hostAddress
        }.getOrNull()
    }
}
