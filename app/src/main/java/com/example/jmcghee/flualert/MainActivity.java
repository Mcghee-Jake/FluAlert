package com.example.jmcghee.flualert;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.jmcghee.flualert.utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<String> {

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
        // Get the loader
        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        android.support.v4.content.Loader<String> queryLoader = loaderManager.getLoader(MY_LOADER);

        // Build the URL
        URL url = NetworkUtils.buildUrl(NUM_DAYS);
        // Put the URL into a bundle
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL, url.toString());

        // Get the callbacks
        android.support.v4.app.LoaderManager.LoaderCallbacks<String> callbacks = MainActivity.this;

        // Init or restart the loader
        if (queryLoader == null) {
           loaderManager.initLoader(MY_LOADER, queryBundle, callbacks);
        } else {
           loaderManager.restartLoader(MY_LOADER, queryBundle, callbacks);
        }
    }
    

    @Override
    public android.support.v4.content.Loader<String> onCreateLoader(int id, final Bundle args) {
        return new android.support.v4.content.AsyncTaskLoader<String>(this) {

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
                    Log.d("URL", url.toString());
                    result = NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<String> loader, String data) {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);

        if (data != null && !data.equals("")) {
            tvTest.setText(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<String> loader) {

    }



}
