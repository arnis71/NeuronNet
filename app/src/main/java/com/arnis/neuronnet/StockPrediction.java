package com.arnis.neuronnet;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnis.neuronnet.Net.NeuralHelper;
import com.arnis.neuronnet.Net.NeuronNet;
import com.arnis.neuronnet.Other.Prefs;
import com.arnis.neuronnet.Retrofit.Data;
import com.arnis.neuronnet.Retrofit.Results;
import com.arnis.neuronnet.Retrofit.Stock;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.arnis.neuronnet.MainActivity.COMPLEX_ANALYSIS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_BRAINS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ERROR;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ITERATIONS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_LEARNRATE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_MOMENTUM;
import static com.arnis.neuronnet.MainActivity.SETTINGS_PREDICTION;
import static com.arnis.neuronnet.MainActivity.SETTINGS_PREFS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_STOCKS_TYPE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_TRAIN;
import static com.arnis.neuronnet.MainActivity.SETTINGS_TYPE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_WINDOW;

public class StockPrediction extends AppCompatActivity {


    private Prefs mainPrefs;
    private LineChart lineChart;
    private List<Stock> stocks;
    private List<Stock> checkStocks;
    private EditText startDate;
    private EditText endDate;

    private TextView error;
    private ProgressBar pb;

    private NeuralHelper helper;
    List<ILineDataSet> chartDataSets;
    List<Entry> realData;
    int dataSch=0;


    private TextView currentBrain;
    private TextView currentStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockprediction);

        lineChart = (LineChart) findViewById(R.id.line_chart);
        startDate = (EditText) findViewById(R.id.from);
        endDate = (EditText) findViewById(R.id.to);
        error = (TextView) findViewById(R.id.error);
        pb = (ProgressBar) findViewById(R.id.pb);
        currentBrain = (TextView) findViewById(R.id.current_brain);
        currentStock = (TextView) findViewById(R.id.current_stock);

        // TODO: 25/09/2016 control hidden neurons
        // TODO: 27/09/2016 add period of trainig in info
        // TODO: 27/09/2016 dont judge by average in stocks
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        applyInfo();
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

        NeuronNet.setLearningRate(Double.parseDouble(preferences.getString(SETTINGS_LEARNRATE,"0.0001")));
        NeuronNet.setMomentum(Double.parseDouble(preferences.getString(SETTINGS_MOMENTUM,"0.9")));

    }

    private void applyInfo() {
        currentBrain.setText(mainPrefs.getNeuralName());
        currentStock.setText(mainPrefs.getSymbol());
    }



    public void start(final View view) {
        view.setClickable(false);
        view.setBackgroundColor(Color.RED);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Results body = Data.getStock(mainPrefs.getSymbol(), startDate.getText().toString(), endDate.getText().toString()).execute().body();
                    if (body!=null){
                        stocks = body.quote;
                        Collections.reverse(stocks);
                        helper.addStockData(stocks);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buildBaseChart();
                            }
                        });
                    }
                    helper.run(error);
                    if (!helper.isTraining()){
                        body = Data.checkStock().execute().body();
                        checkStocks = body.quote;
                        Collections.reverse(checkStocks);
                        helper.join();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateChart(helper.stockPredictionsToChart(stocks));}});
                    }else helper.join();
                } catch (IOException e) {
                    e.printStackTrace();
                }



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StockPrediction.this, "Computation completed", Toast.LENGTH_LONG).show();
                        view.setClickable(true);
                        view.setBackgroundColor(Color.GREEN);
                    }
                });
            }
        }).start();
    }

    private void buildBaseChart(){
        chartDataSets = new ArrayList<ILineDataSet>();
        realData = new ArrayList<Entry>();

        dataSch=0;
        for (Stock stock : stocks) {
            realData.add(new Entry((float) dataSch++, (float) stock.average()));
        }
        LineDataSet real = new LineDataSet(realData, "Real");
        real.setColor(Color.BLACK);
        real.setLineWidth(1.5f);
        chartDataSets.add(real);


        LineData lineData = new LineData(chartDataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }


    private void updateChart(ArrayList<List<Entry>> entries) {

        if (checkStocks!=null&&checkStocks.size()>0){
            for (Stock stock : checkStocks) {
                realData.add(new Entry((float) dataSch++, (float) stock.average()));
            }
        }
        if (entries!=null){
            for (int i=0;i<entries.size();i++) {
                LineDataSet set1 = new LineDataSet(entries.get(i), helper.getName(i));
                set1.setColor(helper.getColor(i));

                chartDataSets.add(set1);
            }
        }
        LineData lineData = new LineData(chartDataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
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

    public void saveBrains(View view) {
        if (helper.canSaveBrains()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("Save brains");

            String message = "This neural network is trained for " + mainPrefs.getSymbol() + " stocks, " + Integer.toString(helper.getNet(0).getEpoch()) +
                    " times and has approximate error of "
                    + String.format("%.4f", helper.getNet(0).getTotalError() * 100) + "%";
            builder.setMessage(message);

            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final EditText input = new EditText(StockPrediction.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            input.setLayoutParams(lp);
            input.setHint("brain name");
            builder.setView(input);
            builder.setPositiveButton("save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = input.getText().toString();
                    currentBrain.setText(name);
                    SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE).edit();
                    editor.putString(SETTINGS_BRAINS, name).apply();
                    helper.setName(name);
                    helper.storeData();
                }
            });
            builder.show();
        }
    }
}
