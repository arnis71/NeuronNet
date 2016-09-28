package com.arnis.neuronnet.Net;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arnis on 04.09.2016.
 */
public class FeedForwardNN extends NeuronNet {

    protected FeedForwardNN() {
        setIteration(0);
        setCurrentTrainingSet(0);
        neuronLayers = new ArrayList<>();
    }

}
