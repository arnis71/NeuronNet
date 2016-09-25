package com.arnis.neuronnet.Net;

import android.util.Log;

import com.arnis.neuronnet.Neurons.Neural;
import com.arnis.neuronnet.Neurons.OutputNeuron;
import com.arnis.neuronnet.Neurons.Synapse;
import com.arnis.neuronnet.Other.TrainingSet;
import com.arnis.neuronnet.Settings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arnis on 04.09.2016.
 */
public class FeedForwardNN extends NeuronNet {

    protected FeedForwardNN() {
        setEpoch(0);
        setMaxEpoch(Settings.MAX_EPOCH);
        setCurrentTrainingSet(0);
        neuronLayers = new ArrayList<>();
        results = new HashMap<>();
    }

}
