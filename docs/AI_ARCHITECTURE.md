# KI-Schreibwerkzeuge

Geplante Schnittstelle. Im aktuellen APK sind keine KI-Schreibwerkzeuge aktiv.

1. Der Nutzer wählt Text und eine Aktion. Die App merkt sich das Feld, die aktuelle Eingabesitzung und die genaue Auswahl.
2. Der ausgewählte Dienst erhält den ausdrücklich freigegebenen Text. Er darf die Tastatur nicht blockieren und kann abgebrochen werden.
3. Die Tastatur zeigt eine Vorschau mit Original und Ergebnis.
4. Beim Anwenden prüft sie Verbindung, Feld, Auswahl und Text erneut. Ist eine Angabe nicht prüfbar oder hat sie sich geändert, bleibt nur die Vorschau. Android bietet für fremde Textfelder keine atomare Prüfung mit gleichzeitigem Ersetzen.
5. Die Tastatur bietet eine sichere Rücknahme im selben Feld an. Der Originaltext wird nur für diesen Zweck kurz im Speicher gehalten.

Lokale Ausführung erhält kleine Aufträge und ein Zeitlimit. Cloud-Verarbeitung muss ausdrücklich aktiviert werden und zeigt den Empfänger vor dem Senden an. Bearbeiteter Text ist Eingabedaten. Er darf die Systemregeln des Auftrags nicht ersetzen. [Android InputConnection](https://developer.android.com/reference/android/view/inputmethod/InputConnection)
