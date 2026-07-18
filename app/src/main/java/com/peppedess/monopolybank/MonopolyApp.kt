package com.peppedess.monopolybank

import android.app.Application
import com.peppedess.monopolybank.data.BankDb
import com.peppedess.monopolybank.data.BankRepository

class MonopolyApp : Application() {
    val repository: BankRepository by lazy { BankRepository(BankDb.get(this).dao()) }
}
