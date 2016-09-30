package com.arnis.neuronnet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String BRAINS_STORAGE = "brains_storage";
    public static final String SETTINGS_PREFS = "settings";
    public static final String SETTINGS_BRAINS = "brains_name";
    public static final String SETTINGS_ERROR = "error";
    public static final String SETTINGS_TYPE = "type";
    public static final String SETTINGS_ITERATIONS = "iterations";
    public static final String SETTINGS_TRAIN = "run";
    public static final String SETTINGS_STOCKS_TYPE = "stock_type";
    public static final String SETTINGS_WINDOW = "window";
    public static final String SETTINGS_PREDICTION = "prediction";
    public static final String SETTINGS_FLOATING_WINDOW = "floatwindow";
    public static final String SETTINGS_MOMENTUM = "momentum";
    public static final String SETTINGS_LEARNRATE = "learnrate";
    public static final String SETTINGS_ANALYTICS_MODE = "analmode";
    public static final String COMPLEX_ANALYSIS = "complex";


    public static final String SETTINGS_VIEW = "setview";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void stocks(View view) {
        Intent intent = new Intent(this,StockPrediction.class);
        startActivity(intent);
    }

    public void currency(View view) {
        Intent intent = new Intent(this,CurrencyPrediction.class);
        startActivity(intent);
    }
}
