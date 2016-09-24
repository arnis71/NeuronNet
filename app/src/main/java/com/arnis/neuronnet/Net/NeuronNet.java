package com.arnis.neuronnet.Net;

import android.util.Log;

import com.arnis.neuronnet.Neurons.InputNeuron;
import com.arnis.neuronnet.Neurons.Neural;
import com.arnis.neuronnet.Neurons.OutputNeuron;
import com.arnis.neuronnet.Neurons.Synapse;
import com.arnis.neuronnet.Other.NetDimen;
import com.arnis.neuronnet.Other.TrainingSet;

import java.util.ArrayList;

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

    private Mode mode;
    private Training training;
    private TrainingSet trainingSet;
    private int currentTrainingSet;
    private int epoch;
    private int maxEpoch;
    private Error error;
    private double err=0;
    ArrayList<ArrayList<Neural>> neuronLayers;
    ArrayList<double[]> results;


    public static NeuronNet getNN(String type){
        switch (type){
            case FEEDFORWARD_NN: return new FeedForwardNN();
            case ELMAN_NN: return new ElmanNN();
            case JORDAN_NN:return new JordanNN();
            default: return null;
        }
    }

    protected void setErrorCalc(String type){
        switch (type){
            case MSE: error = new Error.MeanSquaredError();break;
            case ARCTAN_ERROR: error = new Error.ArctanError();break;
            case SIMPLE_ERR: error = new Error.SimpleError();break;
            default:throw new IllegalArgumentException("No such error calculation: "+type);
        }
    }

    protected void setTrainingMode(String training){
        switch (training){
            case BACKPROPAGATION_TRAINING: this.training= new Training.BackPropagation(this);break;
            default:throw new IllegalArgumentException("No such trainig method: "+training);
        }
    }

    private void setActivFunc(String activFunc){
        Neural.setActivationFunction(activFunc);
    }

    protected void train(){
        training.train();
    }

    protected void addError(double[] idealOut, double[] actualOut){
        if (error instanceof Error.MeanSquaredError)
            err +=  ((Error.MeanSquaredError)error).squareError(idealOut,actualOut);
        else if (error instanceof Error.SimpleError){
            err +=  ((Error.SimpleError)error).absError(idealOut,actualOut);
        }
    }
    protected void calculateError(boolean print){
        err =  error.calculate(trainingSet.getSetEntries(),err);
        if (print)
            Log.d("happy", "ERROR: " + String.format("%.6f",this.err*100)+"%");
        err=0;
    }

    protected void calculateOutputs(ArrayList<Neural> neurals){
        for (Neural neuron:neurals){
            neuron.calculateOut();
        }
    }

    protected void calculateNodes(){
        throw new UnsupportedOperationException("Can not perform calculation");
    }

    protected void calculateGradientsUpdateWeights(ArrayList<Neural> neurals){
        for (Neural neuron:neurals) {
            ArrayList<Synapse> synapses = neuron.getLinks();
            for (Synapse synapse : synapses) {
                synapse.calculateGradient(neuron);
                synapse.setPreviousWeightChange((Training.learningRate * synapse.getGradient() + (synapse.getPreviousWeightChange() * Training.momentum)));
                synapse.updateWeight();
            }
        }
    }
    protected void calculateInOut(){
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

    protected void loadValuesFromSet(int set){
        changeInputs(getTrainingSet().getEntry(set).getInputValues());
        if (getMode() instanceof Mode.Learning)
            changeIdealOutputs(getTrainingSet().getEntry(set).getDesiredOutput());
    }
    protected void changeInputs(double... inputs){
        for (int i = 0; i < inputs.length; i++) {
            neuronLayers.get(0).get(i).setInputValue(inputs[i]);
        }
    }
    protected void changeIdealOutputs(double... outputs){
        for (int i = 0; i < outputs.length; i++) {
            ((OutputNeuron)neuronLayers.get(neuronLayers.size()-1).get(i)).setIdealOutputValue(outputs[i]);
        }
    }
    public void start(){
        getMode().start(this);
    };

    public void getInfo(){
        for (int i = 0; i < neuronLayers.get(0).size()-1; i++) {
            Neural neural =neuronLayers.get(0).get(i);
            if (neural instanceof InputNeuron)
                Log.d("happy", "FOR INPUT: " + neural.getInputValue());
        }
        for (int i = 0; i < neuronLayers.get(neuronLayers.size()-1).size(); i++) {
            Log.d("happy","IDEAL_OUTPUT: "+ ((OutputNeuron)neuronLayers.get(neuronLayers.size()-1).get(i)).getIdealOutputValue());
            Log.d("happy","ACTUAL_OUTPUT: "+ String.format("%.4f",neuronLayers.get(neuronLayers.size()-1).get(i).getOutputValue()));
        }
        Log.d("happy", "-----------------------------------");
    }

    public boolean isTraining() {
        return training!=null;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public void incEpoch() {
        this.epoch++;
    }


    public int getMaxEpoch() {
        return maxEpoch;
    }

    public void setMaxEpoch(int maxEpoch) {
        this.maxEpoch = maxEpoch;
    }

    public int getCurrentTrainingSet() {
        return currentTrainingSet;
    }

    public void setCurrentTrainingSet(int currentTrainingSet) {
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
            case TRAINING_MODE: mode = new Mode.Learning();break;
            case VALIDATION_MODE: mode = new Mode.Validation();break;
            case WORKING_MODE: mode = new Mode.Working();break;
        }
    }

    public ArrayList<double[]> getResults() {
        return results;
    }

    public void addResults(double[] res, String description) {
        this.results.add(res);
    }


    public static class Builder{
        private NeuronNet net;
        private NetDimen dimensions;
        private boolean withBias;

        public Builder(NeuronNet net) {
            this.net = net;
            this.net.error = new Error.MeanSquaredError();
            this.net.training = new Training.BackPropagation(this.net);
            this.net.setActivFunc(ActivationFunction.SIGMOID);
            this.net.setMode(WORKING_MODE);
            withBias=true;
        }

        public Builder setDimensions(NetDimen dimensions){
            this.dimensions = dimensions;
            return this;
        }
//        public Builder addTrainingSet(TrainingSet trainingSet){
//            this.net.setTrainingSet(trainingSet);
//            return this;
//        }
        public Builder setTrainingMethod(String type) {
            this.net.setTrainingMode(type);
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


        private void addLayers(){
            for (int i = 0; i < dimensions.getTotalLayers() ; i++)
                net.neuronLayers.add(new ArrayList<Neural>());
        }

        private void addInputNeurons() {
            ArrayList<Neural> inputLayer = net.neuronLayers.get(0);
            for (int i = 0; i < dimensions.getInputNeurons(); i++){
                inputLayer.add(NeuroFactory.getNeuron(NeuroFactory.INPUT_NEURON));
//                inputLayer.get(i).setInputValue(net.getTrainingSet().getEntry(0).getInputValue(i));
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
//                if (net.isTraining())
//                    ((OutputNeuron)outputLayer.get(i)).setIdealOutputValue(net.getTrainingSet().getEntry(0).getOutputValue(i));
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


