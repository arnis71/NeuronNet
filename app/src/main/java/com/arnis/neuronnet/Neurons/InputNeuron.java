package com.arnis.neuronnet.Neurons;

import java.util.ArrayList;

/**
 * Created by arnis on 04.09.2016.
 */
public class InputNeuron extends Neural {

    public InputNeuron() {
        this.links = new ArrayList<>();
    }

    @Override
    public void setInputValue(double inputValue) {
        super.setInputValue(inputValue);
        this.setOutputValue(inputValue);
    }

    @Override
    public void calculateOut() {

    }

    @Override
    public void linkWithLayer(ArrayList<Neural> layer) {

    }
}
