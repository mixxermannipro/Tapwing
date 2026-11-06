# Herkunft und Lizenzen

- App und Satzfink-Änderungen stehen unter [GPL-3.0](../LICENSE). Übernommen wurde HeliBoard 4.2. Die ursprünglichen Copyright- und SPDX-Hinweise im Code bleiben erhalten. [Quellstand HeliBoard](https://github.com/HeliBorg/HeliBoard/tree/0c60dffb03a4e2e7a37cb82210f5299f93c03979)
- Der von Android Open Source Project übernommene Code enthält Apache-2.0-Hinweise. [Apache-2.0](../LICENSE-Apache-2.0) und [AOSP NOTICE](../NOTICE-AOSP) liegen bei.
- Die ursprünglichen HeliBoard-Grafiken aus dem Quellarchiv tragen die bestehende [CC-BY-SA-4.0-Lizenz](../LICENSE-CC-BY-SA-4.0). Satzfink verwendet ein eigenes adaptives Startsymbol. Derzeit liegen keine zusätzlichen Schriftdateien oder KI-Gewichte bei.
- Die APK enthält drei eingebaute Wörterbücher. Ihre Quellwortlisten liegen unter [dictionaries/sources](../dictionaries/sources). Die weiteren HeliBoard-Wörterbücher wurden aus diesem Stand weggelassen, bis ihre Herkunft geprüft ist. Layouts sind von eingebauten Wörterbüchern unabhängig.

| Wörterbuch | Geprüfte Quellliste | SHA-256 der APK-Datei innerhalb von `assets/dicts` |
| --- | --- | --- |
| Deutsch | [OpenBoard e4fc435, Apache-LatinIME-Abstammung](https://github.com/openboard-team/openboard/commit/e4fc435b55c1096f94ea779843fd4ddf2fee11bc) | `ee5de04b6a93af67d2095a68ac1a8709ecdbf838cc5c98795f0b4ad2d192c2c7` |
| US-Englisch | [OpenBoard 206fc18, Apache-LatinIME-Abstammung](https://github.com/openboard-team/openboard/tree/206fc18ec35b55ddcb73464fa0b03bd31f1ff0d0/dictionaries) | `bd950ef4b57655120eee65cee62a5d216a63f721d9a8bb759ce2022437840443` |
| UK-Englisch | [AOSP LatinIME 127336e](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5/dictionaries/en_GB_wordlist.combined.gz) | `642ff3e5727e81d3a1ebdeddd9226dd7c17166bcd52f8f6e39f34b44be6faabd` |

Alle drei Dateien ließen sich aus den jeweiligen Listen bytegenau nachbauen. Entpacke die jeweilige Liste mit `gzip -dc` und rufe `java -jar dicttool_aosp.jar makedict -s <liste.combined> -d <zieldatei.dict>` auf. [Werkzeug und Quellcode](https://github.com/remi0s/aosp-dictionary-tools/tree/1e69dd2c04258e5e9e04bfb13b46faccf6a435b0)

AndroidX, Kotlin und die übrigen Bibliotheken sind in `app/build.gradle.kts` eingetragen. Eine vollständige maschinenlesbare Liste aller transitiven Bibliotheken und Hinweise steht vor einer stabilen Veröffentlichung noch aus.

## Wischerkennung

Die geometrische Wischerkennung stammt aus [CleverKeys, Stand 6e964a5](https://github.com/tribixbite/CleverKeys/tree/6e964a571c4664a2a5a8c5138f56337cad55667d), GPL-3.0. Der übernommene Kotlin-Code und der Copyright-Hinweis liegen unter `app/src/main/java/tribixbite/cleverkeys`. Die App enthält aus CleverKeys weder Modellgewichte noch Wortlisten.

`tools/generate-swipe-lexicons.py` erzeugt die komprimierten `.lex`-Dateien aus den oben genannten LatinIME-Quellwortlisten. Es entfernt markierte Nichtwörter und potenziell anstößige Einträge, lässt die Schreibweise stehen und sortiert nach Häufigkeit. `python3 tools/generate-swipe-lexicons.py --check` vergleicht die entpackten Wortlisten bytegenau. Verschiedene zlib-Versionen können dieselbe Liste unterschiedlich komprimieren.

Die Compose-UI-Tests verwenden AndroidX `ui-test-junit4` und einen debug-only
Test-Host aus `ui-test-manifest`, abgestimmt auf die bestehende Compose-BOM.
Sie gehören zu AndroidX (Apache-2.0); die Release-App benötigt den Test-Host nicht.
