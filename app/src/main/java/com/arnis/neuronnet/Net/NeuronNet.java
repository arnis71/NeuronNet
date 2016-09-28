package com.arnis.neuronnet.Net;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.arnis.neuronnet.MainActivity;
import com.arnis.neuronnet.StockPrediction;
import com.arnis.neuronnet.Neurons.InputNeuron;
import com.arnis.neuronnet.Neurons.Neural;
import com.arnis.neuronnet.Neurons.OutputNeuron;
import com.arnis.neuronnet.Neurons.Synapse;
import com.arnis.neuronnet.Other.NetDimen;
import com.arnis.neuronnet.Other.OnCompleteListener;
import com.arnis.neuronnet.Other.Prefs;
import com.arnis.neuronnet.Other.TrainingSet;
import com.arnis.neuronnet.Other.ValueChangeListener;
import com.arnis.neuronnet.Retrofit.Currency;
import com.arnis.neuronnet.Retrofit.Stock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arnis on 04.09.2016.
 */
public abstract class NeuronNet {
    public static final String FEEDFORWARD_NN = "feedforward";
    public static final String ELMAN_NN = "elman";
    public static final String JORDAN_NN = "jordan";
    public static final String BACKPROPAGATION_TRAINING = "backpropagation";
    public static final String MSE = "mse";
    public static final String SIMPLE_ERR = "simple";
    public static final String ARCTAN_ERROR = "arctan";
    public static final String TRAINING_MODE = "training";
    public static final String VALIDATION_MODE = "validation";
    public static final String WORKING_MODE = "working";
    public static final String STOCK_PREDICT = "stocks";

    private static double learningRate = 0.0001;
    private static double momentum = 0.9;

    private String name;
    private Brains brains;
    private Mode mode;
    private Training training;
    private TrainingSet trainingSet;
    private Error error;
    private Thread neuralThread;
    private int currentTrainingSet;
    private int epoch;
    private int iteration;
    private int maxIterations;
    private double err;
    ArrayList<ArrayList<Neural>> neuronLayers;

    private ValueChangeListener iterationListener;

    public void setIterationListener(ValueChangeListener listener){
        iterationListener = listener;
    }

    void setName(String name){
        if (name.equals("no brains"))
            this.name = "default";
        else this.name = name;
    }
    public String getName(){
        return name;
    }

    public static NeuronNet getNN(String type){
        switch (type){
            case FEEDFORWARD_NN: return new FeedForwardNN();
            case ELMAN_NN: return new ElmanNN();
            case JORDAN_NN:return new JordanNN();
            default: return null;
        }
    }
    public static double getMomentum() {
        return momentum;
    }

    public static void setMomentum(double value) {
        momentum = value;
    }

    public static void setLearningRate(double value){
        learningRate=value;
    }
    public static double getLearningRate(){
        return learningRate;
    }

    private void setErrorCalc(String type){
        switch (type){
            case MSE: error = new Error.MeanSquaredError();break;
            case ARCTAN_ERROR: error = new Error.ArctanError();break;
            case SIMPLE_ERR: error = new Error.SimpleError();break;
            default:throw new IllegalArgumentException("No such error calculation: "+type);
        }
    }

    public void join(){
        try {
            if (neuralThread!=null)
                neuralThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static NeuronNet requestStockSolvingNN(Context context, Prefs prefs){

        int[] hiddenNeurons = new int[]{3};

        NetDimen dimensions = new NetDimen(prefs.getWindow(),prefs.getPrediction(),hiddenNeurons);

        NeuronNet.Builder builder = new NeuronNet.Builder(NeuronNet.getNN(prefs.getType()));
        builder.setDimensions(dimensions)
                .setBias(true)
                .setErrorCalculation(prefs.getError())
                .setActivationFunction(ActivationFunction.HYPERBOLIC_TANGENT)
                .addBrains(context);

        NeuronNet net = builder.build();
        net.setMaxIterations(prefs.getIterations());

        net.epoch = context.getSharedPreferences(prefs.getBrainName()+"_info",Context.MODE_PRIVATE).getInt("epoch",0);
        net.err = Double.parseDouble(context.getSharedPreferences(prefs.getBrainName()+"_info",Context.MODE_PRIVATE).getString("error","0"));

        if (prefs.isTrain())
            net.setMode(TRAINING_MODE);
        else net.setMode(WORKING_MODE);

        net.loadBrains(prefs.getBrainName());

        net.setName(prefs.getBrainName());

        return net;
    }

    public void addStockData(List<Stock> data){
        TrainingSet trainingSet = new TrainingSet();

        if (isTraining())
            trainingSet.addTrainStocks(data,neuronLayers.get(0).size()-1,neuronLayers.get(neuronLayers.size()-1).size());
        else trainingSet.addWorkStocks(data,neuronLayers.get(0).size()-1);

        setTrainingSet(trainingSet);
    }
    public void addCurrencyData(List<Currency> data){
        TrainingSet trainingSet = new TrainingSet();

        if (isTraining())
            trainingSet.addTrainCurrency(data,neuronLayers.get(0).size()-1,neuronLayers.get(neuronLayers.size()-1).size());
        else trainingSet.addWorkCurrency(data,neuronLayers.get(0).size()-1);

        setTrainingSet(trainingSet);
    }

    void store(Context context){
        if (!name.equals("default")) {
            SharedPreferences.Editor editor = context.getSharedPreferences(name + "_info", Context.MODE_PRIVATE).edit();
            editor.putInt("epoch", getEpoch());
            editor.putString("error", Double.toString(getTotalError())).apply();
            brains.saveBrains(name, this);
            editor = context.getSharedPreferences(MainActivity.BRAINS_STORAGE, Context.MODE_PRIVATE).edit();
            editor.putString(name, name).apply();
        }
    }

    private void loadBrains(String name){
            if (brains.checkCompat(name,this)) {
                Log.d("happy", "brains fit");
                try {
                    brains.loadBrains(name,this);
                } catch (IndexOutOfBoundsException e) {
                    Log.d("happy", "brains damaged");
                }
            } else Log.d("happy", "brains do not fit");
    }

    private void setTrainingMode(String training){
        switch (training){
            case BACKPROPAGATION_TRAINING: this.training= new Training.BackPropagation(this);break;
            default: this.training = null;
        }
    }

    private void setActivFunc(String activFunc){
        Neural.setActivationFunction(activFunc);
    }

    void train(){
        training.train();
    }

    void addError(double[] idealOut, double[] actualOut){
        if (error instanceof Error.MeanSquaredError)
            err +=  ((Error.MeanSquaredError)error).squareError(idealOut,actualOut);
        else if (error instanceof Error.SimpleError){
            err +=  ((Error.SimpleError)error).absError(idealOut,actualOut);
        }
    }
    void calculateError(boolean print){
        err =  error.calculate(trainingSet.getSetEntries(),err);
        if (print)
            Log.d("happy", "ERROR: " + String.format("%.6f",this.err*100)+"%");
    }
    void resetErr(){
        err=0;
    }
    public double getTotalError(){
        return err;
    }

    private void calculateOutputs(ArrayList<Neural> neurals){
        for (Neural neuron:neurals){
            neuron.calculateOut();
        }
    }

    void calculateGradientsUpdateWeights(ArrayList<Neural> neurals){
        for (Neural neuron:neurals) {
            ArrayList<Synapse> synapses = neuron.getLinks();
            for (Synapse synapse : synapses) {
                synapse.calculateGradient(neuron);
                synapse.setPreviousWeightChange((NeuronNet.getLearningRate() * synapse.getGradient() + (synapse.getPreviousWeightChange() * NeuronNet.getMomentum())));
                synapse.updateWeight();
            }
        }
    }
    void calculateInOut(){
        boolean makeNull;
        for (int i = 0; i < neuronLayers.size()-1; i++) {
            makeNull=true;
            for (int j = 0; j < neuronLayers.get(i).size(); j++) {
                Neural neuron = neuronLayers.get(i).get(j);
                ArrayList<Synapse> synapses = neuron.getLinks();
                for (Synapse synapse:synapses){
                    if (makeNull)
                        synapse.getLinkedNeuron().setInputValue(0);
                    synapse.getLinkedNeuron().updateInputValue(synapse.getWeight()*neuron.getOutputValue());
                }
                makeNull=false;
            }
            if (i!=neuronLayers.size()-1)
                calculateOutputs(neuronLayers.get(i+1));
        }
    }

    void loadValuesFromSet(int set){
        changeInputs(getTrainingSet().getEntry(set).getInputValues());
        if (getMode() instanceof Mode.Learning)
            changeIdealOutputs(getTrainingSet().getEntry(set).getDesiredOutput());
    }
    private void changeInputs(double... inputs){
        for (int i = 0; i < inputs.length; i++) {
            neuronLayers.get(0).get(i).setInputValue(inputs[i]);
        }
    }
    private void changeIdealOutputs(double... outputs){
        for (int i = 0; i < outputs.length; i++) {
            ((OutputNeuron)neuronLayers.get(neuronLayers.size()-1).get(i)).setIdealOutputValue(outputs[i]);
        }
    }
    public NeuronNet startWithListener(OnCompleteListener listener){
        final NeuronNet copy = this;
        mOnCompleteListener = listener;
        if (getTrainingSet().getSetEntries()>0) {
            neuralThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getMode().start(copy);
                    brains.saveBrains(name, copy);
                    if (mOnCompleteListener!=null)
                        mOnCompleteListener.onComplete();
                }
            });
            neuralThread.start();
        } else if (mOnCompleteListener!=null)
            mOnCompleteListener.onComplete();
        return this;
    };

    private OnCompleteListener mOnCompleteListener;

    void getInfo(){
        for (int i = 0; i < neuronLayers.get(0).size()-1; i++) {
            Neural neural =neuronLayers.get(0).get(i);
            if (neural instanceof InputNeuron)
                Log.d("happy", "FOR INPUT: " + neural.getInputValue());
        }
        for (int i = 0; i < neuronLayers.get(neuronLayers.size()-1).size(); i++) {
//            Log.d("happy","IDEAL_OUTPUT: "+ ((OutputNeuron)neuronLayers.get(neuronLayers.size()-1).get(i)).getIdealOutputValue());
            Log.d("happy","OUTPUT: "+ Double.toString(neuronLayers.get(neuronLayers.size()-1).get(i).getOutputValue()));
        }
        Log.d("happy", "-----------------------------------");
    }

    public boolean isTraining() {
        return training!=null;
    }

    public int getEpoch() {
        return epoch;
    }

    void setIteration(int iteration) {
        this.iteration = iteration;
        if (iterationListener !=null)
            iterationListener.onValueChange(iteration);
    }

    void iterate() {
        iteration++;
        this.epoch++;
        if (iterationListener!=null){
            iterationListener.onValueChange(iteration);
        }
    }

    void resetIterations(){
        iteration=0;
    }


    public int getMaxIterations() {
        return maxIterations;
    }

    void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }
    int getIteration(){
        return iteration;
    }

    public int getCurrentTrainingSet() {
        return currentTrainingSet;
    }

    void setCurrentTrainingSet(int currentTrainingSet) {
        this.currentTrainingSet = currentTrainingSet;
    }

    public TrainingSet getTrainingSet() {
        return trainingSet;
    }

    public void setTrainingSet(TrainingSet trainingSet) {
        this.trainingSet = trainingSet;
    }

    public double[] getOutput(){
        double[] out = new double[neuronLayers.get(neuronLayers.size() - 1).size()];
        for (int i = 0; i < this.neuronLayers.get(neuronLayers.size() - 1).size(); i++) {
            out[i]=neuronLayers.get(neuronLayers.size()-1).get(i).getOutputValue();
        }
        return out;
    }


    public Mode getMode() {
        return mode;
    }

    public void setMode(String type) {
        switch (type){
            case TRAINING_MODE: mode = new Mode.Learning(); this.setTrainingMode(BACKPROPAGATION_TRAINING);break;
            case VALIDATION_MODE: mode = new Mode.Validation();break;
            case WORKING_MODE: mode = new Mode.Working(); this.setTrainingMode("");break;
        }
    }


    public static class Builder{
        private NeuronNet net;
        private NetDimen dimensions;
        private boolean withBias;

        public Builder(NeuronNet net) {
            this.net = net;
            this.net.error = new Error.MeanSquaredError();
            this.net.setActivFunc(ActivationFunction.SIGMOID);
            this.net.setMode(WORKING_MODE);
            withBias=true;
        }

        public Builder setDimensions(NetDimen dimensions){
            this.dimensions = dimensions;
            return this;
        }
        public Builder setErrorCalculation(String type) {
            this.net.setErrorCalc(type);
            return this;
        }
        public Builder setActivationFunction(String type){
            this.net.setActivFunc(type);
            return this;
        }
        public Builder setMode(String type){
            this.net.setMode(type);
            return this;
        }
        public Builder setBias(boolean withBias){
            this.withBias = withBias;
            return this;
        }
        public Builder addBrains(Context context){
            this.net.brains = new Brains(context);
            return this;
        }


        private void addLayers(){
            for (int i = 0; i < dimensions.getTotalLayers() ; i++)
                net.neuronLayers.add(new ArrayList<Neural>());
        }

        private void addInputNeurons() {
            ArrayList<Neural> inputLayer = net.neuronLayers.get(0);
            for (int i = 0; i < dimensions.getInputNeurons(); i++){
                inputLayer.add(NeuroFactory.getNeuron(NeuroFactory.INPUT_NEURON));
            }
            addBiasNeuron(0);
            addContextNeurons(inputLayer,dimensions.getHiddenLayersNeuron(0));

        }

        private void addContextNeurons(ArrayList<Neural> layer, int amount) {
            if (net instanceof ElmanNN)
                for (int i = 0; i < amount; i++) {
                    layer.add(NeuroFactory.getNeuron(NeuroFactory.CONTEXT_NEURON));
                }
        }

        private void addHiddenNeurons() {
            int k=0;
            for (int i = 1; i <=dimensions.getTotalLayers()-2; i++) {
                for (int j = 0; j < dimensions.getHiddenLayersNeuron(k); j++) {
                    net.neuronLayers.get(i).add(NeuroFactory.getNeuron(NeuroFactory.HIDDEN_NEURON));
                    net.neuronLayers.get(i).get(j).linkWithLayer(net.neuronLayers.get(i - 1));
                }
                k++;
                addBiasNeuron(i);
                if (i<=dimensions.getTotalLayers()-3)
                    addContextNeurons(net.neuronLayers.get(i),dimensions.getHiddenLayersNeuron(i));
            }
        }

        private void addOutputNeurons() {
            ArrayList<Neural> outputLayer = net.neuronLayers.get(dimensions.getTotalLayers()-1);
            for (int i = 0; i < dimensions.getOutputNeurons(); i++){
                outputLayer.add(NeuroFactory.getNeuron(NeuroFactory.OUTPUT_NEURON));
                outputLayer.get(i).linkWithLayer(net.neuronLayers.get(dimensions.getTotalLayers()-2));
            }
        }

        private void addBiasNeuron(int i) {
            if (withBias)
                net.neuronLayers.get(i).add(NeuroFactory.getNeuron(NeuroFactory.BIAS_NEURON));
        }

        public NeuronNet build(){
            addLayers();
            addInputNeurons();
            addHiddenNeurons();
            addOutputNeurons();
            return net;
        }
    }
    }


