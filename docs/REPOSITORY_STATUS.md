# Repository-Stand am 6. November 2026

Repository: [mixxermannipro/Tapwing](https://github.com/mixxermannipro/Tapwing).
Frühere Verweise auf `mixxermannipro/openboard` stammen aus der Zeit vor der Umbenennung.

## Alle vorhandenen Pull Requests und Issues

| Eintrag | Zustand beim Sitzungsbeginn | Inhalt und Abhängigkeit |
| --- | --- | --- |
| [Issue #1](https://github.com/mixxermannipro/Tapwing/issues/1) | geschlossen | Projektdateien; der letzte Kommentar legt **Tapwing** als endgültigen Namen fest. |
| [PR #2](https://github.com/mixxermannipro/Tapwing/pull/2) | offener Entwurf | Android-Grundlage auf main; nach #3 Textkonflikte in sechs Projektdateien. |
| [PR #3](https://github.com/mixxermannipro/Tapwing/pull/3) | zusammengeführt | Projektregeln, Lizenz und Datenschutz; Review ohne Befunde. |
| [PR #4](https://github.com/mixxermannipro/Tapwing/pull/4) | offener Entwurf | Lokale DE/EN-Wischeingabe auf #2; [Android-CI erfolgreich](https://github.com/mixxermannipro/Tapwing/actions/runs/42208242972). |
| [PR #5](https://github.com/mixxermannipro/Tapwing/pull/5) | offener Entwurf | Graphit-Design und sichtbarer App-Name Tapwing auf #4; [Android-CI erfolgreich](https://github.com/mixxermannipro/Tapwing/actions/runs/42208867916). |

Die Diskussionen aller vier PRs und des Issues wurden gelesen. #2, #4 und #5
hatten keine Review-Kommentare. Für #2 lag kein eigener CI-Lauf am Kopfcommit vor.
Es gab zu Sitzungsbeginn keine offenen Issues.

## Fortsetzung

`chore/cloud-continuation` beginnt auf main (`1d4d978`) und integriert den
vollständigen Stand von #5 (`0c22c78`, enthält #2 und #4). Die vorhandenen
Entwicklungszweige und PRs bleiben unverändert. Die Integration löst die
Konflikte in `.gitignore`, `AGENTS.md`, `CONTRIBUTING.md`, `PRIVACY.md`,
`README.md` und `SECURITY.md`. Datenschutzregeln aus main bleiben erhalten;
Struktur, Befehle und aktuelles Verhalten ersetzen ungeprüfte Annahmen.

Die Übergabe priorisiert Tippqualität und Schutz vor verspäteten Wischergebnissen.
Deshalb wird zuerst der Wisch-Lebenszyklus geprüft. Diktat und KI bleiben die
nächsten größeren Meilensteine, nach der Absicherung des Eingabekerns.

Die interne App-Kennung `io.github.mixxermannipro.satzfink` bleibt für
Installation und Update-Kompatibilität erhalten. Produktname ist Tapwing.

Aktuelle Build-/Testergebnisse und verbleibende Geräteprüfungen:
[PROGRESS.md](PROGRESS.md). Einrichtung: [tools/dev/README.md](../tools/dev/README.md).

Die Fortsetzung ist als [Entwurfs-PR #6](https://github.com/mixxermannipro/Tapwing/pull/6)
gegen main geöffnet. Die unabhängige Prüfung des neuen Deltas meldete keine Befunde;
Build- und Testergebnisse werden in PROGRESS.md fortgeschrieben.
