# Builds und Veröffentlichung

```sh
./gradlew :app:assembleDebug :app:assembleRelease
sha256sum app/build/outputs/apk/*/*.apk
```

Die Debug-APK ist mit dem lokalen Entwicklungsschlüssel signiert. Die Release-APK bleibt bis zur Einrichtung eines dauerhaften Projektschlüssels unsigniert. Signierschlüssel gehören außerhalb des Repositorys. Die SDK- und NDK-Versionen sind festgelegt. Ein bytegleicher Vergleich zweier unabhängig erstellter APKs steht aus.

Vor einer stabilen Veröffentlichung müssen Wischen, Diktat, lokale und Cloud-Schreibwerkzeuge sowie die Sicherheits- und Gerätetests fertig sein. Für GitHub Releases sollen APK, SHA-256, Quellarchiv und Änderungsübersicht bereitstehen. Obtainium- und F-Droid-Verteilung sind noch nicht geprüft.
