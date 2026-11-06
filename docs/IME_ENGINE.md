# Tastatur und Eingabefelder

`LatinIME` ist der Android-Dienst für die Tastatur. `PointerTracker` gibt Tastenereignisse an die Eingabelogik weiter. `InputLogic` verwaltet den gerade getippten Text, Korrekturen und die Übergabe über `RichInputConnection`. Die Tastatur zeichnet ihre Ansicht nativ.

Die Wörterbuchbibliothek wird aus C++ gebaut und mit der APK installiert. Sie lädt lokale Wörterbücher für Deutsch, US-Englisch und UK-Englisch. Tippvorschläge und Lernwörter brauchen keinen KI-Dienst.

`InputAttributes` wertet den Android-Feldtyp aus. Passwortfelder unterdrücken Vorschläge und Korrekturen. Die Android-Markierung gegen personalisiertes Lernen wird auch bei Zahlenfeldern gelesen. Die Zwischenablage prüft diese Bedingungen vor automatischen Zugriffen.

Für Wischen bleiben Touch-Erfassung und Vorschlagsanzeige als Anschlusspunkte erhalten. Nächster Schritt ist der geometrische CleverKeys-Decoder. Er bekommt eine Kopie von Tastenpositionen, Wischpfad und passenden Wörterbucheinträgen. Vorschläge verfallen bei Feldwechsel, Layoutwechsel oder Abbruch. Leere Ergebnisse dürfen kein früheres Tippwort einsetzen.
