package com.zenbarrier.mylibrary;


public class WeatherUtil {
    public static double kelvin2Fahrenheit(double kelvin){
        return celsius2Fahrenheit(kelvin2Celsius(kelvin));
    }
    public static double kelvin2Celsius(double kelvin){
        return kelvin - 273.15;
    }
    public static double celsius2Fahrenheit(double celsius){
        return celsius * 1.8 + 32.0;
    }
}
