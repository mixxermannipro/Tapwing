# Diktat

Das aktuelle APK bietet noch kein bedienbares Diktat. Die Verträge für
Spracherkennung, Bereinigung und Vorschau sind mit Testanbietern geprüft.
Das erste Ziel ist wortgetreue lokale Erkennung für Deutsch und Englisch.
[Datenfluss und Anbieter](VOICE_SESSION_ADR.md)

- Mikrofon erst nach einem Tipp einschalten. Aufnahme im Speicher begrenzen und bei Abbruch, ausgeblendeter Tastatur oder Feldwechsel stoppen.
- Ton als Mono-PCM mit 16 kHz an den lokalen Erkenner geben. Status, Dauer und Pegel bleiben in der Tastatur sichtbar.
- Zwischenergebnisse in der Tastatur anzeigen. Text erst nach Prüfung des aktiven Felds einmal einsetzen.
- Bereinigung als eigenen Schritt prüfen. Deutsche Testbeispiele müssen Selbstkorrekturen wie „um acht, nein, um neun“ sowie Namen, Nummern, URLs und Verneinungen abdecken. Original und Vorschau bleiben bis zur Bestätigung verfügbar.
- Nach „Wortgetreu“ folgen „Bereinigt“, „Smart“, „Professionell“, „Übersetzen“ und „Eigene Regeln“. Kein Modus gilt vor Prüfung als unterstützt.

[Android und gemeinsam genutztes Mikrofon](https://developer.android.com/media/platform/sharing-audio-input)

## Aufnahmebaustein für VOICE-02

Der getrennte Aufnahmebaustein liefert PCM-Daten über `SpeechInput.CapturedAudio`.
Ein Hintergrund-Worker besitzt die Audioquelle. Er öffnet höchstens eine Quelle
pro Recorder und gibt sie vor der nächsten Aufnahme frei. Pause schließt die
Quelle. Fortsetzen öffnet sie erneut. Stopp beendet den Audiostrom und lässt
bereits gepufferte Daten abfließen. Ein Coroutine-Abbruch verwirft die Sitzung.

Der Android-Adapter prüft die Mikrofonberechtigung vor `AudioRecord`. Er
versucht zuerst 16 kHz und fällt bei fehlender Unterstützung auf 44,1 kHz mit
Umrechnung zurück. Ein Anruf, eine stummgeschaltete Aufnahme, ein erkannter
Wechsel der Audioquelle oder ein Lesefehler beendet den Audiostrom. Für reine
Aufnahme wird kein Wiedergabefokus angefordert. Die Geräteeignung der Umrechnung
und Erkennung muss gemessen werden.
[AudioRecord](https://developer.android.com/reference/android/media/AudioRecord)

Der Puffer ist auf 16 Audio-Pakete begrenzt. Bei Überlauf bricht die Aufnahme mit
einem Fehler ab. Die Vorgabe pro Sitzung sind maximal 120 Sekunden Nutzsignal.
Anbieter sollen die Pakete ohne stilles Wegwerfen weiterverarbeiten. Audio wird
im Arbeitsspeicher gehalten.

### Mikrofontest in der Debug-Tastatur

Installiere Tapwing Debug und die separat gebaute Test-App aus
`tools/ime-smoke/build.sh` mit demselben Debug-Schlüssel. Wähle Tapwing Debug
und starte die Test-App im Aufnahmemodus.

```sh
adb shell am start -n io.github.mixxermannipro.satzfink.smoke/.FixtureActivity --ez voiceCapture true
```

Öffne im Feld „Normal text“ die Tastatur und tippe auf „Mic test“. Beim
ersten Tipp fragt Android nach der Berechtigung. Nach der Freigabe ist ein
neuer Tipp erforderlich. Die Testleiste zeigt Start, Pause, Fortsetzen,
Stopp, Abbruch und die Zeit bis zum ersten PCM-Paket an. Die Pakete werden
verworfen. Die Testleiste ist auf das markierte Testfeld der gleich
signierten Test-App begrenzt und verschwindet bei geschützten Feldern.
Nur die Debug-APK hat eine Mikrofonberechtigung.

Prüfe mit `adb shell cmd appops get io.github.mixxermannipro.satzfink.debug RECORD_AUDIO`,
ob die Aufnahme läuft. Bei Pause, Stopp, Wechsel ins Privatfeld, Ausblenden
der Tastatur und Ausschalten des Bildschirms muss der Status „running“ enden.

### Offene Android-Prüfung

- Auf physischen Geräten Berechtigungsverweigerung, Anruf, Mikrofonkonflikt und
  Wechsel zwischen eingebautem Mikrofon und Headset testen.
- 100 reale Start-Stopp-Zyklen messen. Pro Zyklus die Zeit vom Tipp bis zur
  ersten PCM-Probe erfassen. Median, 95. Perzentil, Fehler und Freigabe
  dokumentieren. Nur Testton verwenden.
- Für die nutzbare Diktatfunktion Aufnahme mit einem Spracherkenner und einer
  geprüften Vorschau verbinden. Die sichere Textübernahme bleibt in #18 und #32.
