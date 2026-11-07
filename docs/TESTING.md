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

Die optionale Test-App enthält ein normales Feld, ein Feld ohne personalisiertes Lernen, ein Passwortfeld, ein PIN-Feld und ein mehrzeiliges Feld. Sie gehört nur in eine Testumgebung. Nach einem Debug-Build existiert der lokale Entwicklungsschlüssel für die Test-App.

```sh
tools/ime-smoke/build.sh /tmp/satzfink-ime-fixture.apk
adb install /tmp/satzfink-ime-fixture.apk
adb shell am start -n io.github.mixxermannipro.satzfink.smoke/.FixtureActivity
```

Wähle je Feld die Bildschirmtastatur und tippe die Testzeichen über Tapwing. Prüfe Vorschläge, Auswahl, Löschung und Feldwechsel. EN-QWERTY, Vorschläge und die Rücknahme einer Korrektur wurden in der Test-App auf Android 16 geprüft. DE-QWERTZ, lange Eingaben, Browser, Nachrichten-App, Tablet und TalkBack stehen in der Geräteprüfung noch aus. [Letzter Prüfnachweis](PROGRESS.md)

[Bildschirmfoto der englischen Vorschläge auf dem Android-16-Emulator](screenshots/android-16-english-suggestions.png). Pixel 7, Hochformat, 1080 × 2400, 420 dpi, helles Thema, keine Netzverbindung.

### Kernprüfung für CORE-01

Führe die Schritte auf Android 10, 13 und 16 aus. Trage je Lauf Android-Version,
Gerät oder Emulator, Tapwing-Commit, Sprache, Layout und Ergebnis ein. Nutze
Testkonten und erfundene Eingaben. Dokumentiere Fehler mit den Schritten zum
Nachstellen, ohne Eingaben aus echten Konten oder Passwortfeldern zu speichern.

Die gezielten Robolectric-Fälle in `InputLogicTest` laufen mit den APIs 29, 33
und 34. Sie prüfen Auswahl und Cursor, rückwärts gemeldete Auswahl, Wechsel
nach einer Autokorrektur ins leere Passwortfeld und Wiederöffnung am gemeldeten
Cursor. Der separate Android-Test benutzt Bildschirmtasten und echte
Eingabeverbindungen zwischen Test-App und Tastatur.

```sh
./gradlew :app:testDebugUnitTest --tests '*InputLogicTest'

adb shell am instrument -r -w \
  -e layoutKey 594,1706 -e layoutLetter z \
  -e a 108,1864 -e delete 1000,2016 -e shift 80,2016 -e enter 1000,2170 \
  -e passwordA 108,1864 -e passwordDelete 1000,2016 -e pinOne 150,1570 \
  io.github.mixxermannipro.satzfink.smoke/.CoreInstrumentation
```

Wähle zuerst die Tapwing-Debug-Tastatur. Öffne nach der Installation einmal
das normale Feld der Test-App und warte, bis die Tastatur sichtbar ist. Starte
danach den Testbefehl. Die Beispielkoordinaten gelten für
Deutsch QWERTZ im Android-16-Pixel-7-Emulator mit 1080 × 2400 Pixeln, 420 dpi,
Standardhöhe, Zahlenreihe und Gestennavigation im Hochformat. Prüfe die
Tastenmitten nach dem Öffnen für jedes Gerät und passe die Koordinaten an.
Für Englisch QWERTY erwartet dieselbe Position in der ersten Buchstabenreihe
`y`. Ergänze im englischen Layout für den Korrekturtest vor dem Klassennamen
`-e checkAutocorrect true -e t 487,1706 -e e 270,1706 -e h 648,1864 -e space 540,2170`.
Der Lauf ist bei `INSTRUMENTATION_CODE: -1` und
`Core IME checks passed` bestanden. Der Befehl kann auch bei fehlgeschlagenem
Test mit Shell-Status 0 enden.

| Umgebung | Eingabe und erwartetes Ergebnis |
| --- | --- |
| Messaging und E-Mail-Entwurf | Auf EN-QWERTY `teh` tippen. Leertaste korrigiert zu `the `, Löschen stellt `teh` wieder her. Umschalt und doppelte Umschalt prüfen. Senden nur in einem Testkonto. |
| Browser-Adresszeile, Suchfeld und ContentEditable | Test-URL und Text eingeben. Cursor in die Mitte setzen, Auswahl vorwärts und rückwärts ersetzen und löschen. Benachbarte Zeichen bleiben stehen. Eingabeaktion bei der Suche und Zeilenumbruch im mehrzeiligen Feld prüfen. |
| Terminal | In eine leere Eingabe löschen und weiter tippen. Zeichen, Löschen und Enter mit einem harmlosen Testbefehl prüfen. |
| Login, Test-Banking, Passwort und PIN | Mit erfundenen Zeichen zwischen normalen, privaten und geschützten Feldern wechseln. Tippen und Löschen funktionieren. Das vorherige Feld bleibt unverändert. Es erscheinen keine Vorschläge für Passwort oder PIN. |
| Mehrzeiliges Feld und App-Wechsel | Emoji und Umlaute per Emoji-Leiste und langem Tastendruck einsetzen. Zusammengesetztes Emoji einmal löschen, Zeilenumbruch prüfen, App in den Hintergrund schicken und am zuletzt gewählten Cursor weitertippen. |
| Sprachwechsel DE/EN | Deutsch QWERTZ und Englisch QWERTY in Tapwing aktivieren. Mit der Sprachtaste wechseln. Dieselbe Position in der oberen Reihe ergibt `z` bzw. `y`. Prüfe die Vorschlagsleiste in beiden Sprachen. |

## Wischeingabe prüfen

Aktiviere „Gesten-Tippen“ und wähle Deutsch oder Englisch. Die Test-App unterstützt künstliche Touch-Ereignisse im Emulator. Die Koordinaten hängen von Auflösung, Tastaturhöhe und Layout ab. Übergebe mindestens drei Punkte im Format `x,y;x,y;x,y`:

```sh
adb shell "am instrument -r -w -e path '<punkte>' -e expected '<testwort>' io.github.mixxermannipro.satzfink.smoke/.SwipeInstrumentation"
```

`INSTRUMENTATION_CODE: -1` bedeutet bestanden. `-e cancel true` bricht die Geste ab. Mit `-e field 1` prüfst du das Privatfeld, mit `-e field 2` das Passwortfeld. `-e maxChars 1` prüft, dass die Geste in diesen Feldern kein ganzes Wort einsetzt.

## Lint

`./gradlew :app:lintDebug` prüft neue Fehler weiterhin strikt. Eine gezielte
Baseline dokumentiert 68 bereits im übernommenen Stand fehlende Übersetzungen
in den TalkBack-Beschreibungen. Sie enthält ausschließlich diese Fundstellen;
Warnungen sind nicht ausgeblendet. Details und aktuelle Ergebnisse stehen in
[PROGRESS.md](PROGRESS.md).
