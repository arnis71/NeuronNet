package com.arnis.neuronnet.Other;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arnis on 24/09/2016.
 */

public interface ValueChangeListener {
    void onValueChange(double value);
    void onValueChange(double ask,double bid);
    void onPositionOpen(double at,String direction,double amount);
    void onValueChange(ArrayList<Double> values);
    //ArrayList<List<Entry>> values for complex
}
