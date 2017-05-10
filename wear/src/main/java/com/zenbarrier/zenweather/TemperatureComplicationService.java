package com.zenbarrier.zenweather;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

import com.zenbarrier.mylibrary.GetLocationTask;
import com.zenbarrier.mylibrary.PermissionActivity;
import com.zenbarrier.mylibrary.Weather;
import com.zenbarrier.mylibrary.WeatherTask;
import com.zenbarrier.mylibrary.WeatherUtil;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TemperatureComplicationService extends ComplicationProviderService
        implements GetLocationTask.ComplicationInterface {
    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            GetLocationTask locationTask = new GetLocationTask(this, complicationId, dataType, complicationManager);
            locationTask.execute();
        }else{
            ComplicationData.Builder complicationData;
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.putExtra(getString(R.string.extra_permission_request_code), PermissionActivity.REQUEST_CODE_COMPLICATION);
            PendingIntent permissionIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            switch (dataType){
                case ComplicationData.TYPE_SHORT_TEXT:
                    complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT);
                    complicationData.setTapAction(permissionIntent);
                    complicationData.setShortText(
                            ComplicationText.plainText(getString(R.string.complication_no_gps)));
                    complicationData.setShortTitle(
                            ComplicationText.plainText(getString(R.string.complication_permission_short)));
                    complicationManager.updateComplicationData(complicationId, complicationData.build());
                    break;
                case ComplicationData.TYPE_LONG_TEXT:
                    complicationData = new ComplicationData.Builder(ComplicationData.TYPE_LONG_TEXT);
                    complicationData.setTapAction(permissionIntent);
                    complicationData.setLongText(
                            ComplicationText.plainText(getString(R.string.complication_no_gps_long)));
                    complicationManager.updateComplicationData(complicationId, complicationData.build());
                    break;
            }
        }
    }

    @Override
    public void onComplicationActivated(int complicationId, int type, ComplicationManager manager) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("COMPLICATION", "activated");
            getPermission();
        }else {
            GetLocationTask locationTask = new GetLocationTask(this, complicationId, type, manager);
            locationTask.execute();
        }
    }

    private void getPermission(){
        Intent intent = new Intent(this, PermissionActivity.class);
        intent.putExtra(getString(R.string.extra_permission_request_code), PermissionActivity.REQUEST_CODE_COMPLICATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onLocationFound(int complicationId, int dataType, Object complicationManager, Location location) {
        if(complicationManager instanceof ComplicationManager){

            String result="";
            try {
                WeatherTask weatherTask = new WeatherTask(this);
                weatherTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, location.getLatitude(), location.getLongitude());
                result = weatherTask.get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            Weather weather = null;
            try {
                weather = new Weather(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isCelsius = sharedPreferences.getBoolean(getString(R.string.KEY_PREF_IS_CELSIUS), false);

            String temperatureText = "";
            String tempMinText = "";
            String tempMaxText = "";
            if(weather != null) {
                long temp = Math.round(isCelsius ? WeatherUtil.kelvin2Celsius(weather.getTemperature()) :
                        WeatherUtil.kelvin2Fahrenheit(weather.getTemperature()));
                long tempMin = Math.round(isCelsius ? WeatherUtil.kelvin2Celsius(weather.getMinTemperature()) :
                        WeatherUtil.kelvin2Fahrenheit(weather.getMinTemperature()));
                long tempMax = Math.round(isCelsius ? WeatherUtil.kelvin2Celsius(weather.getMaxTemperature()) :
                        WeatherUtil.kelvin2Fahrenheit(weather.getMaxTemperature()));
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
                    if(weather != null) {
                        complicationData.setLongTitle(ComplicationText.plainText(
                                getString(R.string.complication_long_title, temperatureText, weather.getName())));
                    }
                    complicationData.setLongText(
                            ComplicationText.plainText(
                                    getString(R.string.complication_long_text, tempMinText, tempMaxText)));
                    break;
            }

            if(complicationData != null && weather != null) {
                complicationData.setIcon(Icon.createWithResource(this, weather.getIconResourceCode()));
                Intent intent = new Intent(this, MainActivity.class);
                PendingIntent openAppIntent = PendingIntent.getActivity(this, 0, intent, 0);

                complicationData.setTapAction(openAppIntent);

                ((ComplicationManager) complicationManager).updateComplicationData(complicationId, complicationData.build());
            }

        }
    }

}
