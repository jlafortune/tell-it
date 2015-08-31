package net.lafortu.tellit;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


/**
 * The main activity of Tell It,
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new NewsTabFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles clicks in the options menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO fix refresh from drop down menu or get rid of it
//        int id = item.getItemId();

//        NewsTabFragment instanceFragment=
//                (NewsTabFragment)getSupportFragmentManager().findFragmentById(R.id.);
//
//        // Inspect selected item and handle appropriately
//        if (id == R.id.action_refresh) {
//            mSwipeRefreshLayout.setRefreshing(true);
//            refreshArticles(null);
//            mSwipeRefreshLayout.setRefreshing(false);
//        }

        return super.onOptionsItemSelected(item);
    }
}
