package com.arnis.neuronnet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arnis.neuronnet.Net.ActivationFunction;
import com.arnis.neuronnet.Net.Brains;
import com.arnis.neuronnet.Net.Mode;
import com.arnis.neuronnet.Net.NeuronNet;
import com.arnis.neuronnet.Other.NetDimen;
import com.arnis.neuronnet.Other.TrainingSet;

public class MainActivity extends AppCompatActivity {


    private NeuronNet neuralNetwork;

    private int totalLayers;// TODO: 23/09/2016 remove
    private int inputNeurons = 2;
    private int outputNeurons = 1;
    private int[] hiddenNeurons = new int[]{2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        totalLayers = 2+hiddenNeurons.length;

        TrainingSet trainingSet = new TrainingSet();
        trainingSet.addEntry(new TrainingSet.Set("00",new double[]{0,0},new double[]{0}));
        trainingSet.addEntry(new TrainingSet.Set("10",new double[]{1,0},new double[]{1}));
        trainingSet.addEntry(new TrainingSet.Set("01",new double[]{0,1},new double[]{1}));
        trainingSet.addEntry(new TrainingSet.Set("11",new double[]{1,1},new double[]{0}));

        NetDimen dimensions = new NetDimen(totalLayers,inputNeurons,outputNeurons,hiddenNeurons);
        Brains brains = new Brains(this);

        NeuronNet.Builder builder = new NeuronNet.Builder(NeuronNet.getNN(NeuronNet.FEEDFORWARD_NN));
        builder.setDimensions(dimensions)
                .addTrainingSet(trainingSet)
                .setTrainingMethod(NeuronNet.BACKPROPAGATION_TRAINING)
                .setErrorCalculation(NeuronNet.MSE)
                .setActivationFunction(ActivationFunction.SIGMOID)
                .setMode(NeuronNet.TRAINING_MODE); // TODO: 23/09/2016 setBrains()
        neuralNetwork = builder.build();

        if (brains.checkCompat(neuralNetwork)){
            brains.loadBrains(neuralNetwork);
            Log.d("happy", "brains fit");
        } else Log.d("happy", "brains do not fit");
//        neuralNetwork.start();
//        brains.saveBrains(neuralNetwork);
        Log.d("happy", "START2-------------------------------------");

        TrainingSet workingSet = new TrainingSet();
        workingSet.addEntry(new TrainingSet.Set("00",new double[]{0,0}));
        workingSet.addEntry(new TrainingSet.Set("01",new double[]{0,1}));
        workingSet.addEntry(new TrainingSet.Set("10",new double[]{1,0}));
        workingSet.addEntry(new TrainingSet.Set("11",new double[]{1,1}));
        neuralNetwork.setMode(NeuronNet.WORKING_MODE);
        neuralNetwork.startWithData(workingSet);
    }
}
