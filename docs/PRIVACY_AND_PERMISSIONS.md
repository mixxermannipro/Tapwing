# Datenschutz und Berechtigungen

- `VIBRATE` steuert die fühlbare Rückmeldung.
- `RECEIVE_BOOT_COMPLETED` gehört zum übernommenen Startpfad für die Eingabemethode.
- `READ_USER_DICTIONARY` und `WRITE_USER_DICTIONARY` sind ältere Systemberechtigungen für das persönliche Wörterbuch. Android kann sie für gewöhnliche Apps einschränken.
- `BIND_INPUT_METHOD` schützt den Systemdienst. `BIND_TEXT_SERVICE` schützt die Rechtschreibprüfung. Beide sind Anforderungen an den aufrufenden Systemdienst.
- Die Zwischenablage kann freigegebene Dateien über einen nicht exportierten Android-Dateianbieter an ein Zielprogramm geben. Die Speicherung von Dateien im Verlauf ist standardmäßig aus.

Satzfink entfernt die Kontaktberechtigung, die automatische Sicherung, die aktive Erfassung von Gestendaten und den Import ausführbarer Gestenbibliotheken. Explizite Wörterbuchimporte bleiben möglich. Der vorhandene native Parser muss vor einer stabilen Version gegen beschädigte Dateien geprüft werden. Die Prüfung von Protokollen, Abhängigkeiten, Dateifreigaben, Eingabefeldwechseln und der nativen Verarbeitung steht noch aus.
