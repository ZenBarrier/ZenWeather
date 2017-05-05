package com.zenbarrier.zenweather;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zenbarrier.mylibrary.GetLocationTask;
import com.zenbarrier.mylibrary.WeatherTask;
import com.zenbarrier.mylibrary.WeatherUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements WeatherTask.WeatherTaskInterface,
        GetLocationTask.LocationTaskInterface, WearableActionDrawer.OnMenuItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mTextView;
    private WearableDrawerLayout mWearableDrawerLayout;
    private WearableActionDrawer mWearableActionDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWearableDrawerLayout = (WearableDrawerLayout) findViewById(R.id.drawer_layout);
        mWearableDrawerLayout.peekDrawer(Gravity.BOTTOM);

        mTextView = (TextView) findViewById(R.id.textView_main_temp);

        GetLocationTask locationTask = new GetLocationTask(this);
        locationTask.execute();

        mWearableActionDrawer = (WearableActionDrawer) findViewById(R.id.bottom_action_drawer);
        mWearableActionDrawer.setOnMenuItemClickListener(this);
        mWearableActionDrawer.setTitle("title");

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

    @Override
    public void onLocationFound(Location location) {
        Log.d(TAG, location.toString());
        WeatherTask weatherTask = new WeatherTask(this);
        weatherTask.execute(location.getLatitude(), location.getLongitude());
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_location:
                break;
            case R.id.menu_temperature_unit:
                break;
            default:
                return false;
        }
        return true;
    }
}
