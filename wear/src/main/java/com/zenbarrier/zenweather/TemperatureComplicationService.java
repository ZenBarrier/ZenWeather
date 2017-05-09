package com.zenbarrier.zenweather;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;

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
            Double tempMinKelvin = null;
            Double tempMaxKelvin = null;
            String name = "";
            String iconString = "";
            try {
                WeatherTask weatherTask = new WeatherTask(this);
                weatherTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, location.getLatitude(), location.getLongitude());
                result = weatherTask.get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            try {
                JSONObject weatherJson = new JSONObject(result);
                JSONObject mainJson = weatherJson.getJSONObject("main");
                tempKelvin = mainJson.getDouble("temp");
                tempMinKelvin = mainJson.getDouble("temp_min");
                tempMaxKelvin = mainJson.getDouble("temp_max");
                name = weatherJson.getString("name");
                iconString = weatherJson.getJSONArray("weather").getJSONObject(0).getString("icon");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isCelsius = sharedPreferences.getBoolean(getString(R.string.KEY_PREF_IS_CELSIUS), false);

            String temperatureText = "";
            String tempMinText = "";
            String tempMaxText = "";
            if (tempKelvin != null && tempMinKelvin != null && tempMaxKelvin != null) {
                long temp = Math.round(isCelsius ? WeatherUtil.kelvin2Celsius(tempKelvin) :
                        WeatherUtil.kelvin2Fahrenheit(tempKelvin));
                long tempMin = Math.round(isCelsius ? WeatherUtil.kelvin2Celsius(tempMinKelvin) :
                        WeatherUtil.kelvin2Fahrenheit(tempMinKelvin));
                long tempMax = Math.round(isCelsius ? WeatherUtil.kelvin2Celsius(tempMaxKelvin) :
                        WeatherUtil.kelvin2Fahrenheit(tempMaxKelvin));
                temperatureText = isCelsius ? getString(R.string.degree_celsius, temp) :
                        getString(R.string.degree_fahrenheit, temp);
                tempMinText = isCelsius ? getString(R.string.degree_celsius, tempMin) :
                        getString(R.string.degree_fahrenheit, tempMin);
                tempMaxText = isCelsius ? getString(R.string.degree_celsius, tempMax) :
                        getString(R.string.degree_fahrenheit, tempMax);
            }

            ComplicationData.Builder complicationData = null;
            switch (dataType){
                case ComplicationData.TYPE_SHORT_TEXT:
                    complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT);
                    complicationData.setShortText(
                            ComplicationText.plainText(temperatureText));
                    break;
                case ComplicationData.TYPE_LONG_TEXT:
                    complicationData = new ComplicationData.Builder(ComplicationData.TYPE_LONG_TEXT);
                    complicationData.setLongTitle(ComplicationText.plainText(
                            getString(R.string.complication_long_title, temperatureText, name)));
                    complicationData.setLongText(
                            ComplicationText.plainText(
                                    getString(R.string.complication_long_text, tempMinText, tempMaxText)));
                    break;
            }

            if(complicationData != null) {
                complicationData.setIcon(Icon.createWithResource(this, iconCode(iconString)));
                Intent intent = new Intent(this, MainActivity.class);
                PendingIntent openAppIntent = PendingIntent.getActivity(this, 0, intent, 0);

                complicationData.setTapAction(openAppIntent);



                ((ComplicationManager) complicationManager).updateComplicationData(complicationId, complicationData.build());
            }

        }
    }

    private int iconCode(String code){
        int codeNum = Integer.parseInt(code.substring(0,2));
        switch (codeNum){
            case 1: return R.drawable.ic_sun;
            case 2: return R.drawable.ic_part_cloud;
            case 3: return R.drawable.ic_cloud;
            case 4: return R.drawable.ic_broken_clouds;
            case 9: return R.drawable.ic_rain;
            case 10: return R.drawable.ic_light_rain;
            case 11: return R.drawable.ic_thunder;
            case 13: return R.drawable.ic_snow;
            case 50: return R.drawable.ic_mist;
            default: return 0;
        }
    }
}
