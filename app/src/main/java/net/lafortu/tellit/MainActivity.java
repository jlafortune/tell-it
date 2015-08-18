package net.lafortu.tellit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit.RestAdapter;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Map<Integer, Article> articles = new TreeMap<>();
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.txtWelcome);
        refreshArticles(null);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
     * Downloads articles from the server and refreshes the view.
     */
    public void refreshArticles(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            new DownloadArticlesTask().execute();
        } else {
            textView.setText("No network connection.");
        }
    }

    /**
     * Calls the text to speech engine for the specified articleId.
     * @param articleId the ID of the article to speak
     */
    protected void playArticle(final int articleId) {
        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

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
                                textView.post(new Runnable() {     // Update UI thread
                                    @Override
                                    public void run() {
                                        textView.setText(currentSentence);
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


                        String text = articles.get(articleId).getText();

                        // Read story sentence by sentence.
                        // Update UI to show sentence being read.
                        String[] arr = text.split("\\. ");

                        for (int i = 0; i < arr.length; i++) {

                            if (!arr[i].endsWith(".")) {
                                arr[i] = arr[i] + ".";
                            }

                            HashMap<String, String> map = new HashMap<String, String>(1);
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, arr[i]);

                            tts.speak(arr[i], TextToSpeech.QUEUE_ADD, map);
                        }
                    }
                } else
                    Log.e("error", "Initialization Failed!");
            }
        });
    }

    /**
     * Refreshes the UI with the most recently downloaded articles
     */
    protected void addArticlesToDisplay() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        ll.removeAllViews();

        for (Integer id : articles.keySet()) {
            Article a = articles.get(id);
            Button button = new Button(this);
            button.setText(a.getTitle());
            button.setTag(a.getId());

            // Add listener to play article when button clicked
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = (int) view.getTag();
                    playArticle(id);
                }
            });

            // Add article button to UI
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.addView(button, lp);
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread.
    // This task is used to download article contents from the Tell It web service
    private class DownloadArticlesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            List<Article> articlesList = downloadArticles();
            for (Article a : articlesList) {
                articles.put(a.getId(), a);
            }
            return "Done";
        }

        /**
         * Display the results of this download task
         */
        @Override
        protected void onPostExecute(String result) {
            addArticlesToDisplay();
        }

        /**
         * Connect to the web service to download articles.
         */
        private List<Article> downloadArticles() {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://10.0.2.2:8080/TellItService/") // TODO refactor endpoint to property
                    .build();

            TellItWebService service = restAdapter.create(TellItWebService.class);
            List<Article> articles = service.getArticles();
            return articles;
        }
    }

}
