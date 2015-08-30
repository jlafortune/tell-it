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

/**
 * The activity responsible for audio playback of an article.
 */
public class PlayArticleActivity extends AppCompatActivity {
    private TextView mTextSentence;     // UI displays the current sentence being read out loud
    private TextView mArticleTitle;
    private Button mPlayButton;
    private TextToSpeech mTextToSpeech;
    private Article mArticle;
    private String[] mSentences;        // All of the sentences within the article
    private int mPlaybackPos;           // Keeps track of the current sentence being read
    private HashMap<String, String> mTtsParams = new HashMap<>(1);

    /**
     * Sets up playback. Registers UI elements, sets the current article,
     * and splits up the article text by sentence.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_article);
        mTextSentence = (TextView) findViewById(R.id.txtSentence);
        mArticleTitle = (TextView) findViewById(R.id.txtTitle);
        mPlayButton = (Button) findViewById(R.id.btnPlay);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayback(mArticle);
            }
        });

        mArticle = (Article) getIntent().getSerializableExtra("article");
        mArticleTitle.setText(mArticle.getTitle());

        String text = mArticle.getText();
        mSentences = text.split("\\.");
        mPlaybackPos = 0;
    }

    @Override
    protected void onPause() {
        if(mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
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
     * Plays or pauses text-to-speech as appropriate.
     */
    protected void togglePlayback(final Article article) {
        if ("Play".equals(mPlayButton.getText())) {
            mPlayButton.setText("Pause");
            playArticle();
        } else {    // Pause
            mPlayButton.setText("Play");
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }

    /**
     * Calls the text-to-speech engine for the current article.
     */
    protected void playArticle() {
        mTextToSpeech = new TextToSpeech(PlayArticleActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTextToSpeech.setLanguage(Locale.US);

                    if(result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    } else {
                        // Track progress of TTS engine reading mArticle
                        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            /**
                             * Updates the UI to show the sentence about to be read.
                             */
                            @Override
                            public void onStart(final String currentSentence) {
                                mTextSentence.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTextSentence.setText(currentSentence);
                                    }
                                });
                            }

                            /**
                             * Moves onto the next sentence.
                             */
                            @Override
                            public void onDone(String str) {
                                mPlaybackPos++;
                                playback();
                            }

                            @Override
                            public void onError(String str) {
                                // Not implemented
                            }
                        });
                    }
                } else {
                    Log.e("error", "Initialization Failed!");
                }
                playback();     // Start reading aloud
            }
        });
    }

    /**
     * Speaks the current sentence in mSentences.
     */
    private void playback() {
        if (mPlaybackPos < mSentences.length) {
            if (!mSentences[mPlaybackPos].endsWith(".")) {
                mSentences[mPlaybackPos] = mSentences[mPlaybackPos] + ".";
            }

            mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mSentences[mPlaybackPos]);
            mTextToSpeech.speak(mSentences[mPlaybackPos], TextToSpeech.QUEUE_ADD, mTtsParams);
        }
    }
}
