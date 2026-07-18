# 🎩 Monopoly Bank

Banca digitale per Monopoly — Kotlin · Jetpack Compose · Material 3 Expressive.

## Funzionalità
- Giocatori con segnalini emoji e colori dei gruppi proprietà, classifica live con 👑
- Trasferimenti tra giocatori, Banca e Parcheggio Gratuito, banconote tap-to-add
- Passaggio dal VIA, tasse, cauzione, bancarotta, undo, storico partita
- Grafico canvas dell'andamento saldo per giocatore
- Room DB persistente, tema chiaro/scuro, palette Monopoly

## 📡 Multiplayer locale (Wi-Fi)
- Un telefono apre il **tavolo** (icona Wi-Fi nella Home): diventa la Banca autorevole
- Gli altri installano la stessa app → **"Unisciti a un tavolo"** → scoperta automatica
  via NSD/mDNS sulla rete locale (o IP manuale mostrato sull'host)
- Ogni giocatore sceglie chi è e opera dal proprio telefono: VIA, Paga, Incassa,
  riscossione Parcheggio — tutto sincronizzato in tempo reale via TCP (porta 8765)
- Nessun server esterno, nessuna dipendenza aggiuntiva

Build automatica APK via GitHub Actions. Keystore persistente `monopolybank.keystore`
per aggiornamenti sopra installazioni precedenti.
