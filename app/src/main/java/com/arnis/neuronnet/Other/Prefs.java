package com.arnis.neuronnet.Other;

import com.arnis.neuronnet.Net.NeuronNet;

/**
 * Created by arnis on 25/09/2016.
 */

public class Prefs {
    private String brains;
    private boolean train;
    private int epoch;
    private String type;
    private String stockType;
    private String error;

    public Prefs(String stockType,String brains,boolean train, int epoch, int type, int error) {
        this.stockType = stockType;
        this.train = train;
        this.brains = brains;
        this.epoch = epoch;
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
    public String getStockType(){
        return stockType;
    }

    public boolean isTrain() {
        return train;
    }
    public String getBrainName() {
        return brains;
    }

    public int getEpoch() {
        return epoch;
    }

    public String getType() {
        return type;
    }

    public String getError() {
        return error;
    }
}