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

import com.arnis.neuronnet.Net.NeuronNet;
import com.arnis.neuronnet.Other.OnCompleteListener;
import com.arnis.neuronnet.Other.Prefs;
import com.arnis.neuronnet.Other.ValueChangeListener;
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
import java.util.Map;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {



    private NeuronNet neuralNetwork1;
    private NeuronNet neuralNetwork2;
    private NeuronNet neuralNetwork3;
    private NeuronNet neuralNetwork4;
    private NeuronNet neuralNetwork5;
    private LineChart lineChart;
    private List<Stock> data;
    private List<Stock> checkData;
    private EditText startDate;
    private EditText endDate;

    private TextView error;
    private ProgressBar pb;
    private Prefs prefs;

    public static final String BRAINS_STORAGE = "brains_storage";
    private TextView currentBrain;
    private TextView currentStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = (LineChart)findViewById(R.id.line_chart);
        startDate = (EditText)findViewById(R.id.from);
        endDate = (EditText)findViewById(R.id.to);
        error = (TextView)findViewById(R.id.error);
        pb = (ProgressBar)findViewById(R.id.pb);
        currentBrain = (TextView)findViewById(R.id.current_brain);
        currentStock = (TextView)findViewById(R.id.current_stock);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        applyInfo();

        // TODO: 25/09/2016 pass stock window and prediction parameters
        neuralNetwork1 = NeuronNet.requestStockSolvingNN(getApplicationContext(),prefs,2,1);
//        neuralNetwork2 = NeuronNet.requestStockSolvingNN(getApplicationContext(),prefs,4,2);
//        neuralNetwork3 = NeuronNet.requestStockSolvingNN(getApplicationContext(),prefs,7,2);
//        neuralNetwork4 = NeuronNet.requestStockSolvingNN(getApplicationContext(),prefs,10,3);
//        neuralNetwork5 = NeuronNet.requestStockSolvingNN(getApplicationContext(),prefs,14,3);


        progressBarSetup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        neuralNetwork1.store(prefs.getBrainName(),getApplicationContext());
    }

    private void progressBarSetup() {
        pb.setMax(neuralNetwork1.getMaxIterations());
        neuralNetwork1.setEpochListener(new ValueChangeListener() {
            @Override
            public void onValueChange(final double value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setProgress((int) value);
                    }
                });
            }
        });neuralNetwork1 = NeuronNet.requestStockSolvingNN(getApplicationContext(),prefs,10,3);
    }

    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences(Settings.SETTINGS_PREFS,MODE_PRIVATE);
        prefs = new Prefs(preferences.getString(Settings.SETTINGS_STOCKS_TYPE,"no stock"),
                preferences.getString(Settings.SETTINGS_BRAINS,"default"),
                preferences.getBoolean(Settings.SETTINGS_TRAIN,false),
                preferences.getInt(Settings.SETTINGS_ITERATIONS,5000),
                preferences.getInt(Settings.SETTINGS_TYPE,0),
                preferences.getInt(Settings.SETTINGS_ERROR,0));
    }
    private void applyInfo(){
        currentBrain.setText(prefs.getBrainName());
        currentStock.setText(prefs.getStockType());
    }

    public void start(final View view) {
            view.setClickable(false);
            view.setBackgroundColor(Color.RED);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response<Results> response = Data.get(prefs.getStockType(), startDate.getText().toString(), endDate.getText().toString()).execute();
                        if (response.isSuccessful()&&response.body()!=null) {
                            data = response.body().quote;
                            Collections.reverse(data);

                            neuralNetwork1.addStockData(data);
                            neuralNetwork1.setName(prefs.getBrainName());
                            neuralNetwork1.startWithListener(new OnCompleteListener() {
                                @Override
                                public void onComplete() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            buildChart(null);
                                            view.setClickable(true);
                                            view.setBackgroundColor(Color.GREEN);
                                            if (neuralNetwork1.isTraining()) {
                                                error.setText(String.format("%.4f", neuralNetwork1.getTotalError() * 100) + "%");
                                            }
                                        }
                                    });
                                }
                            });
                            if (!neuralNetwork1.isTraining()) {
                                checkData = Data.check().execute().body().quote;
                                Collections.reverse(checkData);
                                neuralNetwork1.join();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        buildChart(render(neuralNetwork1.getRawResults()));
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Data pull error", Toast.LENGTH_SHORT).show();
                                    view.setClickable(true);
                                    view.setBackgroundColor(Color.GREEN);
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                view.setClickable(true);
                                view.setBackgroundColor(Color.GREEN);
                            }
                        });
                    }
                }
            }).start();
    }


    private void buildChart(ArrayList<Double> render) {
        List<Entry> realData = new ArrayList<Entry>();
        List<Entry> nnData = new ArrayList<Entry>();

        int i=0;
        if (checkData==null||render==null) {
            for (Stock stock : data) {
                realData.add(new Entry((float) i++, (float) stock.average()));
            }
            i = 0;
        } else {
            for (Stock stock : checkData) {
                realData.add(new Entry((float) i++, (float) stock.average()));
            }
            i = 0;
        }

        LineDataSet real = new LineDataSet(realData, "Real");
        real.setColor(Color.BLUE);
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(real);

        if (render!=null){
            for (Double d: render) {
                nnData.add(new Entry((float)i++, d.floatValue()));
            }
            LineDataSet nn = new LineDataSet(nnData, "Predictions");
            nn.setColor(Color.RED);
            dataSets.add(nn);
        }


        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh
    }

    private ArrayList<Double> render(Map<String, double[]> arr){
        ArrayList<Double> res = new ArrayList<>();
        if (!neuralNetwork1.isTraining()) {
            double[] temp = arr.get(prefs.getStockType());
            double last = data.get(data.size() - 1).average();
            for (int i = 0; i < data.size(); i++) {
                res.add(last);
            }
            double next = last + (last * temp[0]);
            res.add(next);
            res.add(next + (next * temp[1]));
        }
        return res;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("Save brains");

        String message = "This neural network is trained for "+ prefs.getStockType()+ " stocks, "+ Integer.toString(neuralNetwork1.getEpoch())+
                " times and has approximate error of "
                + String.format("%.4f", neuralNetwork1.getTotalError()*100) + "%";
        builder.setMessage(message);

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        input.setHint("brain name");
        builder.setView(input);
        builder.setPositiveButton("save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences(BRAINS_STORAGE,MODE_PRIVATE).edit();
                String name = input.getText().toString();
                editor.putString(name,name).apply();
                currentBrain.setText(name);
                getSharedPreferences(Settings.SETTINGS_PREFS,MODE_PRIVATE).edit().putString(Settings.SETTINGS_BRAINS,name).apply();
                neuralNetwork1.store(name,getApplicationContext());
            }
        });
        builder.show();

    }
}
