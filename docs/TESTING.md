# Tests

## Lokale Tests und Build

Setze `JAVA_HOME` auf ein vollständiges JDK 17 und `ANDROID_HOME` auf Android SDK 37.2 mit NDK 28.2.13676358 und Build-Tools 36.0.0.

```sh
./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest
git diff --check
```

Robolectric nutzt Android API 34 mit JDK 17. Der Gerätecheck läuft getrennt auf Android 16. Der bekannte Hangul-Kompositionsfall aus HeliBoard ist mit Verweis auf das ursprüngliche Problem als übersprungen markiert. Die Netzwerkprüfungen aus HeliBoard laufen getrennt von den lokalen Tests. Bei Bedarf starten sie mit `SATZFINK_CHECK_EXTERNAL_LINKS=1 ./gradlew :app:testDebugUnitTest --tests '*XLinkTest' --rerun-tasks`. Externe Links waren beim ursprünglichen Lauf teils nicht erreichbar.

Die Tests für Passworteingabe und den Schutz vor personalisiertem Lernen stehen in `InputPrivacyTest.kt`. Die Lebenszyklustests in `latin/inputlogic/OpenSwipeLifecycleTest.java` prüfen die echte Ergebnisfreigabe mit kontrollierten Worker-Rückrufen und Android-Grenzen: Feld- und Layoutwechsel während der Geste, verspätete Antworten, Abbruch und eine inzwischen begonnene neue Geste. Sie ersetzen keine Touch-Prüfung auf Geräten. Emoji-, Vorschlags-, Layout- und Eingabetests stammen überwiegend aus HeliBoard. Die Zeichengrenzen nutzen Android ICU (die Textbibliothek von Android).

Gezielt lässt sich die Ergebnisfreigabe so prüfen:

```sh
./gradlew :app:testDebugUnitTest --tests '*OpenSwipeLifecycleTest'
```

## Emulator oder Testgerät

Installiere die Debug-APK aus dem Projekt. Aktiviere Tapwing über die Android-Tastatureinstellungen. Wenn ein Emulator eine Hardwaretastatur meldet, aktiviere dort „Bildschirmtastatur anzeigen“.

Die optionale Test-App enthält ein normales Feld, ein Feld ohne personalisiertes Lernen, ein Passwortfeld und ein PIN-Feld. Sie gehört nur in eine Testumgebung. Nach einem Debug-Build existiert der lokale Entwicklungsschlüssel für die Test-App.

```sh
tools/ime-smoke/build.sh /tmp/satzfink-ime-fixture.apk
adb install /tmp/satzfink-ime-fixture.apk
adb shell am start -n io.github.mixxermannipro.satzfink.smoke/.FixtureActivity
```

Wähle je Feld die Bildschirmtastatur und tippe die Testzeichen über Tapwing. Prüfe Vorschläge, Auswahl, Löschung und Feldwechsel. EN-QWERTY, Vorschläge und die Rücknahme einer Korrektur wurden in der Test-App auf Android 16 geprüft. DE-QWERTZ, lange Eingaben, Browser, Nachrichten-App, Tablet und TalkBack stehen in der Geräteprüfung noch aus. [Letzter Prüfnachweis](PROGRESS.md)

[Bildschirmfoto der englischen Vorschläge auf dem Android-16-Emulator](screenshots/android-16-english-suggestions.png). Pixel 7, Hochformat, 1080 × 2400, 420 dpi, helles Thema, keine Netzverbindung.

## Wischeingabe prüfen

Aktiviere „Gesten-Tippen“ und wähle Deutsch oder Englisch. Die Test-App unterstützt künstliche Touch-Ereignisse im Emulator. Die Koordinaten hängen von Auflösung, Tastaturhöhe und Layout ab. Übergebe mindestens drei Punkte im Format `x,y;x,y;x,y`:

```sh
adb shell "am instrument -r -w -e path '<punkte>' -e expected '<testwort>' io.github.mixxermannipro.satzfink.smoke/.SwipeInstrumentation"
```

`INSTRUMENTATION_CODE: -1` bedeutet bestanden. `-e cancel true` bricht die Geste ab. Mit `-e field 1` prüfst du das Privatfeld, mit `-e field 2` das Passwortfeld. `-e maxChars 1` prüft, dass die Geste in diesen Feldern kein ganzes Wort einsetzt.
