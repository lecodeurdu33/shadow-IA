# Calculette Vocale — projet Android

Application Android native qui embarque la calculette vocale (`assets/index.html`)
dans une WebView, avec un pont vers les API natives d'Android pour que la
reconnaissance vocale et la synthèse vocale fonctionnent vraiment (le WebView
standard ne supporte pas l'API web `SpeechRecognition`) :

- **Micro** : `MainActivity` lance la reconnaissance vocale système d'Android
  (`RecognizerIntent.ACTION_RECOGNIZE_SPEECH`, en français) et renvoie le texte
  reconnu à la page web.
- **Voix** : les résultats sont lus à voix haute via `android.speech.tts.TextToSpeech`.
- Si aucun service de reconnaissance vocale n'est disponible sur l'appareil, la
  calculette reste utilisable au clavier et au champ texte.

## Compiler l'APK

Prérequis : [Android Studio](https://developer.android.com/studio) (ou juste le
SDK Android en ligne de commande) et une connexion internet (le premier build
télécharge la distribution Gradle et l'Android Gradle Plugin depuis les dépôts
officiels).

**Avec Android Studio** : ouvrir ce dossier (`android-app/`) comme projet,
laisser la synchronisation Gradle se terminer, puis
*Build → Build Bundle(s) / APK(s) → Build APK(s)*.

**En ligne de commande** :

```bash
cd android-app
./gradlew assembleDebug
```

L'APK signé en debug apparaît dans :
`app/build/outputs/apk/debug/app-debug.apk`

Pour un APK release (à signer avec votre propre clé) :

```bash
./gradlew assembleRelease
```

## Structure

```
android-app/
  app/
    src/main/
      AndroidManifest.xml
      java/fr/applia/calculettevocale/MainActivity.java   ← pont voix/TTS
      assets/index.html                                    ← la calculette
      res/                                                 ← icônes, thème
  build.gradle, settings.gradle, gradle/                   ← config Gradle
```
