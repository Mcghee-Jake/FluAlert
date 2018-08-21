package com.example.jmcghee.flualert;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.jmcghee.flualert.utils.NetworkUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private static final String NUM_DAYS = Integer.toString(7);
    private static final String SEARCH_QUERY_URL = "url";
    private static final String SEARCH_RESULTS_RAW_JSON = "results"; // Used to save and restore the results of the API call
    private static final int MY_LOADER = 77;

    private TextView tvTest;
    private ProgressBar pbLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTest = findViewById(R.id.tv_test);
        pbLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        if (savedInstanceState != null) {
            String rawJsonSearchResults = savedInstanceState.getString(SEARCH_RESULTS_RAW_JSON);
            tvTest.setText(rawJsonSearchResults);
        } else {
            makeQuery();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String rawJsonSearchResults = tvTest.getText().toString();
        outState.putString(SEARCH_RESULTS_RAW_JSON, rawJsonSearchResults);
    }

    private void makeQuery() {
        // Build the URL
        URL url = NetworkUtils.buildUrl(NUM_DAYS);

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL, url.toString());

       LoaderManager loaderManager = getLoaderManager();
       Loader<String> queryLoader = loaderManager.getLoader(MY_LOADER);
       if (queryLoader == null) {
           loaderManager.initLoader(MY_LOADER, queryBundle, this);
       } else {
           loaderManager.restartLoader(MY_LOADER, queryBundle, this);
       }
    }
    

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                if (args == null) return;
                pbLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                String searchQueryUrlString = args.getString(SEARCH_QUERY_URL);
                String result = null;
                try {
                    URL url = new URL(searchQueryUrlString);
                    result = NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);

        if (data != null && !data.equals("")) {
            tvTest.setText(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }



}
