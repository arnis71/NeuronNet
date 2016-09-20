package com.arnis.neuronnet.Net;

import android.util.Log;

import com.arnis.neuronnet.Neurons.Neural;
import com.arnis.neuronnet.Neurons.Synapse;

import java.util.ArrayList;

/**
 * Created by arnis on 04.09.2016.
 */
public interface Training {
    public final double learningRate = 0.2;
    public final double momentum = 0.3;
    void train();

    class BackPropagation implements Training{
        private NeuronNet net;

        public BackPropagation(NeuronNet neuronNet) {
            net=neuronNet;
        }

        @Override
        public void train() {
            for (net.getEpoch(); net.getEpoch() < net.getMaxEpoch(); net.incEpoch()) {
//                Log.d("happy", "epoch "+net.getEpoch()+" out of "+net.getMaxEpoch());
                for (int i = 0; i < net.getTrainingSet().getSetEntries(); i++) {
                    net.loadValuesFromSet(i);
                    net.calculateInOut();
//                    net.getInfo();
                    backPropagate();
                }
            }
        }
        private void backPropagate() {
            for (int i = net.neuronLayers.size()-1; i > 0; i--) {
                for (int j = 0; j <= net.neuronLayers.get(i).size()-1; j++) {
                    net.neuronLayers.get(i).get(j).calculateNodeDelta();
//                    if (i<=net.neuronLayers.size()-2){
//                        Neural neuron = net.neuronLayers.get(i).get(j);
//                        ArrayList<Synapse> synapses = neuron.getLinks();
//                        for (Synapse synapse:synapses){
//                            synapse.calculateGradient(neuron);
//                            synapse.setPreviousWeightChange((learningRate*synapse.getGradient()+(synapse.getPreviousWeightChange()*momentum)));
//                            synapse.updateWeight();
//                        }
//                    }
                }
                net.calculateGradients(net.neuronLayers.get(i-1));// rename does weight adjust too
            }
//
//
//
//
//            for (int i = 0; i < net.neuronLayers.size()-1; i++) {
//                for (int j = 0; j < net.neuronLayers.get(i).size(); j++) {
//                    Neural neuron = net.neuronLayers.get(i).get(j);
//                    ArrayList<Synapse> synapses = neuron.getLinks();
//                    for (Synapse synapse:synapses){
//                        synapse.setPreviousWeightChange((learningRate*synapse.getGradient()+(synapse.getPreviousWeightChange()*momentum)));
//                        synapse.updateWeight();
//                    }
//                }
//            }
        }
    }
}
