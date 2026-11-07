# Übergabe für die nächste Sitzung

**Stand 6. November 2026.** Tapwing, Entwicklungsversion `0.1.0-dev`.
Repository [mixxermannipro/Tapwing](https://github.com/mixxermannipro/Tapwing).
Der Fortsetzungszweig `chore/cloud-continuation` basiert auf main und integriert
die bislang getrennten Entwurfs-PRs #2, #4 und #5. main und die bisherigen
Entwicklungszweige bleiben unverändert. [Analyse aller PRs und Issues](REPOSITORY_STATUS.md).
Die Fortsetzung liegt in [Entwurfs-PR #6](https://github.com/mixxermannipro/Tapwing/pull/6).
Der Nutzer hat die Integration und das Zusammenführen aller übernehmbaren PRs
ausdrücklich freigegeben. Eine APK-Veröffentlichung bleibt ein separater Schritt.

## Start für Codex Cloud

1. Lies [AGENTS.md](../AGENTS.md), prüfe `git status --short --branch` und `git fetch origin` sowie den aktuellen PR. Arbeite auf einem eigenen Zweig. Die älteren PR-Zweige nicht verändern.
2. Nutze ein vollständiges JDK 17, Android SDK 37.2, Build-Tools 36.0.0 und NDK 28.2.13676358. Der Wrapper verwendet Gradle 9.7.1 mit Prüfsumme. [Einrichtung und Cloud-Pfade](../tools/dev/README.md). In dieser Sitzung: `source /workspace/tapwing-env.sh`.
3. Führe `python3 tools/generate-swipe-lexicons.py --check`, `./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest` und `./gradlew :app:lintDebug` aus. APKs und lokale Konfiguration bleiben außerhalb von Git.
4. Die folgende Historie enthält Nachweise früherer Sitzungen. Maßgeblich für neue Prüfungen ist der Abschnitt „Aktueller Prüflauf“ am Ende. Physische Geräte wurden in dieser Cloud-Sitzung nicht geprüft.

## Fertig und belegt

- Drei Grundlagen im Quellcode verglichen. HeliBoard 4.2 ist gewählt. Es bringt die vorhandene Java/Kotlin-Eingabe, die native C++-Wörterbuchsuche, layoutspezifische Einstellungen und die Werkzeugleiste mit. Gründe und Einschränkungen stehen in [ARCHITECTURE.md](ARCHITECTURE.md) und [UPSTREAM_RESEARCH.md](UPSTREAM_RESEARCH.md).
- Eigene App-Kennung `io.github.mixxermannipro.satzfink`, eigener Name und eigenes Startsymbol. Die Debug-Variante hat den Zusatz `.debug`. Die interne Kennung stammt vom früheren Arbeitsnamen Satzfink. Der endgültige Produktname ist Tapwing (Issue #1). Eine vollständige Markenprüfung steht aus.
- Deutsche, US-englische und UK-englische Wörterbücher und passende Quellwortlisten enthalten. Die drei Binärwörterbücher wurden aus diesen Quellen bytegenau nachgebaut. [Herkunft und Prüfsummen](LICENSES.md)
- Der Import fremder ausführbarer Gestenbibliotheken, die aktive Gestendatensammlung und die Weiterleitung an eine externe Diktat-Tastatur wurden deaktiviert. Kontaktberechtigung und automatische Android-Sicherung sind entfernt. Zwischenablagevorschläge und Verlauf sind anfangs aus. Der automatische Zugriff prüft geschützte Felder und sensible Clips.
- Der APK-Inhalt wurde geprüft. Er enthält kein `INTERNET`, `RECORD_AUDIO` oder `READ_CONTACTS`, keine Google-Gestenbibliothek und keine Modellgewichte. Eingabedienst und Rechtschreibprüfung sind durch Android-Berechtigungen geschützt. Die vollständige Sicherheitsprüfung vor einer stabilen Version steht aus.
- Android 16 im x86_64-Emulator hat die Debug-APK installiert und Tapwing als aktive Tastatur erkannt. Echte Bildschirmtasten haben im getrennten Testprogramm `helo` eingegeben. Die lokale englische Vorschlagsleiste zeigte `helo`, `help`, `hell`. Ein weiterer Lauf mit der aktuellen APK und ausgeschaltetem WLAN und Mobilfunk hat `teh` mit der Leertaste zu `the` korrigiert. Die Löschtaste stellte danach `teh` wieder her. Das Passwortfeld öffnete die Tastatur und meldete Androids Passworttyp. Eine vollständige Passwort- und PIN-Prüfung, deutsche Vorschläge und die Korrekturrate sind offen. [Bildschirmfoto](screenshots/android-16-english-suggestions.png)
- Zwei APKs sind lokal gebaut. Die Debug-APK ist mit dem lokalen Entwicklungsschlüssel signiert. Die Release-APK ist unsigniert. Android-Code ist für arm64-v8a, armeabi-v7a, x86 und x86_64 enthalten. Das sind gebaute Architekturen. Getestet wurde der x86_64-Emulator.

## Zweiter Meilenstein: Wischeingabe

- [PR #4](https://github.com/mixxermannipro/Tapwing/pull/4) baut auf #2 auf. Deutsche und englische Wischeingabe lässt sich unter „Gesten-Tippen“ aktivieren. Die Tastatur zeigt die Spur während der Berührung und bis zu sechs Treffer nach dem Loslassen.
- Der GPL-3.0-Decoder stammt von CleverKeys. Die Tastatur nutzt drei eigene Wortlisten mit rund 129.000 US-englischen, 126.000 UK-englischen und 206.000 deutschen Wörtern. [Quellen](LICENSES.md)
- Lokal liefen Debug- und Release-Build sowie 187 automatische Fälle. 182 bestanden, fünf bestehende Fälle wurden übersprungen. Ein künstlicher Pfad für „Straße“ und ein Pfad mit Umlaut waren in den lokalen Tests enthalten.
- Der Android-16-Emulator setzte bei ausgeschaltetem WLAN und Mobilfunk `world` und `hallo` über Bildschirm-Wischpfade ein. Abbruch änderte das leere Feld nicht. Der gleiche deutsche Pfad erzeugte in Passwort- und Privatfeldern höchstens ein einzelnes Zeichen. Deutsche ß-Erkennung im Emulator war mit dem getesteten Pfad noch nicht zuverlässig. [Bildschirmfoto](screenshots/android-16-german-swipe.png)
- Der automatische GitHub-Build ist in PR #4 eingerichtet und auch für PR #5 erfolgreich. PR #3 ist zusammengeführt. Die Textkonflikte mit #2 sind im Fortsetzungszweig aufgelöst.

## Gestaltung nach der Bildvorlage

Der Zweig `feat/reference-keyboard` bringt das dunkle Graphit-Design, sichtbare abgerundete Tasten, Zahlenreihe, kompaktes deutsches QWERTZ und den sichtbaren Namen Tapwing in die App. [Gestaltungsdetails](REFERENCE_KEYBOARD.md)

Die vorhandenen Werkzeuge sind aktiv. Die in der Vorlage sichtbaren Funktionen GIF, Übersetzung und Diktat müssen noch umgesetzt werden. Abgleich auf dem Honor-Gerät, exakte Abstände und Formate jenseits des Hochformats sind offene Prüfungen.

## Fortsetzung: Wisch-Lebenszyklus

Editor und Eingabeverbindung werden jetzt bereits bei Gestenbeginn erfasst.
Eine während der Geste gewechselte oder zunächst fehlende Verbindung darf nach
Loslassen keinen Treffer im anschließend aktiven Feld einsetzen. Die Identitäten
werden vor dem Decode und vor der Übernahme der Worker-Antwort geprüft.

15 neue Lebenszyklustests halten Worker- und UI-Rückrufe kontrolliert zurück.
Vier Fälle reproduzierten am bisherigen Code die unerwünschte Freigabe. Mit der
Ergänzung bestanden alle 15 im direkten JUnit-/Robolectric-Lauf. Der reguläre
Gradle-Gesamtlauf wird unten separat dokumentiert. Die Tests prüfen die echte
Ergebnisfreigabe; Android-Editorgrenzen und der asynchrone Decoder sind Testdoubles.
Sie ersetzen keinen Gerätecheck mit menschlichen Wischspuren.

## Einstellungen nach der Nutzervorlage

Die native Oberfläche wurde nach sechs bereitgestellten Bildern umgebaut:
vier Hauptgruppen, Graphit-Karten, blaue Schalter, große Seitentitel und direkte
Regler für Schrift, Emojis und Zwischenablage. Alte Optionen sind weiter erreichbar.
Geplante Funktionen sind sichtbar deaktiviert; Datenschutzvorgaben bleiben erhalten.
[Umsetzung und native Renderings](SETTINGS_REFERENCE.md).

Der aktuelle Gesamtlauf erfasst **216 Tests: 211 bestanden, 5 bekannte Ausnahmen,
0 Fehler**. Alle 12 neuen Compose-UI-Fälle bestanden. Debug und unsignierte
Release-APK wurden auch mit der neuen Oberfläche gebaut. Die abschließende
Lint-Auswertung läuft beim Sichern dieses Standes noch.
Der Abschlusslauf fand einen neuen API-Kompatibilitätsfehler beim Anzeigen des
System-Rechtschreibstatus. Der Zugriff nutzt jetzt ab Android 12 den öffentlichen
Getter und auf Android 10/11 den lesbaren Systemwert; die Lint-Baseline bleibt
unverändert. Die Abschlussprüfung wird im Pull Request festgehalten.
Der zusätzliche UI-Reviewer meldete ein erreichtes Nutzungslimit; der neue
UI-Stand wurde deshalb selbst geprüft. Die früheren unabhängigen Reviews des
Wisch-Fixes und der präzisen Lint-Baseline bleiben dokumentiert.

## Noch offen, in Reihenfolge

1. **Tippqualität und Wischen absichern.** Offene Wischeingabe ist auf dem Zweig `feat/open-swipe` integriert. Prüfe echte Wischspuren für beide Sprachen und die Korrekturrate auf verschiedenen Geräten. Ergänze Abbruch bei App- und Layoutwechsel, deutsche Komposita, Sprachmischung, Umlaut und ß auf echten Geräten. Die bisherige Geräteprüfung lief mit künstlichen Touch-Ereignissen im Emulator.
2. **Darstellung und Bedienung.** Prüfe eine vergleichbare Konfiguration mit Gboard/PixelBoard, ohne fremde Grafiken. Vergleiche Tastengrößen, Leiste, Kontrast und Abstände. Prüfe Hoch- und Querformat, Einhand, geteilte und schwebende Ansicht, Tablet und Faltgerät, TalkBack und mehrere echte Apps. Dokumentiere Gerät, Auflösung und Einstellungen.
3. **Lokales Diktat.** Beginne mit lokalem Whisper für Deutsch und Englisch. Prüfe Modellformat, Prüfsumme und ARM64. Baue danach Mikrofon, Pegel, Zeit, Abbruch, Vorschau und sichere einmalige Einfügung. Erst danach folgen Pausenerkennung, laufende Teilvorschau, Bereinigt, Smart mit Selbstkorrekturen, Professionell, Übersetzen und eigene Regeln. [Diktat](VOICE_DICTATION.md) · [Modelle](MODELS.md)
4. **Modellverwaltung und lokale KI.** Download, Fortsetzen, Abbruch, Prüfsumme, Import, Speicherprüfung und Auswahl fertigstellen. Lokale KI mit llama.cpp, begrenzter Laufzeit und Abbruch getrennt von der Tastatur. Prüfe Texttreue auf DE/EN und nenne nur auf Android getestete Modelle im Katalog.
5. **Cloud-KI und Schreibwerkzeuge.** Eigene API-Schlüssel, verschlüsselte Ablage, ausdrückliche Textfreigabe, Anbieteradapter und sichere Vorschau mit Rücknahme. Ergänze Rechtschreibung, Ton, Übersetzen, Kürzen, Zusammenfassen, Formatieren und eigene Anweisungen. Die Auswertung darf nie nach einem Feldwechsel in ein anderes Feld schreiben. [Bearbeitung](AI_ARCHITECTURE.md) · [Anbieter](PROVIDERS.md)
6. **Produktivität, Sicherheit, Leistung.** Vorlagen, eigene Wörter, App-Profile und Zwischenablage-Aktionen priorisieren. OCR nur nach ausdrücklicher Bildauswahl. Teste fehlerhafte Wörterbücher/Modelle, Protokolle, Eingabefeldwechsel, Berechtigungen, Abbruch und bösartige Anweisungen im Text. Miss Leistung und Speicher auf echten Geräten. [Leistungsmessung](BENCHMARKS.md)
7. **Veröffentlichung.** Dauerhafte Signierung, unabhängig wiederholter Build, Quellarchiv, APK-Prüfsummen, Gerätetestmatrix, [Funktionsvergleich](FEATURE_PARITY.md) und [Veröffentlichungsschritte](RELEASE.md) vervollständigen. Für Obtainium und F-Droid getrennt prüfen. Eine stabile Version setzt bestandene Funktions- und Sicherheitsprüfungen voraus.

## Bekannte technische Hinweise

- Im aktuellen Robolectric-Gesamtlauf erschienen zwei `NullPointerException`-Meldungen aus dem `UncaughtExceptionHandler` von `DefaultDispatcher-worker`. Die JUnit-Berichte enthalten keine Fehler oder fehlgeschlagenen Tests. Die genaue Ursache dieser Hintergrundmeldungen ist noch nicht isoliert; sie bleiben eine offene Testumgebungsfrage und sind kein Nachweis für einen Gerätefehler. Der neue Lebenszyklustest nutzt bereits einen reinen `Application`-Kontext ohne App-Startarbeit.

- Java 25 auf dem bisherigen Fedora-Rechner hatte keinen Compiler. Das vorhandene vollständige JDK 17 funktioniert. Robolectric mit Android 36 würde Java 21 benötigen. Die lokalen Tests nutzen API 34.
- Android Gradle Plugin 9.1.1 meldet, dass SDK 37.2 außerhalb seiner getesteten SDK-Version liegt. Der lokale Build läuft. Bei einem späteren Werkzeugwechsel zuerst einen reproduzierbaren Vergleich bauen.
- Der Emulator startete auf diesem Host mit `-gpu host -feature -Vulkan`. Softwaregrafik und `-gpu off` brachen beim Start ab. Bei gemeldeter Hardwaretastatur musste die Bildschirmtastatur zusätzlich aktiviert werden.
- Das Paket verwendet weiter die internen Java-Paketnamen von HeliBoard. Ein anderer Name für die installierte App steht in `applicationId`. Bewahre diese Trennung, solange native Bindungen die internen Namen voraussetzen.

## Prüflauf vor dem Einstellungsumbau

Die aktuelle lokale Gradle-Suite erfasst 204 Tests: **199 bestanden, 5 bewusst
übersprungen, 0 Fehler**. Die fünf bekannten Ausnahmen sind der Hangul-Fall aus
HeliBoard und vier separat schaltbare URL-Prüfungen. Alle 15 neuen Lebenszyklusfälle
bestanden. Der reine Testkontext startet hierfür keine App-Hintergrundarbeit.

Der Code-Stand `a584711` bestand außerdem den
[GitHub-Android-Check](https://github.com/mixxermannipro/Tapwing/actions/runs/42249646850).
Die unabhängige Prüfung des selbst erstellten Deltas ergab keine Befunde.
Debug- und unsignierte Release-APK wurden für arm64-v8a, armeabi-v7a, x86 und
x86_64 gebaut. Die finale Debug-APK enthält nachweislich die neuen Feldprüfungen;
Signatur und Berechtigungen sind geprüft. Kein INTERNET, RECORD_AUDIO oder READ_CONTACTS.
Die getrennte IME-Test-App wurde ebenfalls gebaut. APKs und SHA-256-Summen liegen
lokal unter `/workspace/shared`, nicht in Git.

Der vollständige Lint-Erstlauf fand 68 Fehler: ausschließlich fehlende Übersetzungen
in der unverändert übernommenen Datei `strings-talkback-descriptions.xml`.
Diese konkreten Altlasten stehen jetzt in `app/lint-baseline.xml`; es wurden keine
Übersetzungen vorgetäuscht und keine Prüfungen pauschal abgeschaltet.
Die 129 Warnungen und ein Hinweis des Erstlaufs bleiben sichtbar.
Der gezielte Lint-Nachlauf war erfolgreich: 129 Warnungen, 1 Hinweis und exakt
68 durch die Baseline erfasste Altfehler. Neue Fehler bleiben blockierend.
Die früheren Gerätenachweise oben stammen vom damaligen Entwicklungsrechner und Emulator.

Der aktive GitHub-Workflow liegt in `.github/workflows/android.yml`.
`tools/ci/android.yml` ist eine ältere Vorlage; bei CI-Änderungen ist der aktive
Workflow maßgeblich. Für PR #4 und #5 wurde erfolgreicher Android-CI-Status geprüft.

## CORE-01 am 7. November 2026

Ausgangsbasis war `60de493` auf `origin/main`. Beim Löschen einer
rückwärts markierten Auswahl meldete der Editor die Grenzen in umgekehrter
Reihenfolge. Dadurch verwendete Tapwing eine negative Löschlänge. Die
Eingabeverbindung ordnet jetzt beide Grenzen auch bei späteren Cursorereignissen.
Der Android-Test reproduzierte den Löschfehler auf der Basis. Ein zusätzlicher
Robolectric-Test reproduzierte den Fehler beim Aufheben der Auswahl vor dem
Fix und bestand danach.

- **Lokal:** Debug- und Release-Build, Unit-Tests, Lint,
  Wörterbuchprüfung und `git diff --check` erfolgreich. 229 Testfälle,
  224 bestanden, fünf bekannte Ausnahmen, keine fehlgeschlagenen Fälle.
  Neue Kernfälle laufen mit simuliertem Android 10, 13 und 14. Der
  Gesamtlauf meldete erneut eine Hintergrund-Ausnahme im Robolectric-Worker.
  Lint meldete 0 Fehler, 141 Warnungen, einen Hinweis und die 68 bekannten
  Baseline-Fälle. Nach Übernahme von VOICE-01 bestanden im kombinierten
  Stand 234 von 239 Fällen. Die fünf bekannten Ausnahmen bleiben erfasst.
- **Emulator:** Android 16, x86_64, Pixel 7, offline. Der Lauf bestand mit
  Deutsch QWERTZ und Englisch QWERTY. Bildschirmtasten prüften Cursor und
  Auswahl, Umschalt, Weiter-Aktion, Passwort, PIN, Privatfeld,
  Zeilenumbruch und Wiederöffnung nach App-Hintergrund. Englisch QWERTY
  korrigierte `teh` zu `the `. Die Löschtaste stellte `teh` wieder her.
  Ein zusätzlicher Lauf mit Deutsch QWERTZ bestätigte die Feststelltaste
  über zwei Buchstaben sowie Einfügen und Löschen von `ä` per langem
  Tastendruck.
- **Physisches Gerät:** Samsung SM-S938B, Android 17 (API 37), 1440 × 3120 Pixel,
  600 dpi, Hochformat. Getesteter Commit `60692ed`. Beide Läufe mit
  Bildschirmtasten bestanden. Geprüft sind Deutsch QWERTZ, Englisch QWERTY,
  Wechsel zwischen beiden Layouts, Cursor, rückwärts markierte und aufgehobene
  Auswahl, Umschalt, Weiter, Zeilenumbruch, Passwort, PIN, Privatfeld und
  Wiederöffnung nach App-Hintergrund. Auch die englische Autokorrektur und
  Rücknahme bestanden. Der Lauf nutzte die Test-App mit synthetischen Eingaben.
- **Offen:** Die App-Matrix auf physischen Geräten mit Android 10, 13 und 16
  sowie Feststelltaste, langer Tastendruck und Emoji-Eingabe über die Oberfläche.
  [Ablauf und Testmatrix](TESTING.md#kernprüfung-für-core-01)

## VOICE-01 am 7. November 2026

Diktatsitzung und Anbieter-Verträge für Erkennung und Bereinigung liegen in
`helium314.keyboard.voice`. Testanbieter prüfen unter anderem Teilresultate,
Abbruch, späte Antworten nach Feldwechsel, getrennte Empfängerfreigaben und
die einmalige Vorschauübernahme. Beim Feldwechsel während eines Zustandswechsels
wird vor dem Anbieteraufruf beziehungsweise vor der Vorschau-Rückgabe erneut
geprüft. [Datenfluss, Anbieter und Abhängigkeiten](VOICE_SESSION_ADR.md)

- **Lokal:** Debug- und Release-Build, Unit-Tests und Lint erfolgreich.
  226 Testfälle, 221 bestanden, fünf bekannte Ausnahmen. Zehn Fälle prüfen
  die neue Sitzung. Der Gesamtlauf meldete erneut eine Hintergrund-Ausnahme
  im Robolectric-Worker.
- **Emulator und physische Geräte:** Diese Stufe hat keine Android-Anbindung.
  Aufnahme, Spracherkennung und Einfügen werden erst in den folgenden
  Tickets auf Geräten geprüft. Das Offline-Manifest enthält weiterhin
  keine Internet- oder Mikrofonberechtigung.
- **Nächste Stufe:** #8 verbindet die Sitzung mit einer begrenzten Aufnahme
  und dem Android-Lebenszyklus. Die sichere Textübernahme braucht #18 und #32.
