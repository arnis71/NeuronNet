package com.arnis.neuronnet.Net;

import com.arnis.neuronnet.Settings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arnis on 04.09.2016.
 */
public class ElmanNN extends NeuronNet {
        public ElmanNN() {
            setEpoch(0);
            setMaxEpoch(Settings.MAX_EPOCH);
            setCurrentTrainingSet(0);
            neuronLayers = new ArrayList<>();
            results = new HashMap<>();
        }

}
