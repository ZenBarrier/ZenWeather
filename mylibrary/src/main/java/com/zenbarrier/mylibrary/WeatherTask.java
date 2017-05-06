package com.zenbarrier.mylibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class WeatherTask extends AsyncTask<Double, Void, String> {
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private boolean hasUpdated = false;

    private static final String KEY_PREF_WEATHER_TIME_STAMP = "KEY_PREF_WEATHER_TIME_STAMP";
    private static final String KEY_PREF_WEATHER_JSON = "KEY_PREF_WEATHER_JSON";
    private static final long ONE_HOUR_MS = 3600 * 1000;

    public interface WeatherTaskInterface {
        void onWeatherRetrieved(String result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    protected String doInBackground(Double... params) {

        long timeStamp = sharedPreferences.getLong(KEY_PREF_WEATHER_TIME_STAMP, System.currentTimeMillis());
        long currentTimeStamp = System.currentTimeMillis();
        if((currentTimeStamp - timeStamp) >= ONE_HOUR_MS){
            return sharedPreferences.getString(KEY_PREF_WEATHER_JSON, "");
        }

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

            hasUpdated = true;
            return stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sharedPreferences.getString(KEY_PREF_WEATHER_JSON, "");
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(hasUpdated && result.length() > 0) {
            sharedPreferences.edit().putLong(KEY_PREF_WEATHER_TIME_STAMP, System.currentTimeMillis())
                    .putString(KEY_PREF_WEATHER_JSON, result).apply();
        }
        if(mContext instanceof WeatherTaskInterface){
            ((WeatherTaskInterface) mContext).onWeatherRetrieved(result);
        }
    }

    public WeatherTask(Context context){
        mContext = context;
    }

}
