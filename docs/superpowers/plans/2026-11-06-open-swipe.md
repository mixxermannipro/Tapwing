# Erster Meilenstein für Wischeingabe

1. Die GPL-3.0-Kernklassen von CleverKeys auf dem geprüften Quellstand übernehmen. Ein kleines Erzeugungswerkzeug erstellt begrenzte Wortlisten aus den bestehenden DE/EN-Quellen. Tests für echte Wortlisten, QWERTY und QWERTZ ergänzen.
2. Unter `latin/swipe` aktuelle Tastenpositionen und Wischpunkte kopieren. Auf einem getrennten Arbeitsthread vorbereiten und erkennen. Zunächst nur nach dem Loslassen erkennen, mit sichtbarer Wischspur und Alternativen.
3. In `InputLogicHandler`, `InputLogic` und `LatinIME` nur das Ergebnis der aktuellen Geste im unveränderten Eingabefeld übernehmen. Bei Taste, Cursor- oder Feldwechsel, Abbruch und Layoutwechsel verwerfen. Passwörter, PINs, Inkognito und Felder ohne Vorschläge ausschließen. Die Einstellung standardmäßig ausschalten.
4. JVM-Tests, APK-Build, Inhaltsprüfung und einen Android-16-Test mit tatsächlichen Touch-Ereignissen ausführen. Ergebnisse und verbleibende Grenzen im Entwurf und in `docs/PROGRESS.md` festhalten.
