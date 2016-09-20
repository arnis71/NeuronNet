package com.arnis.neuronnet.Net;

import com.arnis.neuronnet.Neurons.BiasNeuron;
import com.arnis.neuronnet.Neurons.HiddenNeuron;
import com.arnis.neuronnet.Neurons.InputNeuron;
import com.arnis.neuronnet.Neurons.Neural;
import com.arnis.neuronnet.Neurons.OutputNeuron;

/**
 * Created by arnis on 04.09.2016.
 */
public class NeuroFactory {
    public static final int INPUT_NEURON = 0;
    public static final int HIDDEN_NEURON = 1;
    public static final int BIAS_NEURON = 2;
    public static final int OUTPUT_NEURON = 3;
    public static final int CONTEXT_NEURON = 4;

    public static Neural getNeuron(int type){
        switch (type){
            case INPUT_NEURON: return new InputNeuron();
            case HIDDEN_NEURON: return new HiddenNeuron();
            case OUTPUT_NEURON: return new OutputNeuron();
            case BIAS_NEURON:return new BiasNeuron();
            case CONTEXT_NEURON:return null;
            default: throw new IllegalArgumentException("No neuron of type "+type);
        }
    }
}
