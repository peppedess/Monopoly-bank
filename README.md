# 🎩 Monopoly Bank

Banca digitale per Monopoly — Kotlin · Jetpack Compose · Material 3 Expressive.

- Gestione giocatori con segnalini emoji e colori dei gruppi proprietà
- Trasferimenti tra giocatori, Banca e Parcheggio Gratuito
- Banconote tap-to-add, passaggio dal VIA, tasse, cauzione, bancarotta
- Grafico canvas dell'andamento saldo per giocatore
- Undo dell'ultima transazione
- Room DB persistente, tema chiaro/scuro, palette Monopoly

Build automatica APK via GitHub Actions (`.github/workflows/build.yml`).
Per firma persistente (aggiornamenti sopra installazioni precedenti) genera una
volta `monopolybank.keystore` nella root e committalo (vedi istruzioni).
