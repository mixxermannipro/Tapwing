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
im Arbeitsspeicher gehalten. Die App erhält mit diesem Schritt keine zusätzliche
Berechtigung. Der Aufnahmebaustein ist noch nicht mit der Mikrofontaste verbunden.

### Offene Android-Prüfung

1. Die Mikrofontaste über einen Controller an sichtbare und zulässige Eingabefelder
   binden. Beim ersten Tipp die Systemberechtigung anfragen. Nach Rückkehr aus
   dem Dialog einen neuen Tipp verlangen.
2. Im Testfeld Start, Pause, Fortsetzen, Stopp und Abbruch prüfen. Beim Wechsel
   von Feld, App, Bildschirmzustand oder Tastaturansicht sofort abbrechen.
   Alte Anbieterantworten dürfen kein anderes Feld ändern.
3. Auf physischen Geräten verweigerte Berechtigung, Anruf, Mikrofonkonflikt und
   Wechsel zwischen eingebautem Mikrofon und Headset testen.
4. Auf einem physischen Gerät 100 Start-Stopp-Zyklen fahren. Pro Zyklus die Zeit
   vom Tipp bis zur ersten gültigen PCM-Probe messen. Median, 95. Perzentil,
   Fehler und Freigabe der Audioquelle dokumentieren. Nur Testton verwenden.
