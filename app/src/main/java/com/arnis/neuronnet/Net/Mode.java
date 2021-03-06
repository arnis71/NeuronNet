package com.arnis.neuronnet.Net;

import android.util.Log;

/**
 * Created by arnis on 06.09.2016.
 */
public interface Mode {
    void start(NeuronNet neuronNet);

    class Learning implements Mode{
        @Override
        public void start(NeuronNet neuronNet) {
            Log.d("happy", "TRAINING");
            neuronNet.train();
        }
    }

    class Validation implements Mode{
        @Override
        public void start(NeuronNet neuronNet) {
            Log.d("happy", "VALIDATION");
        }
    }
    class Working implements Mode{
        @Override
        public void start(NeuronNet neuronNet) {
            Log.d("happy", "WORKING");
            neuronNet.loadValuesFromSet(0);
            neuronNet.calculateInOut();
            neuronNet.getInfo();
        }
    }
}
