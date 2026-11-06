# Native settings from the supplied reference

> **For agentic workers:** Use superpowers:executing-plans; implement in this checkout and review the completed UI.

**Goal:** Tapwing-Einstellungen nach den sechs gelieferten PixelBoard-Bildern aufbauen.
**Architecture:** Vorhandene Compose-Navigation und echte SharedPreferences weiterverwenden. Gemeinsame Graphit-Farben, Kartengruppen, Seitenüberschriften und Schalter bilden die Grundlage für Startseite und Unterseiten.
**Tech stack:** Android Compose Material 3, vorhandene Kotlin-/Java-Eingabelogik, Robolectric und Compose-UI-Tests.
**Spec:** Sechs Nutzerbilder vom 6. November 2026, docs/PROGRESS.md und AGENTS.md.

## Vorgaben

- App-Name Tapwing; keine PixelBoard-Marken oder Grafiken übernehmen.
- Dunkler Hintergrund, graue Kartengruppen, blaue Akzente, runde Zurück-Schaltfläche und lesbare deutsche Texte.
- Startseite in vier Gruppen: Sprachen/Einstellungen/Design; Korrekturen/Wischen/Spracheingabe/Textfelder/Emojis; Zwischenablage/Wörterbuch; Datenschutz/Info/Erweitert/Updates.
- Unterseiten „Einstellungen“, „Korrekturen & Vorschläge“, „Zwischenablage“ und „Erweiterte Einstellungen“ an den gelieferten Bildern ausrichten.
- Vorhandene Einstellungen, Suchfunktion und Navigationsziele erreichbar halten. Echte Schalter müssen ihre Werte speichern und nach externen Änderungen aktualisieren.
- Geplante Funktionen klar als noch nicht verfügbar darstellen; keine wirkungslosen aktiven Schalter. KI/Diktat bleiben aus, Passwortschutz und fehlende Netzwerk-/Kontaktberechtigung erhalten.
- Lichtmodus und große Schrift weiterhin unterstützen; Android-Zustandsleiste nicht nachzeichnen.

## Task 1: Gemeinsame Oberfläche

Files: Theme.kt, SettingsLayout.kt, SearchScreen.kt, preferences/Preference.kt, preferences/SwitchPreference.kt, app/build.gradle.kts, SettingsUiTest.kt.

- [x] Compose-UI-Prüfung für gespeicherten Schalterzustand und externe Präferenzänderung schreiben und am aktuellen Code fehlschlagen sehen.
- [x] Gemeinsame Karten, Abschnittsüberschriften, Seitentitel und Schalter umsetzen; Radio-/Schalter-Semantik und große Schrift erhalten.
- [x] Schalterzustand über SharedPreferences beobachten, einen einzigen bedienbaren Schalter pro Zeile anbieten und den UI-Test grün ausführen.

## Task 2: Startseite und Referenz-Unterseiten

Files: MainSettingsScreen.kt, SettingsNavHost.kt, PreferencesScreen.kt, TextCorrectionScreen.kt, ReferenceSettingsScreens.kt, settings reference string resources DE/EN.

- [x] Tests für neue Root-Navigation und für klar deaktivierte geplante Funktionen schreiben.
- [x] Vier Hauptgruppen und alle Ziele implementieren; Zwischenablage aus dem allgemeinen Menü herauslösen.
- [x] Unterseiten gruppieren, echte vorhandene Einstellungen anbinden, zusätzliche alte Optionen über die weiteren Einstellungen zugänglich halten.
- [x] Native Screenshots mit Robolectric/Compose erzeugen und mit den Nutzerbildern vergleichen; sichtbare Probleme beheben. Geräteprüfung davon unterscheiden.

## Task 3: Prüfung und Übergabe

- [x] Neue UI-Tests und gesamte vorhandene Suite ausführen; Debug-/Release-Build und Lint prüfen.
- [x] Selbstprüfung des UI-Deltas durchgeführt; unabhängiger Reviewer wegen erreichtem Nutzungslimit nicht verfügbar, Einschränkung dokumentiert.
- [x] PROGRESS.md, Referenzdokumentation und PR-Titel/-Beschreibung auf den endgültigen Umfang aktualisieren.
