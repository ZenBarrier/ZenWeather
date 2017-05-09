package com.zenbarrier.mylibrary;

import org.json.JSONException;
import org.json.JSONObject;


public class Weather extends JSONObject {
    private JSONObject mMainJSON;
    private JSONObject mWeatherJSON;
    public Weather(String json) throws JSONException {
        super(json);
        mMainJSON = this.getJSONObject("main");
        mWeatherJSON = this.getJSONArray("weather").getJSONObject(0);
    }
    public double getTemperature() {
        try {
            return mMainJSON.getDouble("temp");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    public double getMinTemperature() {
        try {
            return mMainJSON.getDouble("temp_min");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    public double getMaxTemperature() {
        try {
            return mMainJSON.getDouble("temp_max");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    public String getName() {
        try {
            return this.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
    public int getIconResourceCode() {
        try {
            return iconCode(mWeatherJSON.getString("icon"));
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
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
