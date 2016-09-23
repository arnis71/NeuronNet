package com.arnis.neuronnet.Neurons;

import java.util.Random;

/**
 * Created by arnis on 04.09.2016.
 */
public class Synapse {
    private static Random rnd = new Random();


    private double weight;
    private double previousWeightChange=0;
    private double gradient;
    private Neural linkedNeuron;


    Synapse(Neural linkedNeuron) {
        this.linkedNeuron = linkedNeuron;
        weight = rnd.nextDouble();
    }

    public void calculateGradient(Neural neuron){
        setGradient(linkedNeuron.getNodeDelta()*neuron.getOutputValue());
    }

    public double getPreviousWeightChange() {
        return previousWeightChange;
    }

    public void setPreviousWeightChange(double previousWeightChange) {
        this.previousWeightChange = previousWeightChange;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public void updateWeight() {
        this.weight += previousWeightChange;
    }

    public double getGradient() {
        return gradient;
    }

    public void setGradient(double gradient) {
        this.gradient = gradient;
    }

    public Neural getLinkedNeuron() {
        return linkedNeuron;
    }

}
