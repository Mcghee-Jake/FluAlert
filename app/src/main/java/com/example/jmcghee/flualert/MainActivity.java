package com.example.jmcghee.flualert;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.jmcghee.flualert.utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String NUM_DAYS = Integer.toString(7);
    private TextView tvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTest = findViewById(R.id.tv_test);

        makeQuery();
    }

    private void makeQuery() {
        // Build the URL
        URL url = NetworkUtils.buildUrl(NUM_DAYS);
        // Execute the AsyncTask
        new NetworkingTask().execute(url);
    }

    private class NetworkingTask extends AsyncTask<URL, Void, String> {

        /**
         * Make the API call
         *
         * @param urls The URL to fetch the HTTP response from
         * @return The result of the API call
         */
        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String results = null;
            try {
                results = NetworkUtils.getResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && !s.equals("")) {
               tvTest.setText(s);
            }
        }
    }

}
