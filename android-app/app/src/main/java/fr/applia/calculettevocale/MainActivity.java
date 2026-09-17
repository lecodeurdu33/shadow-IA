package fr.applia.calculettevocale;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private TextToSpeech textToSpeech;
    private ActivityResultLauncher<Intent> speechLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.FRANCE);
            }
        });

        speechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onSpeechResult
        );

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidVoiceBridge(), "AndroidVoice");
        setContentView(webView);

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void onSpeechResult(androidx.activity.result.ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            ArrayList<String> matches = result.getData()
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                callJs("window.__androidVoiceResult", matches.get(0));
                return;
            }
        }
        callJs("window.__androidVoiceError", "Je n'ai rien compris, réessayez.");
    }

    private void callJs(String fn, String arg) {
        String json = JSONObject.quote(arg);
        webView.post(() -> webView.evaluateJavascript(
                fn + " && " + fn + "(" + json + ");", null));
    }

    private class AndroidVoiceBridge {

        @JavascriptInterface
        public boolean isAvailable() {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            PackageManager pm = getPackageManager();
            return intent.resolveActivity(pm) != null;
        }

        @JavascriptInterface
        public void startListening() {
            runOnUiThread(() -> {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites votre calcul");
                try {
                    speechLauncher.launch(intent);
                } catch (ActivityNotFoundException e) {
                    callJs("window.__androidVoiceError", "Reconnaissance vocale indisponible sur cet appareil.");
                }
            });
        }

        @JavascriptInterface
        public void speak(String text) {
            runOnUiThread(() -> {
                if (textToSpeech != null) {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "calculette-vocale");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
