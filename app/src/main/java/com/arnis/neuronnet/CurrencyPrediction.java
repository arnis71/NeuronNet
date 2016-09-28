package com.arnis.neuronnet;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arnis.neuronnet.Net.NeuralHelper;
import com.arnis.neuronnet.Other.Player;
import com.arnis.neuronnet.Other.Prefs;
import com.arnis.neuronnet.Other.ValueChangeListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.arnis.neuronnet.MainActivity.COMPLEX_ANALYSIS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_BRAINS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ERROR;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ITERATIONS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_PREDICTION;
import static com.arnis.neuronnet.MainActivity.SETTINGS_PREFS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_STOCKS_TYPE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_TRAIN;
import static com.arnis.neuronnet.MainActivity.SETTINGS_TYPE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_WINDOW;

public class CurrencyPrediction extends AppCompatActivity {

    private Button start;
    private LineChart moneyChart;
    private LineChart ratesChart;

    private NeuralHelper helper;
    private Prefs mainPrefs;
    private ProgressBar pb;
    private ArrayList<Entry> moneyChartData;
    private int moneyX;

    private ArrayList<Entry> rateAskChartData;
    private ArrayList<Entry> rateBidChartData;
    private int rateX;

    private ArrayList<Entry> predictionsChartData;
    ArrayList<ILineDataSet> rateDataSets;

    private ArrayList<Entry> positionsChartData;
    LineDataSet positionsLineData;
    private LinearLayout signals;
    private TextView timer;
    private TextView position;
    private TextView direction;
    private TextView amount;
    LineDataSet askData;
    LineDataSet bidData;

    // TODO: 28/09/2016 create automator
    // TODO: 28/09/2016 apply floating window e.g. 100 entries
    // TODO: 28/09/2016 complex solution handle each predcition as a unit (no averaging)
    // TODO: 28/09/2016 clone nn when predciting
    // TODO: 28/09/2016 advanced trading mechanisms
    // TODO: 28/09/2016 ability to save brains

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencyprediction);

        start = (Button)findViewById(R.id.start_trading);
        moneyChart = (LineChart)findViewById(R.id.money_stats);
        ratesChart = (LineChart)findViewById(R.id.rates);
        pb = (ProgressBar) findViewById(R.id.pb);
        signals = (LinearLayout)findViewById(R.id.signals_scroller);
        timer = (TextView)findViewById(R.id.timer);
        position = (TextView)findViewById(R.id.position_open);
        direction = (TextView)findViewById(R.id.position_direction);
        amount = (TextView)findViewById(R.id.position_amount);



        moneyChartData = new ArrayList<Entry>();
        moneyX=0;
        rateAskChartData = new ArrayList<Entry>();
        rateBidChartData = new ArrayList<Entry>();
        rateX=0;



        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Player player = new Player(getApplicationContext(),helper);
                        player.setMoneyChangeListener(new ValueChangeListener() {
                            @Override
                            public void onValueChange(double value) {
                                updateMoneyChart(value);
                            }

                            @Override
                            public void onValueChange(double ask, double bid) {

                            }

                            @Override
                            public void onPositionOpen(double at, String direction, double amount) {

                            }

                            @Override
                            public void onValueChange(ArrayList<Double> values) {

                            }
                        });
                        player.setRateChangeListener(new ValueChangeListener() {
                            @Override
                            public void onValueChange(double value) {
                            }

                            @Override
                            public void onValueChange(double ask, double bid) {
                                updateRateChart(ask,bid);
                            }

                            @Override
                            public void onPositionOpen(double at, String direction, double amount) {

                            }

                            @Override
                            public void onValueChange(ArrayList<Double> values) {

                            }
                        });
                        player.setPredictionChangeListener(new ValueChangeListener() {
                            @Override
                            public void onValueChange(double value) {

                            }

                            @Override
                            public void onValueChange(double ask, double bid) {
                            }

                            @Override
                            public void onPositionOpen(double at, String direction, double amount) {

                            }

                            @Override
                            public void onValueChange(ArrayList<Double> values) {
                                updatePredictions(values);
                            }
                        });
                        player.setPositionOpenedListener(new ValueChangeListener() {
                            @Override
                            public void onValueChange(double value) {

                            }

                            @Override
                            public void onValueChange(double ask, double bid) {

                            }

                            @Override
                            public void onPositionOpen(double at, String direction, double amount) {
                                    openPosition(at, direction, amount);
                            }

                            @Override
                            public void onValueChange(ArrayList<Double> values) {

                            }
                        });
                        player.setSignalListener(new ValueChangeListener() {
                            @Override
                            public void onValueChange(double value) {

                            }

                            @Override
                            public void onValueChange(double ask, double bid) {

                            }

                            @Override
                            public void onPositionOpen(double at, String direction, double amount) {

                            }

                            @Override
                            public void onValueChange(ArrayList<Double> values) {
                                signalReceived(values);
                            }
                        });
                        player.play();
                    }
                }).start();
            }
        });

    }

    private void signalReceived(ArrayList<Double> values) {
        double sum=0;
        for (int i=0;i<values.size()-1;i++){
            sum+=(values.get(i+1)-values.get(i))/values.get(i);
        }
        sum=sum/(values.size()-1);
        sum*=100;

        final double finalSum1 = sum;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView signal = new ImageView(getApplicationContext());
                signal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                if (finalSum1 >0) {
                    signal.setImageResource(R.drawable.ic_action_arrow_top);
                }
                else {
                    signal.setImageResource(R.drawable.ic_action_arrow_bottom);
                }

                signals.addView(signal);
            }
        });

    }

    private void openPosition(final double at, final String direction1, final double amount1){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO: 28/09/2016 leave dot on the chart
                startTimer();
                position.setText(String.format("%.2f",at));
                direction.setText(direction1);
                amount.setText(String.format("%.2f",amount1));
                positionsChartData = new ArrayList<Entry>();
                positionsChartData.add(new Entry((float) rateX-1, (float)at));
                positionsLineData = new LineDataSet(positionsChartData, "Opened position");
                positionsLineData.setColor(Color.BLUE);
                if (direction1.equals("up"))
                    positionsLineData.setCircleColor(Color.GREEN);
                else positionsLineData.setCircleColor(Color.RED);
                positionsLineData.setCircleRadius(5f);
            }
        });
    }

    private void startTimer(){
        new CountDownTimer(60000,1000){
            int i=60;
            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(Integer.toString(i--));
            }

            @Override
            public void onFinish() {
                positionsChartData=null;
            }
        }.start();
    }

    private void updatePredictions(final ArrayList<Double> predictions){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO: 28/09/2016 for complex solutions
//                for (int i=0;i<predictions.size();i++) {
//                    LineDataSet set1 = new LineDataSet(predictions.get(i), helper.getName(i));
//                    set1.setColor(helper.getColor(i));
//
//                    rateDataSets.add(set1);
//                }
                rateDataSets = new ArrayList<ILineDataSet>();

                predictionsChartData = new ArrayList<Entry>();

                int i = 0;
                for (Double d:predictions){
                    predictionsChartData.add(new Entry((float) rateX-1+i, d.floatValue()));
                    i++;
                }

                LineDataSet predictionsSet = new LineDataSet(predictionsChartData, "Predictions");
                predictionsSet.setColor(Color.YELLOW);
                predictionsSet.setLineWidth(2.0f);

                rateDataSets.add(askData);
                rateDataSets.add(bidData);
                rateDataSets.add(predictionsSet);
                if (positionsChartData!=null)
                    rateDataSets.add(positionsLineData);

                LineData lineData = new LineData(rateDataSets);
                ratesChart.setData(lineData);
                ratesChart.invalidate();
            }
        });
    }

    private void updateRateChart(final double ask, final double bid){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rateDataSets = new ArrayList<ILineDataSet>();

                rateAskChartData.add(new Entry((float) rateX, (float) ask));
                rateBidChartData.add(new Entry((float) rateX, (float) bid));
                rateX++;

                askData = new LineDataSet(rateAskChartData, "Ask");
                askData.setColor(Color.RED);
                askData.setLineWidth(1.0f);

                bidData = new LineDataSet(rateBidChartData, "Bid");
                bidData.setColor(Color.GREEN);
                bidData.setLineWidth(1.0f);

                LineDataSet predictionsSet = new LineDataSet(predictionsChartData, "Predictions");
                predictionsSet.setColor(Color.YELLOW);
                predictionsSet.setLineWidth(2.0f);

                rateDataSets.add(askData);
                rateDataSets.add(bidData);
                rateDataSets.add(predictionsSet);
                if (positionsChartData!=null)
                    rateDataSets.add(positionsLineData);

                LineData lineData = new LineData(rateDataSets);
                ratesChart.setData(lineData);
                ratesChart.invalidate();
            }
        });

    }

    private void updateMoneyChart(final double value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                moneyChartData.add(new Entry((float) moneyX++, (float) value));

                LineDataSet real = new LineDataSet(moneyChartData, "Money");
                real.setColor(Color.BLACK);
                real.setLineWidth(1.0f);

                LineData lineData = new LineData(real);
                moneyChart.setData(lineData);
                moneyChart.invalidate();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        helper = new NeuralHelper(this);
        helper.addNets(mainPrefs);
        helper.setProgressBar(pb);
    }

    @Override
    protected void onPause() {
        super.onPause();
        helper.storeData();
    }

    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);

        mainPrefs = new Prefs(preferences.getString(SETTINGS_STOCKS_TYPE, "n/a"),
                preferences.getString(SETTINGS_BRAINS, "default"),
                preferences.getBoolean(COMPLEX_ANALYSIS, false),
                preferences.getBoolean(SETTINGS_TRAIN, false),
                preferences.getInt(SETTINGS_ITERATIONS, 5000),
                preferences.getInt(SETTINGS_TYPE, 0),
                preferences.getInt(SETTINGS_ERROR, 0),
                preferences.getInt(SETTINGS_WINDOW, 2),
                preferences.getInt(SETTINGS_PREDICTION, 1));

    }

    public void openSettings(View view) {
        Intent intent = new Intent(this,Settings.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Bundle translateBundle = ActivityOptions.makeCustomAnimation(this,R.anim.slide_in_left,R.anim.slide_out_left).toBundle();
            startActivity(intent,translateBundle);
            return;
        }
        startActivity(intent);
    }
}
