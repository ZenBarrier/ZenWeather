package com.zenbarrier.zenweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.zenbarrier.mylibrary.GetLocationTask;
import com.zenbarrier.mylibrary.WeatherTask;
import com.zenbarrier.mylibrary.WeatherUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements WeatherTask.WeatherTaskInterface,
        GetLocationTask.LocationTaskInterface {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mTextView;
    private WearableActionDrawer mWearableActionDrawer;
    private boolean mIsCelsius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView_main_temp);

        GetLocationTask locationTask = new GetLocationTask(this);
        locationTask.execute();

        mWearableActionDrawer = (WearableActionDrawer) findViewById(R.id.bottom_action_drawer);
        mWearableActionDrawer.peekDrawer();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsCelsius = sharedPreferences.getBoolean(getString(R.string.PREF_KEY_IS_CELSIUS), false);
        MenuItem unitMenuItem = mWearableActionDrawer.getMenu().findItem(R.id.menu_temperature_unit);
        if(mIsCelsius){
            unitMenuItem.setTitle("Change to °F");
        }else{
            unitMenuItem.setTitle("Change to °C");
        }
    }

    @Override
    public void onWeatherRetrieved(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject mainJson = jsonObject.getJSONObject("main");
            double temperature = WeatherUtil.kelvin2Fahrenheit(mainJson.getDouble("temp"));
            String name = jsonObject.getString("name");
            ((TextView)findViewById(R.id.textView_main_city)).setText(name);
            mTextView.setText(getString(R.string.degree_fahrenheit, Math.round(temperature)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changeUnit(MenuItem menuItem){
        mIsCelsius = !mIsCelsius;
        if(mIsCelsius){
            menuItem.setTitle("Change to °F");
        }else{
            menuItem.setTitle("Change to °C");
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(getString(R.string.PREF_KEY_IS_CELSIUS), mIsCelsius).apply();
        mWearableActionDrawer.peekDrawer();

    }

    @Override
    public void onLocationFound(Location location) {
        Log.d(TAG, location.toString());
        WeatherTask weatherTask = new WeatherTask(this);
        weatherTask.execute(location.getLatitude(), location.getLongitude());
    }

}
