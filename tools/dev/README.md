# Entwicklungsumgebung

Benötigt werden Linux x86_64, ein vollständiges JDK 17 (`java` und `javac`),
Python 3, Git, unzip und die Android Command-line Tools. Installiere das JDK
beispielsweise aus Eclipse Temurin. Die Toolchains gehören außerhalb des Repos.
Ein erster Build benötigt mehrere GB freien Speicher.

## Android SDK

Setze `JAVA_HOME` auf das JDK und `ANDROID_HOME` auf dein SDK-Verzeichnis.
Ergänze `$JAVA_HOME/bin`, `$ANDROID_HOME/cmdline-tools/latest/bin` und
`$ANDROID_HOME/platform-tools` im `PATH`. Nutze die offiziellen
[Android Command-line Tools](https://developer.android.com/studio#command-tools).
Installiere die folgenden festgelegten Pakete:

```sh
sdkmanager --sdk_root="$ANDROID_HOME" --channel=3 \
  'platforms;android-37.2' 'build-tools;36.0.0' \
  'ndk;28.2.13676358' 'platform-tools'
```

Bei älteren Command-line Tools zuerst `sdkmanager --licenses` ausführen.
Bei neuen Versionen ist `android sdk` der Nachfolger; deren Hinweis zu
CLI-Metriken beachten und die Metriken deaktivieren.

Optional darf die ignorierte `local.properties` den Eintrag
`sdk.dir=/absoluter/pfad/zum/sdk` enthalten. Kein `local.properties` einchecken.
Gradle 9.7.1 wird mit SHA-256-Prüfung vom Wrapper geladen. Keine globale
Gradle-Installation und kein Versionswechsel für die Einrichtung erforderlich.

## Prüfen

Für schnelles Feedback vor einem APK-Build zunächst die lokale Suite starten:

```sh
./gradlew :app:testDebugUnitTest --console=plain
```

Der vollständige erste Prüflauf baut zusätzlich alle vier Architekturen:

```sh
java -version
javac -version
./gradlew --version
python3 tools/generate-swipe-lexicons.py --check
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --console=plain
./gradlew :app:lintDebug --console=plain
git diff --check
```

Debug-APK: `app/build/outputs/apk/debug/Tapwing_0.1.0-dev-debug.apk`.
Release-APK: `app/build/outputs/apk/release/`, unsigniert. APKs und Schlüssel
gehören nicht in Git. Geräte-/Emulatortests: [TESTING.md](../../docs/TESTING.md).

## Verwaltete Cloud-Umgebung

Die aktuelle Sitzung stellt lokal `/workspace/tapwing-env.sh` bereit:

```sh
source /workspace/tapwing-env.sh
cd /workspace/Tapwing
```

Diese Datei und die SDK-/JDK-/Gradle-Verzeichnisse sind lokale Einrichtung,
keine Voraussetzung für andere Rechner. Auf kleinen Cloud-Rechnern sind zwei
Gradle-Worker sinnvoll (`org.gradle.workers.max=2` in der lokalen
`$GRADLE_USER_HOME/gradle.properties`).

Gradle/JVM übernehmen HTTP_PROXY/HTTPS_PROXY nicht automatisch. Wenn ein
verwalteter Proxy erforderlich ist, setze dessen Host/Port als lokale
`systemProp.http.proxyHost`, `systemProp.http.proxyPort` und entsprechende
`https`-Eigenschaften in der Gradle-Benutzerkonfiguration. Bestehende
Proxyregeln und CA-Vertrauen erhalten. Bei eigenem JDK gegebenenfalls den
System-Truststore über `systemProp.javax.net.ssl.trustStore` auswählen.
TLS-Prüfung eingeschaltet lassen; keine Zugangsdaten in Projektdateien schreiben.
