package com.peppedess.monopolybank.net

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket

data class DiscoveredTable(val name: String, val host: String, val port: Int)

/** Scoperta automatica dei tavoli sulla rete locale via NSD/mDNS. */
class TableDiscovery(private val context: Context) {
    val tables = MutableStateFlow<List<DiscoveredTable>>(emptyList())
    private var listener: NsdManager.DiscoveryListener? = null

    fun start() {
        stop()
        val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        val l = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(t: String) {}
            override fun onDiscoveryStopped(t: String) {}
            override fun onStartDiscoveryFailed(t: String, e: Int) {}
            override fun onStopDiscoveryFailed(t: String, e: Int) {}
            override fun onServiceLost(i: NsdServiceInfo) {
                tables.value = tables.value.filterNot { it.name == i.serviceName }
            }
            override fun onServiceFound(i: NsdServiceInfo) {
                @Suppress("DEPRECATION")
                nsd.resolveService(i, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(s: NsdServiceInfo, e: Int) {}
                    override fun onServiceResolved(s: NsdServiceInfo) {
                        val host = s.host?.hostAddress ?: return
                        val entry = DiscoveredTable(s.serviceName, host, s.port)
                        if (tables.value.none { it.host == host && it.port == s.port }) {
                            tables.value = tables.value + entry
                        }
                    }
                })
            }
        }
        listener = l
        runCatching {
            nsd.discoverServices(NetProtocol.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, l)
        }
    }

    fun stop() {
        val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        listener?.let { runCatching { nsd.stopServiceDiscovery(it) } }
        listener = null
        tables.value = emptyList()
    }
}

/** Connessione TCP al telefono-Banca. Mantiene lo stato remoto aggiornato. */
class ClientConnection(private val scope: CoroutineScope) {

    val state = MutableStateFlow(RemoteState())
    val connected = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    val myPlayerId = MutableStateFlow<Long?>(null)

    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var readerJob: Job? = null

    fun connect(host: String, port: Int = NetProtocol.PORT) {
        disconnect()
        error.value = null
        readerJob = scope.launch(Dispatchers.IO) {
            try {
                val s = Socket()
                s.connect(InetSocketAddress(host, port), 4000)
                socket = s
                writer = BufferedWriter(OutputStreamWriter(s.getOutputStream()))
                connected.value = true
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                while (true) {
                    val line = reader.readLine() ?: break
                    val json = runCatching { JSONObject(line) }.getOrNull() ?: continue
                    if (json.optString("type") == "state") {
                        state.value = parseStateMessage(json)
                    }
                }
            } catch (e: Exception) {
                error.value = "Connessione non riuscita"
            } finally {
                connected.value = false
            }
        }
    }

    fun disconnect() {
        readerJob?.cancel(); readerJob = null
        runCatching { writer?.close() }
        runCatching { socket?.close() }
        writer = null; socket = null
        connected.value = false
        myPlayerId.value = null
        state.value = RemoteState()
    }

    private fun send(msg: String) {
        scope.launch(Dispatchers.IO) {
            try {
                writer?.let { w ->
                    synchronized(w) { w.write(msg); w.newLine(); w.flush() }
                }
            } catch (_: Exception) {
                connected.value = false
            }
        }
    }

    fun transfer(fromId: Long, toId: Long, amount: Long, note: String) =
        send(actionTransfer(fromId, toId, amount, note))

    fun passGo(playerId: Long) = send(actionPassGo(playerId))

    fun collectParking(playerId: Long) = send(actionCollectParking(playerId))
}
