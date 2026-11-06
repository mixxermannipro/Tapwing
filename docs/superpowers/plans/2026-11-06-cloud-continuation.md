# Cloud continuation implementation plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Eine reproduzierbare Tapwing-Umgebung und ein geprüfter nächster Schritt aus docs/PROGRESS.md.

**Architecture:** Auf einem eigenen Zweig von main die vorhandenen PR-Stände #2, #4 und #5 zusammenführen. Datenschutzvorgaben aus main erhalten. Den bestehenden asynchronen Wischpfad gegen Feldwechsel während der Geste prüfen und absichern.

**Tech Stack:** JDK 17, Gradle 9.7.1, Android SDK 37.2, Build-Tools 36.0.0, NDK 28.2.13676358, Kotlin/Java, JUnit/Robolectric.

**Spec:** docs/PROGRESS.md, insbesondere „Tippqualität und Wischen absichern“, und AGENTS.md.

## Constraints

- main und bestehende Entwicklungszweige nicht verändern; Änderungen als separater PR.
- Keine Netzwerk- oder Mikrofonberechtigung und keine Eingabeprotokolle ergänzen.
- Keine lokalen Pfade, SDKs, APKs oder Schlüssel in Git.
- Sichtbaren Namen Tapwing verwenden; interne Paketkennung für Updates erhalten.

## Review focus

- Ein Ergebnis darf nach einem Feld-, App-, Layout- oder Cursorwechsel keinen Text einsetzen.
- Ein veralteter Worker-Rückruf darf eine neuere Geste nicht beenden.
- Abbruch vor und nach Worker-Antwort muss funktionieren.
- Fehlender Editor, fehlende Verbindung und geschlossene Eingabeansicht müssen sicher abbrechen.
- Dokumentierte Tests müssen zum aktuellen Stand gehören; physische Geräte nicht als geprüft ausweisen.

## Task 1: Integration und Entwicklungsumgebung

Files: AGENTS.md, README.md, PRIVACY.md, CONTRIBUTING.md, SECURITY.md, .gitignore, tools/dev/README.md, docs/THIRD_PARTY_LICENSES.md.

- [x] PR-Stände in den eigenen Zweig integrieren und Textkonflikte auflösen.
- [x] JDK und festgelegte Android-Pakete lokal installieren; Proxy und CA-Vertrauen lokal konfigurieren.
- [ ] Vollständigen Debug-/Release-Build und vorhandene Unit-Tests ausführen.
- [x] Reproduzierbare Einrichtung und Grenzen dokumentieren.

## Task 2: Feldwechsel während der Wischeingabe

Files: app/src/main/java/helium314/keyboard/latin/inputlogic/InputLogicHandler.java; app/src/test/java/helium314/keyboard/latin/inputlogic/OpenSwipeLifecycleTest.java.

Interface: Bestehende onStartBatchInput(), finishSwipe(InputPointers, CapsMode, int), cancelSwipe().

- [x] Tests für unverändertes Feld, Feldwechsel während der Geste und verspätete Ergebnisse schreiben. Erwartung: nur im unveränderten Feld eine Einfügung.
- [x] Neue Feldwechseltests am bisherigen Code fehlschlagen sehen.
- [x] Identität von Editor und InputConnection zu Gestenbeginn erfassen und vor dem Decode erneut vergleichen; bei Abbruch freigeben.
- [ ] Lebenszyklustests grün ausführen; gesamte Suite und Builds prüfen.

## Task 3: Übergabe und PR

Files: docs/PROGRESS.md, docs/TESTING.md, docs/ARCHITECTURE.md, docs/REPOSITORY_STATUS.md.

- [x] Aktuellen Repository-Namen, PR-Abhängigkeiten und nachgewiesene CI-Ergebnisse dokumentieren.
- [ ] Veraltete Schlussangaben im Fortschritt ersetzen; neue Ergebnisse samt Grenzen eintragen.
- [ ] Wörterbuchprüfung, Lint und Diff-Prüfung ausführen; Befunde beheben oder konkret dokumentieren.
- [ ] Separaten Zweig pushen und einen überprüfbaren Entwurfs-PR gegen main öffnen.
