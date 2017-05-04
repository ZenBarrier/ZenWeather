package com.zenbarrier.zenweather;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.zenbarrier.mylibrary.WeatherTask;

public class MainActivity extends Activity implements WeatherTask.OnTaskCompleted {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView_main);
        mTextView.setText("Hello World");

        WeatherTask weatherTask = new WeatherTask(this);
        weatherTask.execute();
    }

    @Override
    public void onTaskCompleted(String result) {
        mTextView.setText(result);
    }
}
