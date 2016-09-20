package com.arnis.neuronnet.Other;

/**
 * Created by arnis on 05.09.2016.
 */
public class NetDimen {
    public int getTotalLayers() {
        return totalLayers;
    }

    public void setTotalLayers(int totalLayers) {
        this.totalLayers = totalLayers;
    }

    public int getInputNeurons() {
        return inputNeurons;
    }

    public void setInputNeurons(int inputNeurons) {
        this.inputNeurons = inputNeurons;
    }

    public int getOutputNeurons() {
        return outputNeurons;
    }

    public void setOutputNeurons(int outputNeurons) {
        this.outputNeurons = outputNeurons;
    }

    public int getHiddenLayersNeuron(int index) {
        return hiddenLayersNeurons[index];
    }

    private int totalLayers;
    private int inputNeurons;
    private int outputNeurons;
    private int[] hiddenLayersNeurons;

    public NetDimen(int totalLayers, int inputNeurons, int outputNeurons, int[] hiddenLayersNeurons) {
        this.totalLayers = totalLayers;
        this.inputNeurons = inputNeurons;
        this.outputNeurons = outputNeurons;
        this.hiddenLayersNeurons = hiddenLayersNeurons;
    }
}
