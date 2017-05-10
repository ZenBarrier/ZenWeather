package com.zenbarrier.mylibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
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
    private double mLat, mLng;

    public static final String KEY_PREF_WEATHER_TIME_STAMP = "KEY_PREF_WEATHER_TIME_STAMP";
    private static final String KEY_PREF_WEATHER_JSON = "KEY_PREF_WEATHER_JSON";
    private  static final String KEY_PREF_OLD_LOCATION_LAT = "KEY_PREF_OLD_LOCATION_LAT";
    private  static final String KEY_PREF_OLD_LOCATION_LNG = "KEY_PREF_OLD_LOCATION_LNG";
    private static final long ONE_HOUR_MS = 3600 * 1000;
    private static final float DISTANCE_UPDATE_METERS = 160000;

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
        mLat = params[0];
        mLng = params[1];

        long timeStamp = sharedPreferences.getLong(KEY_PREF_WEATHER_TIME_STAMP, 0);
        long currentTimeStamp = System.currentTimeMillis();
        if((currentTimeStamp - timeStamp) <= ONE_HOUR_MS/2){
            float oldLat = sharedPreferences.getFloat(KEY_PREF_OLD_LOCATION_LAT, 0);
            float oldLng = sharedPreferences.getFloat(KEY_PREF_OLD_LOCATION_LNG, 0);
            float[] distance = new float[1];
            Location.distanceBetween(oldLat, oldLng, mLat, mLng, distance);

            if(distance[0] < DISTANCE_UPDATE_METERS) {
                return sharedPreferences.getString(KEY_PREF_WEATHER_JSON, "");
            }
        }

        String appId = mContext.getString(R.string.weather_api);
        String urlString = String.format(Locale.getDefault(),
                "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s", mLat, mLng, appId);

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
            stream.close();
            reader.close();
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
                    .putString(KEY_PREF_WEATHER_JSON, result)
                    .putFloat(KEY_PREF_OLD_LOCATION_LAT, (float) mLat)
                    .putFloat(KEY_PREF_OLD_LOCATION_LNG, (float) mLng)
                    .apply();
        }
        if(mContext instanceof WeatherTaskInterface){
            ((WeatherTaskInterface) mContext).onWeatherRetrieved(result);
        }
    }

    public WeatherTask(Context context){
        mContext = context;
    }

}
