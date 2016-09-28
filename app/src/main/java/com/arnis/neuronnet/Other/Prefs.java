package com.arnis.neuronnet.Other;

import com.arnis.neuronnet.Net.NeuronNet;

/**
 * Created by arnis on 25/09/2016.
 */

public class Prefs {
    private String brains;
    private boolean train;
    private boolean complex;
    private int iterations;
    private String type;
    private String stockType;
    private String error;
    private int window;
    private int prediction;

    public Prefs(String stockType, String brains,boolean complexity, boolean train, int iterations, int type, int error,int window,int prediction) {
        this.stockType = stockType;
        complex = complexity;
        this.train = train;
        this.brains = brains;
        this.iterations = iterations;
        this.window = window;
        this.prediction = prediction;
        switch (type){
            case 0: this.type = NeuronNet.FEEDFORWARD_NN;break;
            case 1: this.type = NeuronNet.ELMAN_NN;break;
            case 2: this.type = NeuronNet.JORDAN_NN;break;
            default: this.type = null;break;
        }
        switch (error){
            case 0: this.error = NeuronNet.MSE;break;
            case 1: this.error = NeuronNet.SIMPLE_ERR;break;
            case 2: this.error = NeuronNet.ARCTAN_ERROR;break;
            default: this.error = null;break;
        }
    }
    public String getSymbol(){
        return stockType;
    }
public boolean isComplex(){
    return complex;
}

    public boolean isTrain() {
        return train;
    }
    public String getBrainName() {
        return brains;
    }

    public int getIterations() {
        return iterations;
    }

    public String getType() {
        return type;
    }

    public String getError() {
        return error;
    }

    public int getPrediction() {
        return prediction;
    }


    public void setWindow(int window) {
        this.window = window;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }    public int getWindow() {
        return window;
    }

    public void setName(String brains) {
        this.brains = brains;
    }
}
