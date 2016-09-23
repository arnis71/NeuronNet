package com.arnis.neuronnet.Net;

/**
 * Created by arnis on 04.09.2016.
 */
public interface Training {
    public final double learningRate = 0.9;
    public final double momentum = 0.7;
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
                    net.addSquaredError(net.getTrainingSet().getEntry(i).getDesiredOutput(),net.getOutput());
                    net.getInfo();
                    backPropagate();
                }
                net.calculateError(true);
            }
        }
        private void backPropagate() {
            for (int i = net.neuronLayers.size()-1; i > 0; i--) {
                for (int j = 0; j <= net.neuronLayers.get(i).size()-1; j++) {
                    net.neuronLayers.get(i).get(j).calculateNodeDelta();
                }
                net.calculateGradientsUpdateWeights(net.neuronLayers.get(i-1));// rename does weight adjust too
            }
        }
    }
}
