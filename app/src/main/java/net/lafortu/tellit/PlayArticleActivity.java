package net.lafortu.tellit;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

public class PlayArticleActivity extends AppCompatActivity {
    private TextView mTextSentence;     // UI displays the current sentence being read out loud
    private TextView mTextTitle;
    private Button mPlayButton;
    private TextToSpeech tts;
    private Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_article);
        mTextSentence = (TextView) findViewById(R.id.txtSentence);
        mTextTitle = (TextView) findViewById(R.id.txtTitle);
        mPlayButton = (Button) findViewById(R.id.btnPlay);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playArticle(article);
            }
        });

        article = (Article) getIntent().getSerializableExtra("article");
        mTextTitle.setText(article.getTitle());
    }

    @Override
    protected void onPause() {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Calls the text to speech engine for the specified article.
     * @param article the article to speak
     */
    protected void playArticle(final Article article) {
        tts = new TextToSpeech(PlayArticleActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);

                    if(result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    } else {
                        // Track progress of TTS engine reading article
                        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(final String currentSentence) {
                                mTextSentence.post(new Runnable() {     // Update UI thread
                                    @Override
                                    public void run() {
                                        mTextSentence.setText(currentSentence);
                                    }
                                });
                            }

                            @Override
                            public void onDone(String str) {
                                // Not implemented
                            }

                            @Override
                            public void onError(String str) {
                                // Not implemented
                            }
                        });


                        String text = article.getText();

                        // Read story sentence by sentence.
                        // Update UI to show sentence being read.
                        String[] arr = text.split("\\. ");

                        for (int i = 0; i < arr.length; i++) {

                            if (!arr[i].endsWith(".")) {
                                arr[i] = arr[i] + ".";
                            }

                            HashMap<String, String> map = new HashMap<>(1);
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, arr[i]);

                            tts.speak(arr[i], TextToSpeech.QUEUE_ADD, map);
                        }
                    }
                } else
                    Log.e("error", "Initialization Failed!");
            }
        });
    }
}
