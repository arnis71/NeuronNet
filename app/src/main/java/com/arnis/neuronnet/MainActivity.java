package com.arnis.neuronnet;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arnis.neuronnet.Net.NeuronNet;
import com.arnis.neuronnet.Other.OnCompleteListener;
import com.arnis.neuronnet.Other.ValueChangeListener;
import com.arnis.neuronnet.Retrofit.Data;
import com.arnis.neuronnet.Retrofit.Results;
import com.arnis.neuronnet.Retrofit.Stock;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    private NeuronNet neuralNetwork;
    private LineChart lineChart;
    private List<Stock> data;
    private List<Stock> checkData;
    private EditText startDate;
    private EditText endDate;
    private CheckBox train;

    private TextView error;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = (LineChart)findViewById(R.id.line_chart);
        startDate = (EditText)findViewById(R.id.from);
        endDate = (EditText)findViewById(R.id.to);
        train = (CheckBox)findViewById(R.id.train_nn);
        error = (TextView)findViewById(R.id.error);
        pb = (ProgressBar)findViewById(R.id.pb);

        Settings.USE_BRAINS = true;
        Settings.MAX_EPOCH = 5000;
        Settings.ERROR_CALC = NeuronNet.MSE;
        Settings.NN_TYPE = NeuronNet.FEEDFORWARD_NN;


        train.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    neuralNetwork.setMode(NeuronNet.TRAINING_MODE);
                } else neuralNetwork.setMode(NeuronNet.WORKING_MODE);
            }
        });

        neuralNetwork = NeuronNet.requestStockSolvingNN(getApplicationContext());
        pb.setMax(neuralNetwork.getMaxEpoch());

        neuralNetwork.setEpochListener(new ValueChangeListener() {
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
    public void start(final View view) {
        view.setClickable(false);
        view.setBackgroundColor(Color.RED);
        Data.get("AAPL",startDate.getText().toString(),endDate.getText().toString()).enqueue(new Callback<Results>() {
            @Override
            public void onResponse(Call<Results> call, Response<Results> response) {
                Log.d("happy", "data received");
                data = response.body().quote;
                Collections.reverse(data);
                buildChart(null);

                neuralNetwork.addStockData(data);
                neuralNetwork.loadBrains();
                neuralNetwork.start().addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete() {
                        neuralNetwork.saveBrains();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.setClickable(true);
                                view.setBackgroundColor(Color.GREEN);
                                if (neuralNetwork.isTraining()){
                                    error.setText(String.format("%.6f",neuralNetwork.getTotalError()*100)+"%");
                                    neuralNetwork.resetErr();
                                }
                            }
                        });

                        if (!neuralNetwork.isTraining()){
                            Data.check().enqueue(new Callback<Results>() {
                                @Override
                                public void onResponse(Call<Results> call, Response<Results> response) {
                                    checkData = response.body().quote;
                                    Collections.reverse(checkData);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            buildChart(render(neuralNetwork.getRawResults()));
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Call<Results> call, Throwable t) {

                                }
                            });
                        }

                    }
                });
            }

            @Override
            public void onFailure(Call<Results> call, Throwable t) {
                Log.d("happy", "data not received");
            }
        });
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
        if (!neuralNetwork.isTraining()) {
            double[] temp = arr.get("AAPL");
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
}
