package com.zenbarrier.zenweather;

import android.location.Location;
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

public class TemperatureComplicationService extends ComplicationProviderService {
    private static final String TAG = TemperatureComplicationService.class.getSimpleName();
    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        //GetLocationTask locationTask = new GetLocationTask(this);
        //locationTask.execute();
        String result="";
        Double temperature = null;
        try {
            Log.d(TAG, "hello");
            //Location location = locationTask.get(800, TimeUnit.MILLISECONDS);
            WeatherTask weatherTask = new WeatherTask(this);
            weatherTask.execute(40.0, 40.0);
            result = weatherTask.get(1000, TimeUnit.MILLISECONDS);
            Log.d(TAG, result);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        try {
            JSONObject weatherJson = new JSONObject(result);
            JSONObject mainJson = weatherJson.getJSONObject("main");
            temperature = WeatherUtil.kelvin2Fahrenheit(mainJson.getDouble("temp"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(temperature != null){
            ComplicationData.Builder complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                    .setShortText(ComplicationText.plainText(getString(R.string.degree_fahrenheit, Math.round(temperature))));
            complicationManager.updateComplicationData(complicationId, complicationData.build());
        }
        Log.d(TAG, temperature+"");
    }

    @Override
    public void onComplicationActivated(int complicationId, int type, ComplicationManager manager) {
        Log.d(TAG, "activated");
        super.onComplicationActivated(complicationId, type, manager);
        ComplicationData.Builder complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                .setShortText(ComplicationText.plainText("Hello"));
        manager.updateComplicationData(complicationId, complicationData.build());
    }

    @Override
    public void onComplicationDeactivated(int complicationId) {
        Log.d(TAG, "deactivated");
        super.onComplicationDeactivated(complicationId);
    }
}
