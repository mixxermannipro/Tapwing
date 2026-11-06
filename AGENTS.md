# Tapwing

Tapwing ist eine freie Android-Tastatur im Stil von Gboard mit eigenen
KI-Funktionen. Datenschutz ist Kernfunktion: Die Tastatur sammelt keine
Eingaben und sendet nichts ins Netz.

## Struktur

Tapwing basiert auf HeliBoard 4.2 und unterstützt Android 10 und neuer.
Java/Kotlin steuern die Eingabe, C++ enthält die lokale Wörterbuchsuche.

- Beginne mit [docs/PROGRESS.md](docs/PROGRESS.md) und [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
- `app/src/main/java/helium314/keyboard` enthält Tastatur, Einstellungen und Eingabelogik.
- `app/src/main/jni` enthält den Wörterbuchcode.
- `dictionaries/sources` enthält Quellwortlisten für Deutsch und Englisch.
- `tools/dev` enthält die Anleitung für eine neue Entwicklungsumgebung.

## Befehle

Vollständiges JDK 17, Android SDK 37.2, Build-Tools 36.0.0 und NDK
28.2.13676358 verwenden. Lokale Pfade stehen nur in Umgebungsvariablen
oder der ignorierten `local.properties`.

- Bauen: `./gradlew :app:assembleDebug :app:assembleRelease`
- Tests: `./gradlew :app:testDebugUnitTest`
- Lint: `./gradlew :app:lintDebug`
- Wörterbücher: `python3 tools/generate-swipe-lexicons.py --check`
- Änderungen: `git diff --check`

Testergebnisse und offene Aufgaben in `docs/PROGRESS.md` festhalten.
Gerät, Emulator und lokale Tests getrennt ausweisen.

## Regeln

- Der Kern der Tastatur (InputMethodService und Verarbeitung) kommt ohne
  die Berechtigung `android.permission.INTERNET` aus. Netzwerkzugriff ist
  nur einem klar abgegrenzten, freiwilligen und standardmaessig
  deaktivierten KI-Modul erlaubt.
- Passwortfelder (`TYPE_TEXT_VARIATION_PASSWORD` und verwandte Typen) sind
  von jeder KI-Verarbeitung, jedem Lernen, jedem Verlauf und jedem Logging
  ausgeschlossen.
- Keine Telemetrie, keine Analyse- oder Werbedienste, keine Abhaengigkeit
  mit eigener Datensammlung.
- Schluessel und Zugangsdaten gehoeren niemals in den Code oder nach Git.
  Lokale Entwicklung nutzt `local.properties` oder Umgebungsvariablen.
- Kein Code aus fremden Projekten uebernehmen, ohne die Lizenz zu pruefen
  und in `docs/THIRD_PARTY_LICENSES.md` zu dokumentieren. Hinweis: Das
  bekannte OpenBoard (openboard-team) steht unter der GPL-3.0.
- Arbeite auf einem eigenen Zweig auf Basis von `main`. Pushe nie auf
  `main` und verwende keine Force-Pushes. Jede Aenderung kommt als Pull
  Request. Fremde oder laufende Branches anderer Assistenten nicht
  anfassen.
- Halte README und docs bei sichtbaren Aenderungen aktuell. Kennzeichne
  ungepruefte Annahmen ausdruecklich.
