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
        results = new ArrayList<>();
    }

}
