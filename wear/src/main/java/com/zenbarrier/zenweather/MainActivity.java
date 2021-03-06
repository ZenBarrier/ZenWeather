package com.zenbarrier.zenweather;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.complications.ProviderUpdateRequester;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zenbarrier.mylibrary.GetLocationTask;
import com.zenbarrier.mylibrary.PermissionActivity;
import com.zenbarrier.mylibrary.Weather;
import com.zenbarrier.mylibrary.WeatherTask;
import com.zenbarrier.mylibrary.WeatherUtil;

import org.json.JSONException;

public class MainActivity extends Activity implements WeatherTask.WeatherTaskInterface,
        GetLocationTask.LocationTaskInterface {

    private TextView mTextView;
    private WearableActionDrawer mWearableActionDrawer;
    private boolean mIsCelsius;
    private double mTemperatureK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textView_main_temp);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            getPermission(null);
        }
        else {
            init();
        }
    }

    public void getPermission(View view){

        Intent complicationIntent = getIntent();
        int requestCode = complicationIntent.getIntExtra(PermissionActivity.KEY_EXTRA_REQUEST_CODE,
                PermissionActivity.REQUEST_CODE_MAIN_WEAR);

        Intent intent = new Intent(this, PermissionActivity.class);
        intent.putExtra(PermissionActivity.KEY_EXTRA_REQUEST_CODE, requestCode);
        startActivityForResult(intent, requestCode);
    }

    private void init() {

        GetLocationTask locationTask = new GetLocationTask(this);
        locationTask.execute();
        ImageView imageViewLocation = (ImageView) findViewById(R.id.imageView_main_location);
        imageViewLocation.setImageResource(R.drawable.ic_location_on);
        imageViewLocation.setOnClickListener(null);

        mWearableActionDrawer = (WearableActionDrawer) findViewById(R.id.bottom_action_drawer);
        mWearableActionDrawer.peekDrawer();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsCelsius = sharedPreferences.getBoolean(getString(R.string.KEY_PREF_IS_CELSIUS), false);

        long updateTime = sharedPreferences.getLong(WeatherTask.KEY_PREF_WEATHER_TIME_STAMP, System.currentTimeMillis());
        long currentTime = System.currentTimeMillis();
        long timeSinceUpdate = (currentTime - updateTime)/60000;

        ((TextView)findViewById(R.id.textView_main_time)).setText(getString(R.string.update_time, timeSinceUpdate));

        MenuItem unitMenuItem = mWearableActionDrawer.getMenu().findItem(R.id.menu_temperature_unit);
        if (mIsCelsius) {
            unitMenuItem.setTitle("Change to °F");
        } else {
            unitMenuItem.setTitle("Change to °C");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PermissionActivity.REQUEST_CODE_MAIN_WEAR){
            init();
        }else if(resultCode == RESULT_CANCELED && requestCode == PermissionActivity.REQUEST_CODE_MAIN_WEAR){
            mTextView.setText(R.string.complication_no_gps_long);
            ((ImageView)findViewById(R.id.imageView_main_location)).setImageResource(R.drawable.ic_location_off);
        }else if(requestCode == PermissionActivity.REQUEST_CODE_COMPLICATION){
            updateComplication();
            finish();
        }
    }

    @Override
    public void onWeatherRetrieved(String result) {
        try {
            Weather weather = new Weather(result);
            mTemperatureK = weather.getTemperature();
            String name = weather.getName();
            ((ImageView)findViewById(R.id.imageView_main_icon)).setImageResource(weather.getIconResourceCode());
            ((TextView)findViewById(R.id.textView_main_city)).setText(name);
            setTemperatureDisplay(mTemperatureK);

            updateComplication();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTemperatureDisplay(double temperatureK){
        long temp = Math.round(mIsCelsius ? WeatherUtil.kelvin2Celsius(temperatureK) :
                WeatherUtil.kelvin2Fahrenheit(temperatureK));
        mTextView.setText(mIsCelsius ? getString(R.string.degree_celsius, temp) :
                getString(R.string.degree_fahrenheit, temp));
    }

    public void changeUnit(MenuItem menuItem){
        mIsCelsius = !mIsCelsius;
        if(mIsCelsius){
            menuItem.setTitle("Change to °F");
        }else{
            menuItem.setTitle("Change to °C");
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(getString(R.string.KEY_PREF_IS_CELSIUS), mIsCelsius).apply();

        setTemperatureDisplay(mTemperatureK);

        updateComplication();

        mWearableActionDrawer.peekDrawer();

    }

    private void updateComplication(){
        ComponentName componentName = new ComponentName(this, TemperatureComplicationService.class);
        ProviderUpdateRequester providerUpdateRequester = new ProviderUpdateRequester(this, componentName);
        providerUpdateRequester.requestUpdateAll();
    }

    @Override
    public void onLocationFound(Location location) {
        WeatherTask weatherTask = new WeatherTask(this);
        weatherTask.execute(location.getLatitude(), location.getLongitude());
    }

    public void aboutDialog(MenuItem item) throws PackageManager.NameNotFoundException {
        String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.about_message, version))
                .setTitle(R.string.menu_about)
        .setNeutralButton(R.string.close_about, null);
        builder.show();
    }
}
