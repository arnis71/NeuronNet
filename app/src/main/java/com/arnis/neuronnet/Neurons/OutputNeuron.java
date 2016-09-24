package com.arnis.neuronnet.Neurons;

import com.arnis.neuronnet.Net.NeuronNet;

import java.util.ArrayList;

/**
 * Created by arnis on 04.09.2016.
 */
public class OutputNeuron extends Neural {

    private double idealOutputValue=-100;

    public double getIdealOutputValue() {
        return idealOutputValue;
    }

    public void setIdealOutputValue(double idealOutputValue) {
        this.idealOutputValue = idealOutputValue;
    }

    @Override
    public void calculateNodeDelta() {
        setNodeDelta((getIdealOutputValue()-getOutputValue())* derivativeActivationFunction(getOutputValue()));
    }
}
