# VOICE-01 · Diktatsitzung und Anbietergrenzen

**Entscheidung am 7. November 2026.** Tapwing trennt Aufnahme, Spracherkennung,
Bereinigung und Einfügen. Dieselbe Sitzung nimmt lokale und optional freigegebene
externe Anbieter an. Sie greift auf keine Android-Eingabeverbindung zu.

## Datenfluss

1. Der spätere Controller der Tastatur prüft den Editor und die bewusste Nutzeraktion.
   Die Sitzung beginnt ohne freigegebenen Editor. Die Freigabe erhält einen neuen
   internen Schlüssel, auch wenn zwei aufeinanderfolgende Felder denselben Typ haben.
2. `SpeechProvider` bekommt Sprache, eine Audioquelle und gegebenenfalls ausdrücklich
   freigegebene Wörter. Aufnahme durch Tapwing liefert 16-Bit-Mono-PCM mit 16 kHz.
   Ein Android-Systemerkenner kann stattdessen sein eigenes Mikrofon verwalten.
   Die Schnittstelle überträgt die Verantwortung für jedes Audio-Paket an den
   Empfänger. Sie benötigt weder Pfade noch Dateien. Der spätere Recorder begrenzt
   den Speicher und schließt den Audiostrom beim Stoppen.
3. Der Anbieter meldet Zwischenstand, endgültiges Ergebnis oder einen Fehlercode.
   Zeitbereich und Konfidenz sind optional. Ein Zwischenstand überschreibt den
   vorherigen Zwischenstand. Das erste endgültige Ergebnis beendet die Erkennung.
   Segment-Ergebnisse fügen die Anbieter vor ihrem endgültigen Ergebnis zusammen.
4. Ein separat gewählter `CleanupProvider` kann den erkannten Text bearbeiten.
   Das Original bleibt in der Vorschau. Scheitert die Bereinigung, bleibt das
   Original mit Warnung verfügbar. Scheitert die Erkennung oder fehlt ein
   endgültiges Ergebnis, gibt es keine Vorschau zur Übernahme.
5. Eine gültige Vorschau kann einmal abgeholt werden. Der künftige IME-Controller
   prüft beim Einfügen erneut Editor, Eingabeverbindung, Auswahl und Original.
   Erst dieser Controller darf Text schreiben. Die sichere Übernahme und
   Rücknahme gehören zu #18 und #32.

Der Controller und die Sitzung verwenden denselben Thread, später den
Android-Hauptthread. Anbieter verschieben blockierende Aufnahme, Modell- und
Netzarbeit in den Hintergrund. Die Sitzung speichert keine Nutzerdaten auf
Datenträgern und gibt keine Anbieter-Fehlermeldungen mit Nutzerdaten weiter.

## Android-Lebenszyklus und Datenschutz

| Ereignis | Vertrag für den Android-Controller |
| --- | --- |
| Eingabestart, Neustart, Feld- oder Appwechsel | Laufende Arbeit und Vorschau sofort verwerfen. Den bisherigen Schlüssel vor eventuell verzögerten IME-Aufrufen ungültig machen. Neue Felder einzeln klassifizieren. |
| Passwort, PIN, OTP, Inkognito, kein personalisiertes Lernen oder unklarer Editor | Keine Aufnahme, keine Bereinigung, keine Vorschauübernahme. Frisches `EditorInfo` und die aktuelle Inkognito-Einstellung prüfen. |
| Tastatur ausgeblendet, App im Hintergrund, Bildschirm aus | Editorfreigabe und Sitzung beenden. Mikrofon und laufende Anbieterarbeit abbrechen. |
| Mikrofon verweigert, belegt oder unterbrochen | Aufnahme nicht beginnen oder sofort beenden. Fehlercode melden, Texteingabe bleibt möglich. |
| Nutzer bricht ab oder beendet die Sitzung | Abbruch verwirft Zwischenstand und Vorschau und bricht Ressourcenarbeit ab. Stoppen beendet nur den Audiostrom, damit der Anbieter die endgültige Erkennung liefern kann. |
| Prozess-Tod | Betriebssystem beendet Aufnahme und Arbeit. Kein automatischer Neustart, keine Wiederherstellung eines Mitschnitts. |

Die Sitzung weist unbekannte oder gesperrte Editoren ab. Externe und bezüglich
Netzübertragung unbekannte Anbieter brauchen eine getrennte Freigabe für Erkennung
und Bereinigung. Freigaben gelten für den konkreten Empfänger und Auftrag.
Zur Freigabe gehören später auch die tatsächlich übertragenen Audio-, Text- und
Wörterbuchdaten. Standard ist lokal. Es gibt keinen automatischen Wechsel zu
einem anderen Anbieter und keinen automatischen Neuversand. Android-Berechtigungen,
Netzregeln, Laufzeitgrenzen und Aufnahmeunterbrechungen folgen in den jeweiligen
Umsetzungstickets.

`Cancelled` bedeutet, dass Ergebnisse logisch verworfen sind und der Abbruch
angefordert wurde. Der Recorder in #8 muss die tatsächliche Freigabe abwarten,
bevor er das Mikrofon für eine neue Sitzung öffnet.

## Anbieter zur Prüfung

Stand der verlinkten Herstellerdokumentation am 7. November 2026. Deutsch und
Englisch sind das Ziel. Dialektqualität, Tempo und Ergebnisqualität wurden in
Tapwing für keinen Kandidaten gemessen. Preise sind Beispiele in US-Dollar und
können sich ändern. Bei lokalen Kandidaten fallen keine API-Kosten an.

| Kandidat und Primärquelle | DE/EN, Zwischenstand und Verarbeitung | Kosten, Grenzen, Modell und Schlüssel |
| --- | --- | --- |
| [whisper.cpp](https://github.com/ggml-org/whisper.cpp) | Mehrsprachige Modelle. Lokale Erkennung. Laufende Vorschau erfordert Verarbeitung überlappender Fenster. | Kein Upload und kein Schlüssel. Tiny- und Base-Kandidaten mit etwa 32 bzw. 60 MB stehen unter [Modelle](MODELS.md). Bibliothek MIT. Exakte Gewichte, Lizenz und Android-Speicherbedarf vor Übernahme prüfen. |
| [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) | Lokale Streaming- und Offline-Erkenner. DE/EN und Zwischenergebnisse hängen vom konkreten Modell ab. | Kein Upload und kein Schlüssel. Bibliothek Apache-2.0. Größe, Lizenz und Sprachabdeckung je Modell prüfen. |
| [Android SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer) | Erkenner und verfügbare Sprachen sind geräteabhängig. Expliziter On-Device-Erkenner ab API 31, wenn installiert. Zwischenstände sind optional. | Kein eigener Schlüssel für die Android-Schnittstelle. Normaler Systemdienst kann Audio übertragen und gilt bis zur Prüfung als extern. [Externe Audioquelle](https://developer.android.com/reference/android/speech/RecognizerIntent#EXTRA_AUDIO_SOURCE) ab API 33, Unterstützung hängt vom Dienst ab. |
| [Groq Speech-to-Text](https://console.groq.com/docs/speech-to-text) | Mehrsprachiges Whisper mit Datei-Upload. Zeitmarken sind möglich. Die dokumentierte Datei-API liefert keinen laufenden Mikrofon-Zwischenstand. | Eigener Schlüssel. Direkter Anhang bis 25 MB. V3 Turbo laut Anbieter 0,04 USD pro Audiostunde bei mindestens zehn Sekunden Abrechnung. Cloud-API, keine lokalen Gewichte. |
| [OpenAI File Transcription](https://developers.openai.com/api/docs/guides/speech-to-text), [Realtime Transcription](https://developers.openai.com/api/docs/guides/realtime-transcription) | Mehrsprachige Datei- und Echtzeiterkennung. Streaming aus einer schon vollständigen Datei unterscheidet sich vom Live-Mikrofon. | Eigener Schlüssel. Dateigrenze 25 MB. Abrechnung hängt vom Modell ab. [Preise](https://developers.openai.com/api/docs/pricing) |
| [Gemini Transcribe](https://ai.google.dev/gemini-api/docs/transcribe), [Live Transcribe](https://ai.google.dev/gemini-api/docs/live-api/live-transcribe) | Getrennte Datei- und Echtzeit-Erkennung. Live dokumentiert laufende Textausgabe und Mono-PCM mit 16 kHz. | Eigener Schlüssel. Live-Sitzungen laut Doku bis zehn Minuten. Anbieter schätzt etwa 0,005 USD pro Minute für Datei bzw. 0,009 USD für Live im bezahlten Tarif. [Preise und Datennutzung](https://ai.google.dev/gemini-api/docs/pricing) |
| [OpenRouter Speech-to-Text](https://openrouter.ai/collections/speech-to-text-models/) | Datei-Endpunkt für ausgewählte Modelle. DE/EN, Zwischenstände und Zeitmarken müssen je gewähltem Modell geprüft werden. | Eigener Schlüssel. Preis und Limits je Modell und nachgelagertem Anbieter. Route vor Upload offenlegen. |
| [Anthropic Claude](https://platform.claude.com/docs/en/models/overview) | Kandidat für nachgelagerte Textbereinigung. Nur der freigegebene Text würde übertragen. | Eigener Schlüssel. [Textpreise und Modellgrenzen](https://platform.claude.com/docs/en/about-claude/pricing) |

Für [Google Rambler](https://support.google.com/pixelphone/answer/17468539) ist
hier kein autorisierter Zugang belegt. #15 prüft offizielle Android-APIs.
Es wurden keine Anbieter-SDKs, Modelle oder proprietären Ressourcen übernommen.
Vor einer Aktivierung werden bei lokalen Modellen Version, Herkunft und Rechte
geprüft. Bei Cloud-Anbietern werden Kosten, Limits, Zielregion, Datennutzung
und Aufbewahrung erneut geprüft. Ein Fehler bleibt beim gewählten Anbieter.

## Abhängigkeiten

- #8 implementiert als Nächstes Aufnahme, Ressourcenfreigabe und Android-Lebenszyklus.
  #11 folgt nach der Berechtigungsentscheidung in #43. #9 braucht #8 und #11.
- #12 kann nach #8 eine Vorschau mit Status anbieten. Die Einfügefreigabe hängt von
  #18 und #32 ab. Die beiden Tickets verweisen aufeinander. Die gemeinsame
  Feldprüfung zuerst isoliert testen, anschließend Editor- und Diktat-Anbindung
  getrennt prüfen.
- #15 prüft nach #45 den offiziellen Systemerkenner. #14 hängt zusätzlich von #37,
  #38 und #43 ab. #13 braucht #8, das freigegebene Vokabular aus #22 und #35.
- #16 definiert vor der Modellwahl das Messprotokoll. Für die vollständige Messung
  braucht es die lauffähigen Kandidaten aus #9, #10, #13 und #14. #10 nutzt einen
  ersten Vergleich aus #16 für die Integrationsentscheidung.
- #17 folgt nach #12, #18 und #32. #19 folgt nach #8, #9, #10 und #16.

## Prüfen

```sh
./gradlew :app:testDebugUnitTest --tests '*DictationSessionTest'
```

Der Test nutzt kontrollierte Anbieter statt Mikrofon, Modell oder Netzwerk.
Er prüft unter anderem getrennte Empfängerfreigaben, Abbruch und späte
Ergebnisse, Fehlercodes sowie die einmalige Abholung der Vorschau.
