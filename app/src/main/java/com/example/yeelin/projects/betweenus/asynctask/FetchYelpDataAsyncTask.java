package com.example.yeelin.projects.betweenus.asynctask;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ninjakiki on 2/16/16.
 */
public class FetchYelpDataAsyncTask extends AsyncTask<Pair<Context, String>, Void, String> {
    private static final String TAG = FetchYelpDataAsyncTask.class.getCanonicalName();
    private Context context;

    @Override
    protected String doInBackground(Pair<Context, String>... params) {
        context = params[0].first;
        String name = params[0].second;

        //https://betweenus-3636.appspot.com/fetch/yelp?name=Pusheen
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("https")
                .authority("betweenus-3636.appspot.com")
                .appendPath("fetch")
                .appendPath("yelp")
                .appendQueryParameter("name", name);
        Uri uri = uriBuilder.build();
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(uri.toString());
            Log.d(TAG, "Contacting url: " + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    Log.d(TAG, "Reading one line");
                    buffer.append(line);
                }
                in.close();
                return buffer.toString();
            }
            else {
                Log.w(TAG, "Not OK: " + urlConnection.getErrorStream());
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return "Something went wrong";
    }

    @Override
    protected void onPostExecute(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
