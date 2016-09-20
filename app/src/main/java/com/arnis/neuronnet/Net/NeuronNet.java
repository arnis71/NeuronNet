package com.arnis.neuronnet.Net;

import com.arnis.neuronnet.Neurons.ContextNeurons;
import com.arnis.neuronnet.Neurons.Neural;
import com.arnis.neuronnet.Neurons.OutputNeuron;
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
    protected ArrayList<ArrayList<Neural>> neuronLayers;


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

    protected double calculateError(int predictions,double idealOut,double actualOut){
        return error.calculate(predictions,idealOut,actualOut);
    }

    protected void calculateOutputs(ArrayList<Neural> neurals){
        throw new UnsupportedOperationException("Can not perform calculation");
    }

    protected void calculateNodes(){
        throw new UnsupportedOperationException("Can not perform calculation");
    }

    protected void calculateGradients(ArrayList<Neural> neurals){
        throw new UnsupportedOperationException("Can not perform calculation");
    }
    protected void calculateInOut(){
        throw new UnsupportedOperationException("Can not perform calculation");
    }

    protected void loadValuesFromSet(int set){
        throw new UnsupportedOperationException("Can not load values");
    }
    protected void changeInputs(double... inputs){
        throw new UnsupportedOperationException("Can not perform operation");
    }
    protected void changeOutputs(double... outputs){
        throw new UnsupportedOperationException("Can not perform operation");
    }
    public abstract void start();
    public void startWithData(TrainingSet trainingSet){
        throw new UnsupportedOperationException("Can not perform operation");
    }

    public void getInfo(){
        throw new UnsupportedOperationException("Can not perform operation");
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


    public static class Builder{
        private NeuronNet net;
        private NetDimen dimensions;
        private ContextNeurons contextNeurons;

        public Builder(NeuronNet net) {
            this.net = net;
            this.net.error = new Error.MeanSquaredError();
            this.net.training = new Training.BackPropagation(this.net);
            this.net.setActivFunc(ActivationFunction.SIGMOID);
            this.net.setMode(WORKING_MODE);
        }

        public Builder setDimensions(NetDimen dimensions){
            this.dimensions = dimensions;
            return this;
        }
        public Builder addTrainingSet(TrainingSet trainingSet){
            this.net.setTrainingSet(trainingSet);
            return this;
        }
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


        private void addLayers(){
            for (int i = 0; i < dimensions.getTotalLayers() ; i++)
                net.neuronLayers.add(new ArrayList<Neural>());
        }

        private void addInputNeurons() {
            ArrayList<Neural> inputLayer = net.neuronLayers.get(0);
            for (int i = 0; i < dimensions.getInputNeurons(); i++){
                inputLayer.add(NeuroFactory.getNeuron(NeuroFactory.INPUT_NEURON));
                inputLayer.get(i).setInputValue(net.getTrainingSet().getEntry(0).getInputValue(i));
            }
            addBiasNeuron(0);
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
            }
        }

        private void addOutputNeurons() {
            ArrayList<Neural> outputLayer = net.neuronLayers.get(dimensions.getTotalLayers()-1);
            for (int i = 0; i < dimensions.getOutputNeurons(); i++){
                outputLayer.add(NeuroFactory.getNeuron(NeuroFactory.OUTPUT_NEURON));
                outputLayer.get(i).linkWithLayer(net.neuronLayers.get(dimensions.getTotalLayers()-2));
                if (net.isTraining())
                    ((OutputNeuron)outputLayer.get(i)).setIdealOutputValue(net.getTrainingSet().getEntry(0).getOutputValue(i));
            }
        }

        private void addBiasNeuron(int i) {
            net.neuronLayers.get(i).add(NeuroFactory.getNeuron(NeuroFactory.BIAS_NEURON));
        }

        private void addContextNeurons(){
            if (net instanceof ElmanNN)
                contextNeurons = new ContextNeurons.ElmanNet();
            else if (net instanceof JordanNN)
                contextNeurons = new ContextNeurons.JordanNet();

            contextNeurons.addContextNeurons();
        }

        public NeuronNet build(){
            addLayers();
            addInputNeurons();
            addHiddenNeurons();
            addOutputNeurons();

            if (!(net instanceof FeedForwardNN))
                addContextNeurons();
            return net;
        }
    }
    }


