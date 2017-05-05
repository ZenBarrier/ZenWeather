package com.zenbarrier.mylibrary;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by Anthony on 5/3/2017.
 * This file is the fragment that holds all the preferences
 */

public class WeatherTask extends AsyncTask<Double, Void, String> {
    private Context mContext;

    public interface WeatherTaskInterface {
        void onWeatherRetrieved(String result);
    }

    @Override
    protected String doInBackground(Double... params) {
        double lat = params[0];
        double lng = params[1];
        String appId = mContext.getString(R.string.weather_api);
        String urlString = String.format(Locale.getDefault(),
                "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s", lat, lng, appId);

        HttpURLConnection connection;
        BufferedReader reader;

        try{
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while((line = reader.readLine())!=null){
                stringBuilder.append(line);
            }

            return stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(mContext instanceof WeatherTaskInterface){
            ((WeatherTaskInterface) mContext).onWeatherRetrieved(result);
        }
    }

    public WeatherTask(Context context){
        mContext = context;
    }

}
