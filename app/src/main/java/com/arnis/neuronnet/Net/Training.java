package com.arnis.neuronnet.Net;

import android.util.Log;

/**
 * Created by arnis on 04.09.2016.
 */
public interface Training {
    void train();

    class BackPropagation implements Training{
        private NeuronNet net;

        public BackPropagation(NeuronNet neuronNet) {
            net=neuronNet;
        }

        @Override
        public void train() {
            net.resetErr();
            for (net.getIteration(); net.getIteration() < net.getMaxIterations(); net.iterate()) {
                Log.d("happy", net.getName()+" iteration "+net.getIteration()+" out of "+net.getMaxIterations() + " total epoch "+ net.getEpoch());
                for (int i = 0; i < net.getTrainingSet().getSetEntries(); i++) {
                    net.loadValuesFromSet(i);
                    net.calculateInOut();
                    net.addError(net.getTrainingSet().getEntry(i).getDesiredOutput(),net.getOutput());
//                    net.getInfo();
                    backPropagate();
                }
                net.calculateError(true);
                if (net.getIteration()!=net.getMaxIterations()-1)
                    net.resetErr();
            }
            net.resetIterations();
        }
        private void backPropagate() {
            for (int i = net.neuronLayers.size()-1; i > 0; i--) {
                for (int j = 0; j <= net.neuronLayers.get(i).size()-1; j++) {
                    net.neuronLayers.get(i).get(j).calculateNodeDelta();
                }
                net.calculateGradientsUpdateWeights(net.neuronLayers.get(i-1));
            }
        }
    }
}
