# Technische Grundlage

**Entscheidung:** HeliBoard 4.2, Quellstand `0c60dffb03a4e2e7a37cb82210f5299f93c03979`. Tapwing übernimmt die native Eingabe und nutzt die eigene Android-Kennung `io.github.mixxermannipro.satzfink`. Die Java-Pakete bleiben vorerst gleich, weil Ressourcen und native Bindungen sie verwenden. [Quellstand](https://github.com/HeliBorg/HeliBoard/tree/0c60dffb03a4e2e7a37cb82210f5299f93c03979)

| Kriterium | HeliBoard 4.2 | LeanType | AOSP LatinIME |
| --- | --- | --- | --- |
| Texteingabe | Gepflegte Engine, deutsche und englische Wörterbücher | Dieselbe Basis mit zusätzlichen Änderungen, Wörterbücher aus APKs ausgeschlossen | Dieselbe historische Engine, älteres eigenständiges Projekt |
| Bedienung | Native Ansicht, Toolbar, Emoji, Zwischenablage und flexible Layouts | Mehr Funktionen, mehr Abhängigkeiten und Zustände | Weniger heutige Bedienfunktionen |
| Sicherheit und Wartung | Überschaubarer Ausgangspunkt. Laden fremder Bibliotheken und automatische Sammlung mussten entfallen | Externe APKs werden im Tastaturprozess ausgeführt, Cloud-Schlüssel können in Klartext landen | Moderner eigenständiger Build und Bedienung müssten nachgerüstet werden |
| Leistung | Schlanker Pfad vom Antippen bis zum Wörterbuch | Lokales Modell wird beim Öffnen der Tastatur vorgeladen | Schlanker Pfad, zusätzlicher Aufwand für heutige Android-Versionen |
| Lizenz und Wischen | GPL-3.0 mit AOSP-Hinweisen. Offenes Wischen muss ergänzt werden | GPL-3.0 mit AOSP-Hinweisen. Offenes Wischen muss ergänzt werden | Apache-2.0. Offener Wischdecoder fehlt |
| Aufwand für KI | Getrennt ergänzen, ohne den Tippweg umzubauen | Mehr vorhandener KI-Code, erheblicher Umbau der Sicherheitsgrenzen nötig | Mehr Arbeit vor der KI-Anbindung |

Der unveränderte HeliBoard-Build traf lokal auf das fehlende NDK 28.0. Tapwing verwendet das vorhandene NDK 28.2 und SDK 37.2. LeanType ließ sich für den Vergleich mit einer angepassten NDK-Version bauen. Der zugrunde liegende Quellvergleich steht unter [UPSTREAM_RESEARCH.md](UPSTREAM_RESEARCH.md). Die Tabelle enthält keine gemessenen Leistungswerte.

## Grenzen zwischen Komponenten

- Die Tastatur besitzt die Android-Eingabeverbindung. Nur diese Komponente darf Text im aktiven Feld ändern.
- Wörterbuchsuche und Vorschläge laufen lokal. Wischen bekommt einen getrennten Decoder und nutzt denselben Vorschlagsstreifen.
- Diktat und KI liefern Vorschläge. Die Tastatur hält diese in einer Vorschau. Sie prüft Feld, Auswahl und Originaltext vor dem Einfügen erneut.
- Modellverwaltung prüft heruntergeladene Daten vor der Freigabe. Modelle enthalten Gewichte als Daten. Die App lädt keine APKs oder nativen Bibliotheken aus Downloads.
- Die derzeitige App enthält weder Netzwerk- noch Mikrofonberechtigung. Eine spätere Cloud-Variante braucht ausdrückliche Auswahl und eine geprüfte Trennung von der normalen Eingabe.

[Android-Dokumentation zu Eingabemethoden](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method)
