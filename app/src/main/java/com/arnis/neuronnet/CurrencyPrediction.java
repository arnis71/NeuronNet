package com.arnis.neuronnet;

import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arnis.neuronnet.Net.NeuralHelper;
import com.arnis.neuronnet.Net.NeuronNet;
import com.arnis.neuronnet.Other.ListAdapter;
import com.arnis.neuronnet.Other.Player;
import com.arnis.neuronnet.Other.Position;
import com.arnis.neuronnet.Other.Prefs;
import com.arnis.neuronnet.Other.Utility;
import com.arnis.neuronnet.Other.ValueChangeListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import okhttp3.internal.Util;

import static com.arnis.neuronnet.MainActivity.COMPLEX_ANALYSIS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ANALYTICS_MODE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_BRAINS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ERROR;
import static com.arnis.neuronnet.MainActivity.SETTINGS_FLOATING_WINDOW;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ITERATIONS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_LEARNRATE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_MOMENTUM;
import static com.arnis.neuronnet.MainActivity.SETTINGS_PREDICTION;
import static com.arnis.neuronnet.MainActivity.SETTINGS_PREFS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_STOCKS_TYPE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_TRAIN;
import static com.arnis.neuronnet.MainActivity.SETTINGS_TYPE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_VIEW;
import static com.arnis.neuronnet.MainActivity.SETTINGS_WINDOW;

public class CurrencyPrediction extends AppCompatActivity {

    private Button start;
    private LineChart ratesChart;
    private NeuralHelper helper;
    private Prefs mainPrefs;
    private ProgressBar pb;
    private ArrayList<Entry> moneyChartData;
    private int moneyX;
    private LineChart moneyChart;

    private ArrayList<Entry> rateAskChartData;
    private ArrayList<Entry> rateBidChartData;
    private int rateX;

    private ArrayList<Entry> predictionsChartData;
    ArrayList<ILineDataSet> rateDataSets;
    private ArrayList<Entry> positionsChartData;
    LineDataSet positionsLineData;


    LineDataSet askData;
    LineDataSet bidData;

    boolean firstClick=true;
    Player player;
    private NotificationManager manager;
    private Button delete;
    private LinearLayout signals;
    private HorizontalScrollView scroller;
    private TextView balance;
    private ListView positionsList;
    private ListAdapter listAdapter;

    // TODO: 28/09/2016 create automator
    // TODO: 28/09/2016 complex solution handle each predcition as a unit (no averaging)
    // TODO: 28/09/2016 clone nn when predciting
    // TODO: 28/09/2016 advanced trading mechanisms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencyprediction);

        start = (Button)findViewById(R.id.start_trading);
        ratesChart = (LineChart)findViewById(R.id.rates);
        pb = (ProgressBar) findViewById(R.id.pb);
        delete = (Button)findViewById(R.id.delete_data);
        signals = (LinearLayout)findViewById(R.id.signals_scroller);
        scroller = (HorizontalScrollView)findViewById(R.id.scroller);
        balance = (TextView)findViewById(R.id.balance);
        moneyChart = (LineChart)findViewById(R.id.money_chart);
        scroller.setHorizontalScrollBarEnabled(false);
        positionsList = (ListView)findViewById(R.id.positions_list);
        positionsList.setX(-1440f);

        listAdapter = new ListAdapter(this);
        positionsList.setAdapter(listAdapter);
        positionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utility.animateDrawerOut(positionsList);
            }
        });

        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        helper = new NeuralHelper(this);
        player = new Player(getApplicationContext(), helper);

        rateAskChartData = new ArrayList<Entry>();
        rateBidChartData = new ArrayList<Entry>();
        rateX=0;
        moneyChartData = new ArrayList<Entry>();
        moneyX=0;

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.deleteData();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstClick) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            player.setRateChangeListener(new ValueChangeListener() {
                                @Override
                                public void onValueChange(double value) {
                                }

                                @Override
                                public void onValueChange(String value) {

                                }

                                @Override
                                public void onValueChange(double ask, double bid) {
                                    updateRateChart(ask, bid);
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
                                public void onValueChange(String value) {

                                }

                                @Override
                                public void onValueChange(double ask, double bid) {
                                }

                                @Override
                                public void onValueChange(ArrayList<Double> values) {
                                    updatePredictions(values);
                                }
                            });
                            player.setPositionChangeListener(new ValueChangeListener() {
                                @Override
                                public void onValueChange(double value) {

                                }

                                @Override
                                public void onValueChange(String value) {

                                }

                                @Override
                                public void onValueChange(double id, double balance) {
                                    positionChanged(id,balance);
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
                                public void onValueChange(String value) {
                                    signalReceived(value);
                                }

                                @Override
                                public void onValueChange(double ask, double bid) {

                                }

                                @Override
                                public void onValueChange(ArrayList<Double> values) {
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateBalance(100);
                                    balance.setText("100$");
                                }
                            });
                            player.play();
                        }
                    }).start();
                    firstClick=false;
                } else {
                    player.stop();
                    firstClick=true;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Position.clear();
    }
    public void updateBalance(int value){
        moneyChartData.add(new Entry((float) moneyX++, (float) value));

        LineDataSet real = new LineDataSet(moneyChartData, "Money");
        real.setColor(Color.BLACK);
        real.setLineWidth(1.0f);

        LineData lineData = new LineData(real);
        moneyChart.setData(lineData);
        moneyChart.invalidate();
        String val = Integer.toString(value)+"$";
        balance.setText(val);
    }

    private void signalReceived(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView signal = new ImageView(getApplicationContext());
                signal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                if (value.equals("up")) {
                    signal.setImageResource(R.drawable.ic_action_arrow_top);
                }
                else if (value.equals("down")){
                    signal.setImageResource(R.drawable.ic_action_arrow_bottom);
                } else signal.setImageResource(R.drawable.ic_action_expand);

                addSignal(signal);
            }
        });

    }
    private void addSignal(ImageView signal){
        signals.addView(signal);
        Utility.animateScrollView(scroller);
    }

    private void positionChanged(final double id, final double balance){
        final Position position = Position.getPositionById((int)id);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
                if (position.getAction().equals(Position.BUY)){
                    positionsChartData = new ArrayList<Entry>();
                    positionsChartData.add(new Entry((float) rateX - 1, (float) position.getOpen()));
                    positionsLineData = new LineDataSet(positionsChartData, "Opened position");
                    positionsLineData.setColor(Color.BLUE);
                    positionsLineData.setCircleColor(getColor(position.getDirection()));
                    positionsLineData.setCircleColorHole(Color.BLUE);
                    positionsLineData.setCircleHoleRadius(2f);
                    positionsLineData.setCircleRadius(6f);
                    Utility.animateDrawerPop(positionsList);
                } else if (position.getAction().equals(Position.SELL)){
                    updateBalance((int) balance);
                    createNotification(position.getResult(),position.getAmount(),balance);
                }
            }
        });
    }


    private void createNotification(String Case, double amount1, double balance){
        String title="";
        String message="";
        int icon=0;
        int sound=0;
        switch (Case){
            case "success":title = "Player won";message = String.format("%.2f",amount1*0.7) + "$ won, current money "+String.format("%.2f",balance)+"$" ;icon = R.drawable.ic_action_arrow_top;sound = R.raw.chimes_glassy;break;
            case "fail":title = "Player lost";message = String.format("%.2f",amount1) + "$ lost, current money "+String.format("%.2f",balance)+"$";icon = R.drawable.ic_action_arrow_bottom;sound = R.raw.fail;break;
            case "draw":title = "Draw";message = String.format("%.2f",amount1) + "$ funds returned, current money "+String.format("%.2f",balance)+"$";icon = R.drawable.ic_action_expand;break;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(new long[]{1000,500,1000})
                .setLights(getColor(Case),3000,3000)
                .setSound(Uri.parse("android.resource://"+getPackageName()+"/"+sound));
        Notification fullScreenNotification = builder.build();
        manager.notify(1,fullScreenNotification);
    }

    private int getColor(String direction){
        switch (direction){
            case "up": return Color.GREEN;
            case "success": return Color.GREEN;
            case "down": return Color.RED;
            case "fail": return Color.RED;
            default: return Color.BLACK;
        }
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
                if (positionsLineData!=null)
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
                if (positionsLineData!=null)
                    rateDataSets.add(positionsLineData);

                LineData lineData = new LineData(rateDataSets);
                ratesChart.setData(lineData);
                ratesChart.invalidate();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        helper.addNets(mainPrefs);
        helper.setFloatingWindow(mainPrefs.getFloatWindow());
        helper.setProgressBar(pb);
        player.newHelper(helper);
        player.setMode(mainPrefs.isAnalyticsMode());
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

        NeuronNet.setLearningRate(Double.parseDouble(preferences.getString(SETTINGS_LEARNRATE,"0.0001")));
        NeuronNet.setMomentum(Double.parseDouble(preferences.getString(SETTINGS_MOMENTUM,"0.9")));
        mainPrefs.setFloatWindow(preferences.getInt(SETTINGS_FLOATING_WINDOW,50));
        mainPrefs.setAnalyticsMode(preferences.getBoolean(SETTINGS_ANALYTICS_MODE,true));
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this,Settings.class);
        intent.putExtra(SETTINGS_VIEW,"currency");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Bundle translateBundle = ActivityOptions.makeCustomAnimation(this,R.anim.slide_in_left,R.anim.slide_out_left).toBundle();
            startActivity(intent,translateBundle);
            return;
        }
        startActivity(intent);
    }

    public void saveBrains(View view) {
        if (helper.canSaveBrains()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("Save brains");

            String message = "This neural network is trained for " + mainPrefs.getSymbol() + " , " + Integer.toString(helper.getNet(0).getEpoch()) +
                    " times and has approximate error of "
                    + String.format("%.4f", helper.getNet(0).getTotalError() * 100) + "%";
            builder.setMessage(message);

            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final EditText input = new EditText(CurrencyPrediction.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            input.setLayoutParams(lp);
            input.setHint("brain name");
            builder.setView(input);
            builder.setPositiveButton("save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = input.getText().toString();
                    SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).edit();
                    editor.putString(SETTINGS_BRAINS, name).apply();
                    helper.setName(name);
                    helper.storeData();
                }
            });
            builder.show();
        }
    }

    public void openDrawer(View view) {
        Utility.animateDrawerIn(positionsList);
    }
}
