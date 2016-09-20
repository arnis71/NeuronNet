package com.arnis.neuronnet.Net;

import android.util.Log;

import com.arnis.neuronnet.Neurons.Neural;
import com.arnis.neuronnet.Neurons.OutputNeuron;
import com.arnis.neuronnet.Neurons.Synapse;
import com.arnis.neuronnet.Other.TrainingSet;

import java.util.ArrayList;

/**
 * Created by arnis on 04.09.2016.
 */
public class FeedForwardNN extends NeuronNet {

    protected FeedForwardNN() {
        setEpoch(0);
        setMaxEpoch(50000);
        setCurrentTrainingSet(0);
        neuronLayers = new ArrayList<>();
    }

    @Override
    public void start() {
        getMode().start(this);
    }

    @Override
    public void startWithData(TrainingSet trainingSet) {
        setTrainingSet(trainingSet);
        setMode(NeuronNet.WORKING_MODE);
        getMode().start(this);
    }

    @Override
    public void getInfo() {
        for (int i = 0; i < neuronLayers.get(0).size()-1; i++) {
            Log.d("happy", "FOR INPUT: " + neuronLayers.get(0).get(i).getInputValue());
        }
        for (int i = 0; i < neuronLayers.get(neuronLayers.size()-1).size(); i++) {
            Log.d("happy","ACTUAL_OUTPUT: "+ neuronLayers.get(neuronLayers.size()-1).get(i).getOutputValue());
            Log.d("happy","IDEAL_OUTPUT: "+ ((OutputNeuron)neuronLayers.get(neuronLayers.size()-1).get(i)).getIdealOutputValue());
//            Log.d("happy", "ERROR: "+calculateError(neuronLayers.get(neuronLayers.size()-1).size(),
//                    ((OutputNeuron)neuronLayers.get(neuronLayers.size()-1).get(i)).getIdealOutputValue(),
//                    neuronLayers.get(neuronLayers.size()-1).get(i).getOutputValue()));
        }
        Log.d("happy", "-----------------------------------");
    }

    @Override
    protected void loadValuesFromSet(int set) {
        changeInputs(getTrainingSet().getEntry(set).getInputValues());
        if (getMode() instanceof Mode.Learning)
            changeOutputs(getTrainingSet().getEntry(set).getDesiredOutput());
    }

    @Override
    protected void changeInputs(double... inputs) {
        for (int i = 0; i < inputs.length; i++) {
            neuronLayers.get(0).get(i).setInputValue(inputs[i]);
        }
    }

    @Override
    protected void changeOutputs(double... outputs) {
        for (int i = 0; i < outputs.length; i++) {
            ((OutputNeuron)neuronLayers.get(neuronLayers.size()-1).get(i)).setIdealOutputValue(outputs[i]);
        }
    }

//    @Override
//    public void calculateNodes() {
//        for (int i = neuronLayers.size()-1; i > 0; i--) {
//            for (int j = 0; j <= neuronLayers.get(i).size()-1; j++) {
//                neuronLayers.get(i).get(j).calculateNodeDelta();
//            }
//        }
//    }

    @Override
    protected void calculateOutputs(ArrayList<Neural> neurals) {
        for (Neural neuron:neurals){
            neuron.calculateOut();
        }
    }

    @Override
    public void calculateInOut() {
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

    @Override
    public void calculateGradients(ArrayList<Neural> neurals) {
        for (Neural neuron:neurals) {
            ArrayList<Synapse> synapses = neuron.getLinks();
            for (Synapse synapse : synapses) {
                synapse.calculateGradient(neuron);
                synapse.setPreviousWeightChange((Training.learningRate * synapse.getGradient() + (synapse.getPreviousWeightChange() * Training.momentum)));
                synapse.updateWeight();
            }
        }
    }
}
