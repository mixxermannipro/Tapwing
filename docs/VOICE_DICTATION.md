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
