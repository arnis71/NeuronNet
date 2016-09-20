package com.arnis.neuronnet.Neurons;

import com.arnis.neuronnet.Net.ActivationFunction;

import java.util.ArrayList;

/**
 * Created by arnis on 04.09.2016.
 */
public abstract class Neural {
    private double inputValue;
    private double outputValue;
    private double nodeDelta;
    protected ArrayList<Synapse> links;
    private static ActivationFunction activationFunc;

    public static void setActivationFunction(String function) {
        switch (function){
            case ActivationFunction.SIGMOID: activationFunc = new ActivationFunction.Sigmoid();break;
                default: activationFunc =null;
        }
    }
    protected double activationFunction(double x){
        return activationFunc.calculate(x);
    }

    protected double derivativeActivationFunction(double x){
        return activationFunc.calculateDerivative(x);
    }

    public void calculateOut(){
        this.setOutputValue(activationFunction(this.getInputValue()));
    }

    protected void calculateIn(Neural neuron, Synapse synapse){
        this.updateInputValue(neuron.getInputValue()*synapse.getWeight());
    }

    public void calculateNodeDelta(){
        throw new UnsupportedOperationException("Can not calculate node delta");
    }

    public void linkWithLayer(ArrayList<Neural> layer){
        for (Neural neuron: layer) {
            Synapse synapse = new Synapse(this);
            neuron.links.add(synapse);
//            calculateIn(neuron,synapse);
        }
    }

    public ArrayList<Synapse> getLinks() {
        return links;
    }

    public double getInputValue() {
        return inputValue;
    }

    public void setInputValue(double inputValue) {
        this.inputValue = inputValue;
    }

    public void updateInputValue(double inputValue) {
        this.inputValue += inputValue;
    }

    public double getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(double outputValue) {
        this.outputValue = outputValue;
    }

    public double getNodeDelta() {
        return nodeDelta;
    }

    public void setNodeDelta(double nodeDelta) {
        this.nodeDelta = nodeDelta;
    }


}
