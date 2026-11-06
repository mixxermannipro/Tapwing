# Cloud-Anbieter

Cloud-KI ist offen. Die erste App enthält keine Internetberechtigung. Geplant sind OpenAI-kompatible Schnittstellen für OpenAI, OpenRouter, Groq, Mistral, DeepSeek und xAI sowie eigene Adapter für Gemini und Anthropic. Eigene Server wie Ollama oder LM Studio brauchen eine ausdrückliche Konfiguration.

Vor einer Freigabe sind erforderlich

- Schlüsselverschlüsselung über den Android-Schlüsselspeicher. Fehler müssen den Vorgang abbrechen.
- Bestätigung des Empfängers und der ausgewählten Daten vor der Übertragung.
- Tests für Antwortformate, Abbruch, Zeitlimit, Umleitung, ungültige Modelle und fehlende Berechtigung.
- Dokumentation je Anbieter zu Preisangaben, übertragenen Daten und optionaler Modellauswahl.
