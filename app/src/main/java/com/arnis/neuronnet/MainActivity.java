package com.arnis.neuronnet;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.arnis.neuronnet.Net.ActivationFunction;
import com.arnis.neuronnet.Net.Brains;
import com.arnis.neuronnet.Net.NeuronNet;
import com.arnis.neuronnet.Other.NetDimen;
import com.arnis.neuronnet.Other.TrainingSet;
import com.arnis.neuronnet.Retrofit.Data;
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

public class MainActivity extends AppCompatActivity {


    private NeuronNet neuralNetwork;

    private int totalLayers;// TODO: 23/09/2016 remove
    private int inputNeurons = 3;
    private int outputNeurons = 1;
    private int[] hiddenNeurons = new int[]{3};

    List<Stock> quoteAll;
    List<Stock> quoteTrain;
    List<Stock> quotePredict;
    TrainingSet trainingSet;
    NetDimen dimensions;
    Brains brains;
    Thread neuralThread;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        totalLayers = 2+hiddenNeurons.length;

        lineChart = (LineChart)findViewById(R.id.line_chart);

        dimensions = new NetDimen(totalLayers,inputNeurons,outputNeurons,hiddenNeurons);
        brains = new Brains(this);

        NeuronNet.Builder builder = new NeuronNet.Builder(NeuronNet.getNN(NeuronNet.FEEDFORWARD_NN));
        builder.setDimensions(dimensions)
                .setBias(true)
                .setTrainingMethod(NeuronNet.BACKPROPAGATION_TRAINING)
                .setErrorCalculation(NeuronNet.MSE)
                .setActivationFunction(ActivationFunction.HYPERBOLIC_TANGENT)
                .setMode(NeuronNet.TRAINING_MODE); // TODO: 23/09/2016 setBrains()
        neuralNetwork = builder.build();


        final Thread dataLoad = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    quoteAll = Data.get(0).execute().body().quote;
                    quoteTrain = Data.get(1).execute().body().quote;
                    quotePredict = Data.get(2).execute().body().quote;
                    Log.d("happy", "data received");
//                    Collections.reverse(quoteAll);
                    Collections.reverse(quoteTrain);
                    Collections.reverse(quotePredict);
                } catch (IOException e) {
                    Log.d("happy", "data not received");
                    e.printStackTrace();
                }
            }
        });
        dataLoad.start();


        neuralThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataLoad.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (brains.checkCompat(neuralNetwork)){
                    brains.loadBrains(neuralNetwork);
                    Log.d("happy", "brains fit");
                } else Log.d("happy", "brains do not fit");
                trainingSet = new TrainingSet();
                trainingSet.addEntry(new TrainingSet.Set("",new double[]{0.1,0.1,0.1},new double[]{-0.1}));
                trainingSet.addEntry(new TrainingSet.Set("",new double[]{0.1,0.1,-0.1},new double[]{-0.1}));
                trainingSet.addEntry(new TrainingSet.Set("",new double[]{0.1,-0.1,-0.1},new double[]{0.1}));

//                trainingSet.addTrainStocks(quoteTrain);

                neuralNetwork.setMode(NeuronNet.WORKING_MODE);
//                trainingSet.addWorkStocks(quotePredict);

                neuralNetwork.setTrainingSet(trainingSet);
                neuralNetwork.start();
                brains.saveBrains(neuralNetwork);

                final ArrayList<double[]> arr = neuralNetwork.getResults();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupChart(render(arr));
                    }
                });
            }
        });
        neuralThread.start();
    }

    private void setupChart(ArrayList<Double> render) {
        List<Entry> realData = new ArrayList<Entry>();
        List<Entry> nnData = new ArrayList<Entry>();

        int i=0;
        for (Stock stock: quotePredict) {
            realData.add(new Entry((float)i++, (float)stock.average()));
        }
        i=0;
        for (Double d: render) {
            nnData.add(new Entry((float)i++, d.floatValue()));
        }

        LineDataSet real = new LineDataSet(realData, "Real");
        LineDataSet nn = new LineDataSet(nnData, "Neural net");

        real.setColor(Color.BLUE);
        nn.setColor(Color.RED);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(real);
        dataSets.add(nn);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh
    }

    private ArrayList<Double> render(ArrayList<double[]> arr){
        ArrayList<Double> res = new ArrayList<>();
        boolean offset = true;
        for (int i = 0; i < arr.size(); i++) {
            if (offset){
                res.add(quotePredict.get(i+3).average());
                res.add(quotePredict.get(i+3).average());
                res.add(quotePredict.get(i+3).average());
                res.add(quotePredict.get(i+3).average());
                offset=false;
            }
            res.add(quotePredict.get(i+3).average()+(quotePredict.get(i+3).average()*(arr.get(i)[0])));

        }
        return res;
    }

}
