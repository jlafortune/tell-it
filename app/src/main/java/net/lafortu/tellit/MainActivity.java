package net.lafortu.tellit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit.RestAdapter;

public class MainActivity extends AppCompatActivity {
    // UI elements
    private TextView mTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;

    private List<Article> articles = new ArrayList<>();
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize swipe-to-refresh ListView of articles
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mListView = (ListView) findViewById(R.id.listview);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshArticles(null);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        mTextView = (TextView) findViewById(R.id.txtWelcome);
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

    /**
     * Handles clicks in the options menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Inspect selected item and handle appropriately
        if (id == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            refreshArticles(null);
            mSwipeRefreshLayout.setRefreshing(false);
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
            mTextView.setText("No network connection.");
        }
    }

    /**
     * Calls the text to speech engine for the specified articleId.
     * @param article the article to speak
     */
    protected void playArticle(final Article article) {
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
                                mTextView.post(new Runnable() {     // Update UI thread
                                    @Override
                                    public void run() {
                                        mTextView.setText(currentSentence);
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

    /**
     * Refreshes the UI with the most recently downloaded articles
     */
    protected void addArticlesToDisplay() {
        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        ArrayAdapter<Article> adapter = new ArrayAdapter<Article>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, articles);

        // Assign adapter to ListView
        mListView.setAdapter(adapter);

        // ListView Item Click Listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item value
                Article clickedArticle    = (Article) mListView.getItemAtPosition(position);
                playArticle(clickedArticle);
            }
        });
    }


    // Uses AsyncTask to create a task away from the main UI thread.
    // This task is used to download article contents from the Tell It web service
    private class DownloadArticlesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            articles = downloadArticles();
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
