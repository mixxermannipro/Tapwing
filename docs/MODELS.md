# Lokale Modelle

Satzfink enthält zurzeit keinen Modellkatalog. Die folgenden Modelle sind Recherchekandidaten. RAM, Geschwindigkeit und Ergebnisqualität auf Android sind noch offen.

| Kandidat | Format und Größe laut Anbieter | Quelle und Lizenz |
| --- | --- | --- |
| Whisper base-q5_1 | Mehrsprachige Spracherkennung, 59.707.625 Byte | [Whisper.cpp Gewichte, MIT](https://huggingface.co/ggerganov/whisper.cpp/tree/5359861c739e955e79d9a303bcbc70fb988958b1) |
| Whisper tiny-q5_1 | Mehrsprachige Spracherkennung, 32.152.673 Byte | [Whisper.cpp Gewichte, MIT](https://huggingface.co/ggerganov/whisper.cpp/tree/5359861c739e955e79d9a303bcbc70fb988958b1) |
| Qwen2.5-0.5B-Instruct Q4_K_M | Sprachmodell in GGUF, 491.400.032 Byte | [Modell, Apache-2.0](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/blob/9217f5db79a29953eb74d5343926648285ec7e67/qwen2.5-0.5b-instruct-q4_k_m.gguf) |

- [whisper.cpp, 727d3e7](https://github.com/ggml-org/whisper.cpp/tree/727d3e7666485e12450746b523bfb4e7f057cc5e) bietet Segment- und Abbruchfunktionen. Die Beispiel-Vorschau verarbeitet überlappende Audiofenster. Die Android-Anbindung muss mit NDK 28 und 16-KiB-Seiten geprüft werden.
- [sherpa-onnx, ed0e4b1](https://github.com/k2-fsa/sherpa-onnx/tree/ed0e4b171ea8e8a8feee1863726a936445bbf064) bietet laufende Erkennung und eine Android-Bibliothek. Parakeet v3 unterstützt Deutsch und Englisch, wird in der geprüften Anbindung aber im Offline-Erkenner verarbeitet.
- [llama.cpp, 4b77dee](https://github.com/ggml-org/llama.cpp/tree/4b77dee0524c64b8471869232e2958bd9589607d/examples/llama.android) enthält eine Android-Anbindung. Das Beispiel protokolliert Eingabe und Ausgabeteile. Satzfink braucht eine getrennte Anbindung ohne Textprotokolle und ohne geteilte Gesprächsdaten.
- [FUTO Voice Input, c0bbb05](https://github.com/futo-org/voice-input/tree/c0bbb05b47f02e1df089bed5bf63736ae1735d1a) ist wegen der nichtkommerziellen Lizenz eine Referenz für Audioaufnahme und Sitzungswechsel.

Der spätere Modellmanager braucht feste Downloadadressen und SHA-256-Prüfsummen, eine Größenprüfung vor dem Download, Abbruch und Fortsetzen sowie eine Prüfung vor dem atomaren Freigeben. Manuelle Importe werden als Datendateien behandelt. Vor der Aufnahme in den Katalog werden Modell, Sprache und Gerätearchitektur praktisch geprüft.
