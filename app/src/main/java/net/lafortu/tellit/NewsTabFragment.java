package net.lafortu.tellit;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;

/**
 * Responsible for downloading and displaying a list of articles which can each be selected
 * and individually played by the text-to-speech engine.
 *
 * Selecting an article launches PlayArticleActivity, which has a UI for manipulating
 * the playback of the article.
 */
public class NewsTabFragment extends Fragment {
    public static final String ARG_TAB_INDEX = "TAB_INDEX";

    private TextView mTextWelcome;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;

    private List<Article> mArticles = new ArrayList<>();    // News articles
    protected String mCategory;     // News category of tab. E.g. business, tech, sports, etc.

    public static NewsTabFragment newInstance(int tabIndex) {
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_INDEX, tabIndex);
        NewsTabFragment fragment = new NewsTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int tabIndex = getArguments().getInt(ARG_TAB_INDEX);

        switch (tabIndex) {
            case 1:
                mCategory = "Top Stories";
                break;
            case 2:
                mCategory = "Business";
                break;
            case 3:
                mCategory = "World";
                break;
            case 4:
                mCategory = "Politics";
                break;
            case 5:
                mCategory = "Technology";
                break;
            case 6:
                mCategory = "Entertainment";
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_tab_fragment, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mListView = (ListView) view.findViewById(R.id.listview);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshArticles(null);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        mTextWelcome = (TextView) view.findViewById(R.id.txtWelcome);
        refreshArticles(null);

        return view;
    }

    /**
     * Downloads articles from the server and refreshes the view.
     */
    public void refreshArticles(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
        ArrayAdapter<Article> adapter = new ArrayAdapter<Article>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, mArticles);

        // Assign adapter to ListView
        mListView.setAdapter(adapter);

        // ListView Item Click Listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Launch the view/play activity for the clicked article
                Article clickedArticle = (Article) mListView.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), PlayArticleActivity.class);
                intent.putExtra("article", clickedArticle);
                startActivity(intent);
            }
        });
    }

    // This AsyncTask is used to download article contents from the Tell It web service
    private class DownloadArticlesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            mArticles = downloadArticles();
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
                    .setEndpoint("http://lafortu.net:8080/") // TODO refactor endpoint to property
                    .build();
            TellItWebService service = restAdapter.create(TellItWebService.class);
            List<Article> articles = service.getArticles(mCategory);
            return articles;
        }
    }
}
