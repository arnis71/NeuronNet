package com.arnis.neuronnet.Net;

import java.util.ArrayList;

/**
 * Created by arnis on 04.09.2016.
 */
public class ElmanNN extends NeuronNet {
        public ElmanNN() {
            setEpoch(0);
            setMaxEpoch(5000);
            setCurrentTrainingSet(0);
            neuronLayers = new ArrayList<>();
            results = new ArrayList<>();
        }

}
