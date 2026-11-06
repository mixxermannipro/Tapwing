# Tastatur und Eingabefelder

`LatinIME` ist der Android-Dienst für die Tastatur. `PointerTracker` gibt Tastenereignisse an die Eingabelogik weiter. `InputLogic` verwaltet den gerade getippten Text, Korrekturen und die Übergabe über `RichInputConnection`. Die Tastatur zeichnet ihre Ansicht nativ.

Die Wörterbuchbibliothek wird aus C++ gebaut und mit der APK installiert. Sie lädt lokale Wörterbücher für Deutsch, US-Englisch und UK-Englisch. Tippvorschläge und Lernwörter brauchen keinen KI-Dienst.

`InputAttributes` wertet den Android-Feldtyp aus. Passwortfelder unterdrücken Vorschläge und Korrekturen. Die Android-Markierung gegen personalisiertes Lernen wird auch bei Zahlenfeldern gelesen. Die Zwischenablage prüft diese Bedingungen vor automatischen Zugriffen.

## Offene Wischeingabe

Über „Gesten-Tippen“ lässt sich die Wischeingabe für Deutsch und Englisch einschalten. Die Spur erscheint während der Geste. Der geometrische CleverKeys-Decoder liefert das Wort und Alternativen nach dem Loslassen. Die App liest dafür nur die eingebauten Wortlisten. Pro Arbeitsthread bleiben jeweils eine Wortliste und ein Layout-Index im Speicher.

`SwipeLayout` übernimmt die Tastenpositionen aus der aktuell gezeichneten Tastatur. Umlaute können über ihre Grundbuchstaben erkannt werden. Wenn ß keine eigene Taste hat, benutzt der Decoder die Position von S. Das Ergebnis behält die Wortschreibweise aus der Liste.

`InputLogicHandler` prüft nach der Berechnung noch einmal die Geste, das Eingabefeld, die Verbindung, das Layout und die Cursorposition. Abbruch, neue Eingabe, Feld- oder Cursorwechsel verwerfen ausstehende Ergebnisse. Erst ein gültiges Wort ändert den Text. Passwort/PIN, Inkognito und Felder ohne Vorschläge sind ausgenommen. Diese Fassung verarbeitet einzelne Wörter und einen Finger.
