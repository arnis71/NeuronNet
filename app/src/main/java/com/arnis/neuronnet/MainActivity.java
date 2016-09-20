package com.arnis.neuronnet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arnis.neuronnet.Net.ActivationFunction;
import com.arnis.neuronnet.Net.NeuronNet;
import com.arnis.neuronnet.Other.NetDimen;
import com.arnis.neuronnet.Other.TrainingSet;

public class MainActivity extends AppCompatActivity {

//    private LinearLayout inputNeurons;
//    private EditText input1;
//    private EditText input2;
//    private LinearLayout hiddenNeurons;
//    private LinearLayout outputNeurons;
//    private double inputValue1;
//    private double inputValue2;
//    private double idealOutput;
    private NeuronNet neuralNetwork;
//    private TextView output1;

    private int totalLayers = 3;
    private int inputNeurons = 2;
    private int outputNeurons = 1;
    private int[] hiddenNeurons = new int[]{2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        inputNeurons = (LinearLayout)findViewById(R.id.input_neurons);
//        input1 = (EditText)findViewById(R.id.input_1);
//        input2 = (EditText)findViewById(R.id.input_2);
//        output1 = (TextView)findViewById(R.id.output1);
//        hiddenNeurons = (LinearLayout)findViewById(R.id.hidden_neurons);
//        outputNeurons = (LinearLayout)findViewById(R.id.output_neurons);

        TrainingSet trainingSet = new TrainingSet();
        trainingSet.addEntry(new TrainingSet.Set("00",new double[]{0,0},new double[]{0}));
        trainingSet.addEntry(new TrainingSet.Set("10",new double[]{1,0},new double[]{1}));
        trainingSet.addEntry(new TrainingSet.Set("01",new double[]{0,1},new double[]{1}));
        trainingSet.addEntry(new TrainingSet.Set("11",new double[]{1,1},new double[]{0}));

        NetDimen dimensions = new NetDimen(totalLayers,inputNeurons,outputNeurons,hiddenNeurons);

        NeuronNet.Builder builder = new NeuronNet.Builder(NeuronNet.getNN(NeuronNet.FEEDFORWARD_NN));
        builder.setDimensions(dimensions)
                .addTrainingSet(trainingSet)
                .setTrainingMethod(NeuronNet.BACKPROPAGATION_TRAINING)
                .setErrorCalculation(NeuronNet.MSE)
                .setActivationFunction(ActivationFunction.SIGMOID)
                .setMode(NeuronNet.TRAINING_MODE);
        neuralNetwork = builder.build();
        neuralNetwork.start();
        Log.d("happy", "START2-------------------------------------");

        TrainingSet trainingSet2 = new TrainingSet();
        trainingSet2.addEntry(new TrainingSet.Set("00",new double[]{0,0}));
        trainingSet2.addEntry(new TrainingSet.Set("01",new double[]{0,1}));
        trainingSet2.addEntry(new TrainingSet.Set("10",new double[]{1,0}));
        trainingSet2.addEntry(new TrainingSet.Set("11",new double[]{1,1}));
        neuralNetwork.startWithData(trainingSet2);
    }

//    public void gotoSettings(View view) {
//
//    }
//
//    public void apply(View view){
//        inputValue1 = Double.parseDouble(input1.getText().toString());
//        inputValue2 = Double.parseDouble(input2.getText().toString());
//        if (inputValue1==inputValue2)
//            idealOutput=0;
//        else idealOutput=1;
////        neuralNetwork.changeInputs(inputValue1,inputValue2);
////        neuralNetwork.changeIdeal(idealOutput);
//    }
//
//    public void train(View view) {
//        neuralNetwork.start();
//    }
//
//    public void play(View view) {
////        String out = Double.toString(neuralNetwork.calculateOutputs());
////        output1.setText(out);
//    }
}
