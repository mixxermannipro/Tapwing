# Übergabe für die nächste Sitzung

**Stand 6. November 2026.** Projekt Satzfink, Entwicklungsversion `0.1.0-dev`. Repository [mixxermannipro/openboard](https://github.com/mixxermannipro/openboard). Der erste Entwicklungsstand liegt auf `feat/satzfink-foundation`. Änderungen an `main` und eine Veröffentlichung brauchen die ausdrückliche Freigabe des Nutzers. Der Stand ist als [Entwurfs-PR #2](https://github.com/mixxermannipro/openboard/pull/2) hochgeladen.

## Start für Codex Cloud

1. Lies [AGENTS.md](../AGENTS.md) und prüfe `git status --short --branch`, `git fetch origin` sowie den offenen Entwurfs-PR. Checke den Zweig aus, auf dem der Quellcode liegt.
2. Stelle ein vollständiges JDK 17, Android SDK 37.2, Build-Tools 36.0.0 und NDK 28.2.13676358 bereit. Der Wrapper lädt die festgelegte Gradle-Version mit Prüfsumme. Auf dem bisherigen Rechner lag das SDK unter `~/Android/sdk` und das JDK im Gradle-Verzeichnis `~/.gradle/jdks`. Die Pfade sind lokal und dürfen nicht fest im Projekt stehen.
3. Führe `./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest` aus. Die APKs entstehen unter `app/build/outputs/apk`. Lege sie außerhalb von Git ab.
4. Prüfe [PR #4](https://github.com/mixxermannipro/openboard/pull/4) für die Wischeingabe und [PR #3](https://github.com/mixxermannipro/openboard/pull/3) für den endgültigen Namen Tapwing. Halte jeden weiteren Meilenstein baubar und ergänze diese Datei nach den Tests.

## Fertig und belegt

- Drei Grundlagen im Quellcode verglichen. HeliBoard 4.2 ist gewählt. Es bringt die vorhandene Java/Kotlin-Eingabe, die native C++-Wörterbuchsuche, layoutspezifische Einstellungen und die Werkzeugleiste mit. Gründe und Einschränkungen stehen in [ARCHITECTURE.md](ARCHITECTURE.md) und [UPSTREAM_RESEARCH.md](UPSTREAM_RESEARCH.md).
- Eigene App-Kennung `io.github.mixxermannipro.satzfink`, eigener Name und eigenes Startsymbol. Die Debug-Variante hat den Zusatz `.debug`. „Satzfink“ ist ein Arbeitsname. Erste Web- und GitHub-Suchen ergaben keinen klaren Treffer für eine gleichnamige Tastatur. Eine vollständige Markenprüfung steht aus.
- Deutsche, US-englische und UK-englische Wörterbücher und passende Quellwortlisten enthalten. Die drei Binärwörterbücher wurden aus diesen Quellen bytegenau nachgebaut. [Herkunft und Prüfsummen](LICENSES.md)
- Der Import fremder ausführbarer Gestenbibliotheken, die aktive Gestendatensammlung und die Weiterleitung an eine externe Diktat-Tastatur wurden deaktiviert. Kontaktberechtigung und automatische Android-Sicherung sind entfernt. Zwischenablagevorschläge und Verlauf sind anfangs aus. Der automatische Zugriff prüft geschützte Felder und sensible Clips.
- Der APK-Inhalt wurde geprüft. Er enthält kein `INTERNET`, `RECORD_AUDIO` oder `READ_CONTACTS`, keine Google-Gestenbibliothek und keine Modellgewichte. Eingabedienst und Rechtschreibprüfung sind durch Android-Berechtigungen geschützt. Die vollständige Sicherheitsprüfung vor einer stabilen Version steht aus.
- Android 16 im x86_64-Emulator hat die Debug-APK installiert und Satzfink als aktive Tastatur erkannt. Echte Bildschirmtasten haben im getrennten Testprogramm `helo` eingegeben. Die lokale englische Vorschlagsleiste zeigte `helo`, `help`, `hell`. Ein weiterer Lauf mit der aktuellen APK und ausgeschaltetem WLAN und Mobilfunk hat `teh` mit der Leertaste zu `the` korrigiert. Die Löschtaste stellte danach `teh` wieder her. Das Passwortfeld öffnete die Tastatur und meldete Androids Passworttyp. Eine vollständige Passwort- und PIN-Prüfung, deutsche Vorschläge und die Korrekturrate sind offen. [Bildschirmfoto](screenshots/android-16-english-suggestions.png)
- Zwei APKs sind lokal gebaut. Die Debug-APK ist mit dem lokalen Entwicklungsschlüssel signiert. Die Release-APK ist unsigniert. Android-Code ist für arm64-v8a, armeabi-v7a, x86 und x86_64 enthalten. Das sind gebaute Architekturen. Getestet wurde der x86_64-Emulator.

## Zweiter Meilenstein: Wischeingabe

- [PR #4](https://github.com/mixxermannipro/openboard/pull/4) baut auf #2 auf. Deutsche und englische Wischeingabe lässt sich unter „Gesten-Tippen“ aktivieren. Die Tastatur zeigt die Spur während der Berührung und bis zu sechs Treffer nach dem Loslassen.
- Der GPL-3.0-Decoder stammt von CleverKeys. Die Tastatur nutzt drei eigene Wortlisten mit rund 129.000 US-englischen, 126.000 UK-englischen und 206.000 deutschen Wörtern. [Quellen](LICENSES.md)
- Lokal liefen Debug- und Release-Build sowie 187 automatische Fälle. 182 bestanden, fünf bestehende Fälle wurden übersprungen. Ein künstlicher Pfad für „Straße“ und ein Pfad mit Umlaut waren in den lokalen Tests enthalten.
- Der Android-16-Emulator setzte bei ausgeschaltetem WLAN und Mobilfunk `world` und `hallo` über Bildschirm-Wischpfade ein. Abbruch änderte das leere Feld nicht. Der gleiche deutsche Pfad erzeugte in Passwort- und Privatfeldern höchstens ein einzelnes Zeichen. Deutsche ß-Erkennung im Emulator war mit dem getesteten Pfad noch nicht zuverlässig. [Bildschirmfoto](screenshots/android-16-german-swipe.png)
- Der automatische GitHub-Build ist in PR #4 eingerichtet. Der Zweig von PR #2 bleibt die Basis. PR #3 enthält nur die vom Issue verlangten Projektdateien für Tapwing. Beim Zusammenführen müssen die überschnittenen Projekttexte abgeglichen werden.

## Gestaltung nach der Bildvorlage

Der Zweig `feat/reference-keyboard` bringt das dunkle Graphit-Design, sichtbare abgerundete Tasten, Zahlenreihe, kompaktes deutsches QWERTZ und den sichtbaren Namen Tapwing in die App. [Gestaltungsdetails](REFERENCE_KEYBOARD.md)

Die vorhandenen Werkzeuge sind aktiv. Die in der Vorlage sichtbaren Funktionen GIF, Übersetzung und Diktat müssen noch umgesetzt werden. Abgleich auf dem Honor-Gerät, exakte Abstände und Formate jenseits des Hochformats sind offene Prüfungen.

## Noch offen, in Reihenfolge

1. **Tippqualität und Wischen absichern.** Offene Wischeingabe ist auf dem Zweig `feat/open-swipe` integriert. Prüfe echte Wischspuren für beide Sprachen und die Korrekturrate auf verschiedenen Geräten. Ergänze Abbruch bei App- und Layoutwechsel, deutsche Komposita, Sprachmischung, Umlaut und ß auf echten Geräten. Die bisherige Geräteprüfung lief mit künstlichen Touch-Ereignissen im Emulator.
2. **Darstellung und Bedienung.** Prüfe eine vergleichbare Konfiguration mit Gboard/PixelBoard, ohne fremde Grafiken. Vergleiche Tastengrößen, Leiste, Kontrast und Abstände. Prüfe Hoch- und Querformat, Einhand, geteilte und schwebende Ansicht, Tablet und Faltgerät, TalkBack und mehrere echte Apps. Dokumentiere Gerät, Auflösung und Einstellungen.
3. **Lokales Diktat.** Beginne mit lokalem Whisper für Deutsch und Englisch. Prüfe Modellformat, Prüfsumme und ARM64. Baue danach Mikrofon, Pegel, Zeit, Abbruch, Vorschau und sichere einmalige Einfügung. Erst danach folgen Pausenerkennung, laufende Teilvorschau, Bereinigt, Smart mit Selbstkorrekturen, Professionell, Übersetzen und eigene Regeln. [Diktat](VOICE_DICTATION.md) · [Modelle](MODELS.md)
4. **Modellverwaltung und lokale KI.** Download, Fortsetzen, Abbruch, Prüfsumme, Import, Speicherprüfung und Auswahl fertigstellen. Lokale KI mit llama.cpp, begrenzter Laufzeit und Abbruch getrennt von der Tastatur. Prüfe Texttreue auf DE/EN und nenne nur auf Android getestete Modelle im Katalog.
5. **Cloud-KI und Schreibwerkzeuge.** Eigene API-Schlüssel, verschlüsselte Ablage, ausdrückliche Textfreigabe, Anbieteradapter und sichere Vorschau mit Rücknahme. Ergänze Rechtschreibung, Ton, Übersetzen, Kürzen, Zusammenfassen, Formatieren und eigene Anweisungen. Die Auswertung darf nie nach einem Feldwechsel in ein anderes Feld schreiben. [Bearbeitung](AI_ARCHITECTURE.md) · [Anbieter](PROVIDERS.md)
6. **Produktivität, Sicherheit, Leistung.** Vorlagen, eigene Wörter, App-Profile und Zwischenablage-Aktionen priorisieren. OCR nur nach ausdrücklicher Bildauswahl. Teste fehlerhafte Wörterbücher/Modelle, Protokolle, Eingabefeldwechsel, Berechtigungen, Abbruch und bösartige Anweisungen im Text. Miss Leistung und Speicher auf echten Geräten. [Leistungsmessung](BENCHMARKS.md)
7. **Veröffentlichung.** Dauerhafte Signierung, unabhängig wiederholter Build, Quellarchiv, APK-Prüfsummen, Gerätetestmatrix, [Funktionsvergleich](FEATURE_PARITY.md) und [Veröffentlichungsschritte](RELEASE.md) vervollständigen. Für Obtainium und F-Droid getrennt prüfen. Eine stabile Version setzt bestandene Funktions- und Sicherheitsprüfungen voraus.

## Bekannte technische Hinweise

- Java 25 auf dem bisherigen Fedora-Rechner hatte keinen Compiler. Das vorhandene vollständige JDK 17 funktioniert. Robolectric mit Android 36 würde Java 21 benötigen. Die lokalen Tests nutzen API 34.
- Android Gradle Plugin 9.1.1 meldet, dass SDK 37.2 außerhalb seiner getesteten SDK-Version liegt. Der lokale Build läuft. Bei einem späteren Werkzeugwechsel zuerst einen reproduzierbaren Vergleich bauen.
- Der Emulator startete auf diesem Host mit `-gpu host -feature -Vulkan`. Softwaregrafik und `-gpu off` brachen beim Start ab. Bei gemeldeter Hardwaretastatur musste die Bildschirmtastatur zusätzlich aktiviert werden.
- Das Paket verwendet weiter die internen Java-Paketnamen von HeliBoard. Ein anderer Name für die installierte App steht in `applicationId`. Bewahre diese Trennung, solange native Bindungen die internen Namen voraussetzen.

## Letzter lokaler Prüflauf

- `./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest` war erfolgreich. 184 Tests erfasst, 179 bestanden, 5 bewusst übersprungen. Vier davon prüfen öffentliche URLs. Einer betrifft den bekannten Hangul-Kompositionsfehler aus HeliBoard.
- Die CI-Vorlage (automatischer Build und Tests auf GitHub) liegt in `tools/ci/android.yml`. Die aktuelle GitHub-Anmeldung hat keinen `workflow`-Umfang zum Hochladen aktiver GitHub-Abläufe. Sobald der Zugriff dafür vorhanden ist, prüfe die Vorlage und lege sie unter `.github/workflows/android.yml` ab. Bis dahin gelten die lokalen Prüfergebnisse.
- App-APK und Compiler-Ausgaben gehören nicht in Git. Die Wege zum erneuten Bauen und Testen stehen in [TESTING.md](TESTING.md) und [RELEASE.md](RELEASE.md).
- Die Git-Prüfung gegen den festgelegten HeliBoard-Quellstand meldete keine neu eingebrachten Leerzeichenfehler. Beim Vergleich mit dem ursprünglichen leeren Projekt fallen unveränderte Leerzeichen und Zeilenenden aus dem Quellimport auf. Die Originaldateien wurden dafür nicht umformatiert.
