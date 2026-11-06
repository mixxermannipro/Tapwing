# Geprüfte Quellprojekte

## HeliBoard

Stand [v4.2 / 0c60dff](https://github.com/HeliBorg/HeliBoard/tree/0c60dffb03a4e2e7a37cb82210f5299f93c03979). Hauptzweig zusätzlich auf [2d9869b](https://github.com/HeliBorg/HeliBoard/commit/2d9869b2bf466874492f529d40630e135f4f14af) geprüft. Anfang November 2026 wurden Start, Emoji-Einfügen und der Wechsel der Tastaturansichten weiter gepflegt.

- `LatinIME.java` verwaltet den Android-Dienst und Feldwechsel. `InputLogic.java` verarbeitet Text, Korrektur und Rückgängig. `RichInputConnection.java` kapselt die Verbindung zum aktiven Textfeld.
- `MainKeyboardView`, `KeyboardSwitcher` und `PointerTracker` zeichnen und verarbeiten die Touch-Tastatur. Das Layout kommt aus Ressourcen und den Parsern unter `keyboard/internal/keyboard_parser`.
- `Suggest.kt`, `DictionaryFacilitatorImpl.kt` und `BinaryDictionary.java` verbinden Sprache, Wörterbuch, Korrektur, Lernwörter und Vorhersage. Die C++-Suche liegt unter `app/src/main/jni`. Sprachvarianten und Layouts werden in den Einstellungen zusammengeführt.
- `ClipboardHistoryManager.kt` und `ClipboardDao` verwalten die Zwischenablage. HeliBoard schaltete Verlauf und Vorschläge von Haus aus ein. Satzfink deaktiviert beide standardmäßig und sperrt automatische Zugriffe in geschützten Feldern.
- `BatchInputArbiter` erfasst die Wischspur. HeliBoard lädt dafür optional fremde Bibliotheken. Satzfink lädt nur die selbst gebaute Wörterbuchbibliothek. Die offene Wischerkennung fehlt noch.
- Die Einstellungen verwenden Compose und Android-Einstellungen (SharedPreferences). Vorhandene Tests verwenden JUnit, Robolectric und die AndroidX-Testbibliothek. Der Build nutzt Kotlin 2.4.0, Gradle 9.7.1 und Android Gradle Plugin 9.1.1. Herkunft und Lizenz der übernommenen Daten stehen in [LICENSES.md](LICENSES.md).

## LeanType

Stand [e544683](https://github.com/LeanBitLab/LeanType/tree/e544683de0d56aef158a673b40e5e679f214a6fb), mit Änderungen an der Eingabe vom 5. November 2026.

- `app/build.gradle.kts` nutzt Varianten für Standard und Offline, schließt aber alle Wörterbücher in beiden aus. Die gemeinsame Engine basiert auf HeliBoard. Touch, Vorschläge, Zwischenablage und Einstellungen folgen dessen Aufbau. Für Wischen kann die App eine Google-Bibliothek laden.
- `OfflineAiLoader.kt` lädt ausführbaren Code aus einem externen APK und initialisiert ihn im Tastaturprozess. Die Versionsprüfung belegt die Herkunft der Datei nicht. Der GGUF-Zusatz nutzt CPU-Ausführung und auf interne Bibliotheksdetails zugeschnittene Zugriffe. [Offline-Zusatz](https://github.com/LeanBitLab/LeanType-Offline-AI-Plugin/tree/b91c10885c2e9f837dddc29a79c03501b5a419e8)
- `ProofreadService.kt` der Standardvariante unterstützt Gemini, Groq und OpenAI-kompatible Server. Bei einem Fehler der verschlüsselten Ablage nutzt sie gewöhnliche Einstellungen als Ausweichlösung. `InputLogic.kt` kann ohne Auswahl große Teile des Eingabefelds lesen. Die Kontrolle vor dem Ersetzen erkennt Änderungen im selben Feld nicht sicher.
- `VoiceInputManager.kt` überträgt Ton an einen Dienst. Sitzungskennungen und das Erkennen eines Dienstabbruchs sind brauchbare Ansätze. Das Schreiben von Zwischenständen direkt in das Textfeld und die Rohtextprotokolle sind für Satzfink ungeeignet. Der externe Dienst ist im geprüften Stand ohne eigene Berechtigung exportiert. Der Modellkatalog verwendet bewegliche URLs ohne fest eingetragene Prüfsummen. [Sprach-Zusatz](https://github.com/LeanBitLab/LeanType-Voice-Plugin/tree/f8a42adc856191ae70e33bad89d0a33153e1e577)
- Der gemeinsame Android-Manifest erlaubt unter anderem Mikrofon und Kamera. Die Standardvariante erlaubt Netzwerkzugriffe. Der Build ohne Anpassung brach wegen NDK 28.0 ab. Mit NDK 28.2 entstand ein Offline-Debug-APK. Die Paketprüfung bestätigte fehlende Wörterbücher und keine Internetberechtigung. Die Prüfdateien im Projekt nutzen Robolectric und JUnit. Der geprüfte CI-Ablauf kompiliert Tests, führt sie aber nicht aus.

## Weitere Tastaturen und Wischen

- [AOSP LatinIME, 127336e](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5) enthält die historische Java- und C++-Engine. Die offene Gestenfabrik liefert einen leeren Handler. Der alte eigenständige Build nutzt SDK 28 und JCenter.
- [PixelBoard, 7e02566](https://github.com/Akshayykadam/PixelBoard/tree/7e025660c974f3088549b55364b909a9b2b836c9) schaltet Funktionen in der Gboard-App über Patches frei. Diktat und Schreibwerkzeuge hängen dort von Google-Code ab. Die [Bildmontage](https://github.com/Akshayykadam/PixelBoard/blob/7e025660c974f3088549b55364b909a9b2b836c9/docs/images/showcase.png) zeigt drei versetzte Tastenreihen, abgesetzte Sondertasten und die Werkzeugleiste. Gerät und Pixeldichte der Montage sind unbekannt.
- [CleverKeys, 6e964a5](https://github.com/tribixbite/CleverKeys/tree/6e964a571c4664a2a5a8c5138f56337cad55667d) enthält einen geometrischen Kotlin-Decoder (GPL-3.0) mit Tests für DE/EN. Das ist der erste Kandidat für die Integration. Der ebenfalls vorhandene neuronale Decoder benötigt zusätzliche Prüfung von Gewichten, Daten und Wortlisten.
- [FlorisBoard, bac346e](https://github.com/florisboard/florisboard/tree/bac346e8a5e7ee7104359cebdbce7d4105d3f0fa) hat einen geometrischen Klassifizierer. Der aufrufende `GlideTypingManager` ist im geprüften Stand deaktiviert.
- [FUTO Keyboard, 7d61ab4](https://github.com/futo-org/android-keyboard/tree/7d61ab4132cfcbb7aa713190d934f0125dc38b03) verwendet eigene Modelle für Wischen und Kontext. Die Lizenz beschränkt die kommerzielle Weitergabe. Der Code bleibt eine Referenz.

Die Recherche zu lokaler Sprache und KI steht unter [MODELS.md](MODELS.md).
