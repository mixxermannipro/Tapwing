# Tapwing

Tapwing ist eine quelloffene Android-Tastatur auf Basis von HeliBoard. Die erste Entwicklungsversion konzentriert sich auf Tippen und Korrektur auf Deutsch und Englisch. Sie enthält keine Netzwerkberechtigung.

**Stand 0.1.0-dev:** Die Tastatur ist für Entwicklung und Tests gedacht. Wischeingabe für Deutsch und Englisch lässt sich unter „Gesten-Tippen“ einschalten. Diktat und KI-Schreibwerkzeuge sind offen. Die [Funktionsübersicht](docs/FEATURE_PARITY.md) trennt vorhandene Funktionen von geprüften Funktionen.

## Bauen und ausprobieren

Du brauchst ein vollständiges JDK 17, Android SDK 37.2 und NDK 28.2.13676358. Setze `JAVA_HOME` auf das JDK und `ANDROID_HOME` auf das Android SDK.

```sh
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/Tapwing_0.1.0-dev-debug.apk
```

Öffne Tapwing, aktiviere die Tastatur in den Android-Einstellungen und wähle sie als Eingabemethode. Unter „Sprachen und Layouts“ kannst du Deutsch (QWERTZ) und Englisch (QWERTY) einrichten.

[Einstellungen nach Bildvorlage](docs/SETTINGS_REFERENCE.md) · [Gestaltung nach Bildvorlage](docs/REFERENCE_KEYBOARD.md) · [Tests](docs/TESTING.md) · [Fortschritt](docs/PROGRESS.md) · [Datenschutz](PRIVACY.md) · [Lizenzen](docs/LICENSES.md)

Tapwing-eigene Beiträge stehen unter [GPL-3.0-or-later](LICENSE). Übernommene GPL-3.0-only-Dateien behalten ihre Lizenz; für die kombinierte App gilt GPLv3. Der vorhandene Tastaturcode stammt von HeliBoard, OpenBoard und AOSP LatinIME. Die Originalhinweise stehen im Quellcode und unter [docs/upstream](docs/upstream).

[Mitwirken](CONTRIBUTING.md) · [Sicherheitsmeldungen](SECURITY.md) · [Arbeitsanleitung](AGENTS.md) · [Entwicklungsumgebung](tools/dev/README.md)

## English

Tapwing is an open-source Android keyboard based on HeliBoard. The development build includes offline typing and dictionaries for German, US English and UK English. Offline swipe typing for German and English can be enabled in Gesture Typing. Dictation and AI writing tools are planned. Use the build commands above to install the development APK. [Development handoff](docs/PROGRESS.md)
