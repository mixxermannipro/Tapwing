# Tapwing

Tapwing ist eine freie Android-Tastatur im Stil von Gboard mit eigenen
KI-Funktionen. Datenschutz ist Kernfunktion: Die Tastatur sammelt keine
Eingaben und sendet nichts ins Netz.

## Struktur

Annahme, an den tatsaechlichen Stand anpassen, sobald Code liegt:

- `app/` enthaelt das Android-Modul (Kotlin).
- `docs/` enthaelt Architektur, Arbeitsstand und Entscheidungen.

## Befehle

Annahmen, beim ersten Lauf pruefen und hier korrigieren:

- Bauen: `./gradlew assembleDebug`
- Tests: `./gradlew test`
- Lint: `./gradlew lint`

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
