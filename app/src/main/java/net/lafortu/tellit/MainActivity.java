package net.lafortu.tellit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
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
                        String text = articles.get(articleId).getText();
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else
                    Log.e("error", "Initialization Failed!");
            }
        });
    }

    /**
     * Refreshes the UI with the most recently downloaded articles
     */
    protected void addArticlesToDisplay() {
        for (Integer id : articles.keySet()) {
            Article a = articles.get(id);
            Button button = new Button(this);
            button.setText(a.getTitle());
            button.setTag(a.getId());

            // Add listener to play article when button clicked
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) v.getTag();
                    playArticle(id);
                }
            });

            // Add article button to UI
            LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
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
