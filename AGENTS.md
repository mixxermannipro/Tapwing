# Projekt-Anleitung

## Projekt und Struktur

Satzfink ist eine native Android-Tastatur (IME) für Android 10 und neuer. Der Quellcode basiert auf HeliBoard 4.2. Java und Kotlin steuern die Eingabe. C++ enthält die Wörterbuchsuche.

- Beginne mit [docs/PROGRESS.md](docs/PROGRESS.md) und [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
- `app/src/main/java/helium314/keyboard` enthält Tastatur, Einstellungen und Eingabelogik.
- `app/src/main/jni` enthält den lokal gebauten Wörterbuchcode.
- `dictionaries/sources` enthält die Quellwortlisten für Deutsch, US-Englisch und UK-Englisch.

## Bauen und prüfen

- Vollständiges JDK 17, Android SDK 37.2 und NDK 28.2.13676358 nutzen.
- `./gradlew :app:assembleDebug :app:testDebugUnitTest` und `git diff --check` ausführen.
- Testergebnisse und offene Aufgaben in `docs/PROGRESS.md` festhalten. Gerät und Emulator getrennt ausweisen.

## Regeln

- Teste Änderungen in einem Zweig und öffne einen Pull Request (Anfrage zum Übernehmen der Änderungen). Bei fehlgeschlagenen Prüfungen bleibt der Zweig separat.
- Offline-Tippen bleibt unabhängig von Netz, Mikrofon, Modellen und KI. Keine Passwörter, Eingabetexte, Prompts oder Schlüssel in Protokolle schreiben.
- Keine fremden ausführbaren Dateien als Modelle oder Erweiterungen laden. Bei neuen Wörterbüchern, Modellen und Bibliotheken die Herkunft und Lizenz belegen.
- Nutze das bestehende Repository `mixxermannipro/openboard`. Keine anderen Projekte ändern. Keine Schlüssel, lokalen Konfigurationsdateien oder privaten Testdaten hochladen.
