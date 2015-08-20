package net.lafortu.tellit;

import android.content.Context;
import android.content.Intent;
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

/**
 * The main activity of Tell It, responsible for downloading and displaying a list of
 * articles which can each be selected and individually played by the text-to-speech engine.
 *
 * Selecting an article launches PlayArticleActivity, which has a UI for manipulating
 * the playback of the article.
 */
public class MainActivity extends AppCompatActivity {
    // UI elements
    private TextView mTextWelcome;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;

    private List<Article> articles = new ArrayList<>();

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
        mTextWelcome = (TextView) findViewById(R.id.txtWelcome);
        refreshArticles(null);
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
            mTextWelcome.setText("No network connection.");
        }
    }

    /**
     * Updates the UI with the most recently downloaded articles
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
                // Launch the view/play activity for the clicked article
                Article clickedArticle = (Article) mListView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, PlayArticleActivity.class);
                intent.putExtra("article", clickedArticle);
                startActivity(intent);
            }
        });
    }


    // This AsyncTask is used to download article contents from the Tell It web service
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
