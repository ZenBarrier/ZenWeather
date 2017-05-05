package com.zenbarrier.zenweather;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

import com.zenbarrier.mylibrary.GetLocationTask;
import com.zenbarrier.mylibrary.WeatherTask;
import com.zenbarrier.mylibrary.WeatherUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TemperatureComplicationService extends ComplicationProviderService
        implements GetLocationTask.ComplicationInterface {
    private static final String TAG = TemperatureComplicationService.class.getSimpleName();
    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        GetLocationTask locationTask = new GetLocationTask(this, complicationId, dataType, complicationManager);
        locationTask.execute();
    }

    @Override
    public void onComplicationActivated(int complicationId, int type, ComplicationManager manager) {
    }

    @Override
    public void onLocationFound(int complicationId, int dataType, Object complicationManager, Location location) {
        if(complicationManager instanceof ComplicationManager){

            String result="";
            Double tempKelvin = null;
            try {
                WeatherTask weatherTask = new WeatherTask(this);
                weatherTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, location.getLatitude(), location.getLongitude());
                result = weatherTask.get(1000, TimeUnit.MILLISECONDS);
                Log.d(TAG, result);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            try {
                JSONObject weatherJson = new JSONObject(result);
                JSONObject mainJson = weatherJson.getJSONObject("main");
                tempKelvin = mainJson.getDouble("temp");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ComplicationData.Builder complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT);

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent openAppIntent = PendingIntent.getActivity(this, 0, intent, 0);

            complicationData.setTapAction(openAppIntent);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isCelsius = sharedPreferences.getBoolean(getString(R.string.KEY_PREF_IS_CELSIUS), false);

            if(tempKelvin != null){
                long temp = Math.round(isCelsius ? WeatherUtil.kelvin2Celsius(tempKelvin):
                        WeatherUtil.kelvin2Fahrenheit(tempKelvin));
                String temperatureText = isCelsius ? getString(R.string.degree_celsius, temp) :
                        getString(R.string.degree_fahrenheit, temp);
                complicationData.setShortText(
                        ComplicationText.plainText(temperatureText));
            }
            ((ComplicationManager)complicationManager).updateComplicationData(complicationId, complicationData.build());

        }
    }
}
