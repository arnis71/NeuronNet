package com.arnis.neuronnet;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    private NeuronNet neuralNetwork;
    private Prefs prefs;
    private LineChart lineChart;
    private List<Stock> data;
    private List<Stock> checkData;
    private EditText startDate;
    private EditText endDate;

    private TextView error;
    private ProgressBar pb;

    private Prefs prefs1;
    private Prefs prefs2;
    private Prefs prefs3;
    private Prefs prefs4;
    private Prefs prefs5;
    private NeuronNet neuralNetwork1;
    private NeuronNet neuralNetwork2;
    private NeuronNet neuralNetwork3;
    private NeuronNet neuralNetwork4;
    private NeuronNet neuralNetwork5;
    int nntype;
    int nnerror;

    public static final String BRAINS_STORAGE = "brains_storage";
    private TextView currentBrain;
    private TextView currentStock;

    private boolean isComplex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = (LineChart) findViewById(R.id.line_chart);
        startDate = (EditText) findViewById(R.id.from);
        endDate = (EditText) findViewById(R.id.to);
        error = (TextView) findViewById(R.id.error);
        pb = (ProgressBar) findViewById(R.id.pb);
        currentBrain = (TextView) findViewById(R.id.current_brain);
        currentStock = (TextView) findViewById(R.id.current_stock);

        // TODO: 25/09/2016 control hidden neurons
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        applyInfo();

        if (isComplex) {
            prefs1 = new Prefs(prefs.getStockType(), prefs.getStockType() + " ultra short", prefs.isTrain(), prefs.getIterations(), nntype, nnerror, 2, 1);
            prefs2 = new Prefs(prefs.getStockType(), prefs.getStockType() + " short", prefs.isTrain(), prefs.getIterations(), nntype, nnerror, 4, 2);
            prefs3 = new Prefs(prefs.getStockType(), prefs.getStockType() + " medium", prefs.isTrain(), prefs.getIterations(), nntype, nnerror, 7, 2);
            prefs4 = new Prefs(prefs.getStockType(), prefs.getStockType() + " long", prefs.isTrain(), prefs.getIterations(), nntype, nnerror, 10, 3);
            prefs5 = new Prefs(prefs.getStockType(), prefs.getStockType() + " ultra long", prefs.isTrain(), prefs.getIterations(), nntype, nnerror, 14, 3);

            neuralNetwork1 = NeuronNet.requestStockSolvingNN(getApplicationContext(), prefs1);
            neuralNetwork2 = NeuronNet.requestStockSolvingNN(getApplicationContext(), prefs2);
            neuralNetwork3 = NeuronNet.requestStockSolvingNN(getApplicationContext(), prefs3);
            neuralNetwork4 = NeuronNet.requestStockSolvingNN(getApplicationContext(), prefs4);
            neuralNetwork5 = NeuronNet.requestStockSolvingNN(getApplicationContext(), prefs5);
        } else {
            neuralNetwork = NeuronNet.requestStockSolvingNN(getApplicationContext(), prefs);
        }

        progressBarSetup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isComplex) {
            storeData(neuralNetwork1, prefs1);
            storeData(neuralNetwork2, prefs2);
            storeData(neuralNetwork3, prefs3);
            storeData(neuralNetwork4, prefs4);
            storeData(neuralNetwork5, prefs5);
        } else if (neuralNetwork.getName()!=null&&!neuralNetwork.getName().equals("default"))
            storeData(neuralNetwork, prefs);
    }

    private void progressBarSetup() {
        if (!isComplex) {
            pb.setMax(neuralNetwork.getMaxIterations());
            neuralNetwork.setIterationListener(new ValueChangeListener() {
                @Override
                public void onValueChange(final double value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setProgress((int) value);
                        }
                    });

                }
            });
        } else{
            pb.setMax(neuralNetwork5.getMaxIterations());
            neuralNetwork5.setIterationListener(new ValueChangeListener() {
                @Override
                public void onValueChange(final double value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setProgress((int) value);
                        }
                    });

                }
            });
        }
    }

    private void storeData(NeuronNet net, Prefs prefs) {
        net.store(prefs.getBrainName(), getApplicationContext());
    }

    private void run(final NeuronNet net, final Prefs prefs) {
        net.addStockData(data);
        net.setName(prefs.getBrainName());
        net.startWithListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (net.isTraining()) {
                            error.setText(String.format("%.4f", net.getTotalError() * 100) + "%");
                        }
                    }
                });
            }
        });
    }

    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences(Settings.SETTINGS_PREFS, MODE_PRIVATE);

        isComplex = preferences.getBoolean(Settings.COMPLEX_ANALYSIS, false);
        nntype = preferences.getInt(Settings.SETTINGS_TYPE, 0);
        nnerror = preferences.getInt(Settings.SETTINGS_ERROR, 0);

        prefs = new Prefs(preferences.getString(Settings.SETTINGS_STOCKS_TYPE, "no stock"),
                preferences.getString(Settings.SETTINGS_BRAINS, "default"),
                preferences.getBoolean(Settings.SETTINGS_TRAIN, false),
                preferences.getInt(Settings.SETTINGS_ITERATIONS, 5000),
                nntype,
                nnerror,
                preferences.getInt(Settings.SETTINGS_WINDOW, 2),
                preferences.getInt(Settings.SETTINGS_PREDICTION, 1));

    }

    private void applyInfo() {
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
                    Results body = Data.get(prefs.getStockType(), startDate.getText().toString(), endDate.getText().toString()).execute().body();
                    if (body!=null){
                        data = body.quote;
                        Collections.reverse(data);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buildChart(null);
                            }
                        });
                    }

                    if (isComplex) {
                        MainActivity.this.run(neuralNetwork1, prefs1);
                        MainActivity.this.run(neuralNetwork2, prefs2);
                        MainActivity.this.run(neuralNetwork3, prefs3);
                        MainActivity.this.run(neuralNetwork4, prefs4);
                        MainActivity.this.run(neuralNetwork5, prefs5);
                        if (!neuralNetwork1.isTraining()) {
                            body = Data.check().execute().body();
                            if (body != null) {
                                checkData = body.quote;
                                Collections.reverse(checkData);
                                neuralNetwork1.join();
                                neuralNetwork2.join();
                                neuralNetwork3.join();
                                neuralNetwork4.join();
                                neuralNetwork5.join();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        buildChart(render(neuralNetwork1), render(neuralNetwork2), render(neuralNetwork3),
                                                render(neuralNetwork4), render(neuralNetwork5));
                                    }
                                });
                            }
                        }

                    } else {
                        MainActivity.this.run(neuralNetwork, prefs);

                        if (!neuralNetwork.isTraining()) {
                            body = Data.check().execute().body();
                            if (body != null){
                                checkData = body.quote;
                                Collections.reverse(checkData);
                                neuralNetwork.join();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        buildChart(render(neuralNetwork));
                                    }
                                });
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (isComplex) {
                    neuralNetwork1.join();
                    neuralNetwork2.join();
                    neuralNetwork3.join();
                    neuralNetwork4.join();
                    neuralNetwork5.join();
                } else neuralNetwork.join();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Computation completed", Toast.LENGTH_LONG).show();
                        view.setClickable(true);
                        view.setBackgroundColor(Color.GREEN);
                    }
                });
            }
        }).start();
    }





    private void buildChart(ArrayList<Double> render) {
        List<Entry> realData = new ArrayList<Entry>();
        List<Entry> nn1 = new ArrayList<Entry>();

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
        real.setColor(Color.BLACK);
        real.setLineWidth(1.5f);
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(real);

        if (render!=null){
            for (Double d: render) {
                nn1.add(new Entry((float)i++, d.floatValue()));
            }

            LineDataSet set1 = new LineDataSet(nn1, "Predictions of "+ neuralNetwork.getName());
            set1.setColor(Color.RED);

            dataSets.add(set1);
        }


        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh
    }


    private void buildChart(ArrayList<Double> render1,ArrayList<Double> render2,ArrayList<Double> render3,ArrayList<Double> render4,ArrayList<Double> render5) {
        List<Entry> realData = new ArrayList<Entry>();
        List<Entry> nn1 = new ArrayList<Entry>();
        List<Entry> nn2 = new ArrayList<Entry>();
        List<Entry> nn3 = new ArrayList<Entry>();
        List<Entry> nn4 = new ArrayList<Entry>();
        List<Entry> nn5 = new ArrayList<Entry>();

        int i=0;
        if (checkData==null||render1==null) {
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
        real.setColor(Color.BLACK);
        real.setLineWidth(1.5f);
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(real);

        if (render1!=null){
            for (Double d: render1) {
                nn1.add(new Entry((float)i++, d.floatValue()));
            }
            i = 0;
            for (Double d: render2) {
                nn2.add(new Entry((float)i++, d.floatValue()));
            }
            i = 0;
            for (Double d: render3) {
                nn3.add(new Entry((float)i++, d.floatValue()));
            }
            i = 0;
            for (Double d: render4) {
                nn4.add(new Entry((float)i++, d.floatValue()));
            }
            i = 0;
            for (Double d: render5) {
                nn5.add(new Entry((float)i++, d.floatValue()));
            }

            LineDataSet set1 = new LineDataSet(nn1, "ultra short");
            set1.setColor(Color.RED);

            LineDataSet set2 = new LineDataSet(nn2, "short");
            set2.setColor(Color.GREEN);

            LineDataSet set3 = new LineDataSet(nn3, "medium");
            set3.setColor(Color.YELLOW);

            LineDataSet set4 = new LineDataSet(nn4, "long");
            set4.setColor(Color.MAGENTA);

            LineDataSet set5 = new LineDataSet(nn5, "ultra long");
            set5.setColor(Color.BLUE);


            dataSets.add(set1);
            dataSets.add(set2);
            dataSets.add(set3);
            dataSets.add(set4);
            dataSets.add(set5);
        }


        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh
    }

    private ArrayList<Double> render(NeuronNet neuronNet){
        ArrayList<Double> res = new ArrayList<>();
        if (!neuronNet.isTraining()&&neuronNet.getTrainingSet().getSetEntries()>0) {
            Map<String, double[]> arr = neuronNet.getRawResults();
            double[] temp = arr.get(prefs.getStockType());
            double last = data.get(data.size() - 1).average();
            for (int i = 0; i < data.size(); i++) {
                res.add(last);
            }
            double next;
            for (int i = 0; i < temp.length; i++) {
                next = last + (last * temp[i]);
                res.add(next);
                last=next;
            }
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
        if (!isComplex||!neuralNetwork.getName().equals("default")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("Save brains");

            String message = "This neural network is trained for " + prefs.getStockType() + " stocks, " + Integer.toString(neuralNetwork.getEpoch()) +
                    " times and has approximate error of "
                    + String.format("%.4f", neuralNetwork.getTotalError() * 100) + "%";
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
                    String name = input.getText().toString();
                    currentBrain.setText(name);
                    getSharedPreferences(Settings.SETTINGS_PREFS, MODE_PRIVATE).edit().putString(Settings.SETTINGS_BRAINS, name).apply();
                    neuralNetwork.store(name, getApplicationContext());
                }
            });
            builder.show();
        }
    }
}
