package com.zenbarrier.mylibrary;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by Anthony on 5/3/2017.
 * This file is the fragment that holds all the preferences
 */

public class WeatherTask extends AsyncTask<Void, Void, String> {
    private Context mContext;

    public interface OnTaskCompleted{
        void onTaskCompleted(String result);
    }

    @Override
    protected String doInBackground(Void... params) {
        return "hello result";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(mContext instanceof OnTaskCompleted){
            ((OnTaskCompleted) mContext).onTaskCompleted(result);
        }
    }

    public WeatherTask(Context context){
        mContext = context;
    }

}
