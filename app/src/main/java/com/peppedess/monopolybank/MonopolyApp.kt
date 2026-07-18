package com.peppedess.monopolybank

import android.app.Application
import com.peppedess.monopolybank.data.BankDb
import com.peppedess.monopolybank.data.BankRepository
import com.peppedess.monopolybank.net.ClientConnection
import com.peppedess.monopolybank.net.HostServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MonopolyApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val repository: BankRepository by lazy { BankRepository(BankDb.get(this).dao()) }
    val hostServer: HostServer by lazy { HostServer(repository, appScope, this) }
    val clientConnection: ClientConnection by lazy { ClientConnection(appScope) }
}
