package com.arnis.neuronnet.Net;

import android.content.Context;
import android.content.SharedPreferences;

import com.arnis.neuronnet.Neurons.Neural;
import com.arnis.neuronnet.Neurons.Synapse;

import java.util.ArrayList;

/**
 * Created by arnis on 22/09/2016.
 */

public class Brains {
    private ArrayList<ArrayList<Double>> matrices;
    private SharedPreferences brainsStorage;
    private SharedPreferences dimenStorage;
    private int sch = 1;

    public Brains(Context context) {
        brainsStorage = context.getSharedPreferences("brains",Context.MODE_PRIVATE);
        dimenStorage = context.getSharedPreferences("dimen",Context.MODE_PRIVATE);
        matrices = new ArrayList<>();
    }

    // TODO: 23/09/2016 take biases into consideration
    public boolean checkCompat(NeuronNet net){
        if (net.neuronLayers.size()!=dimenStorage.getInt("total",0))
            return false;
        if (net.neuronLayers.get(0).size()!=dimenStorage.getInt("input",0))
            return false;
        if (net.neuronLayers.get(net.neuronLayers.size()-1).size()!=dimenStorage.getInt("output",0))
            return false;
        for (int i = 1; i < net.neuronLayers.size()-1; i++) {
            if (net.neuronLayers.get(i).size()!=dimenStorage.getInt("hidden"+Integer.toString(i),0))
                return false;
        }

        return true;
    }

    protected void saveBrains(NeuronNet neuronNet){
        brainsStorage.edit().clear().apply();
        for (int i = 0; i < neuronNet.neuronLayers.size()-1; i++) {
            for (int j = 0; j < neuronNet.neuronLayers.get(i).size(); j++) {
                matrices.add(getWeights(neuronNet.neuronLayers.get(i).get(j)));
            }
        }

        saveDimens(neuronNet);
        sch = 1;
    }

    private void saveDimens(NeuronNet neuronNet) {
        SharedPreferences.Editor editor = dimenStorage.edit().clear();
        editor.putInt("total",neuronNet.neuronLayers.size());
        editor.putInt("input",neuronNet.neuronLayers.get(0).size());
        editor.putInt("output",neuronNet.neuronLayers.get(neuronNet.neuronLayers.size()-1).size());
        for (int i = 1; i < neuronNet.neuronLayers.size()-1; i++) {
            editor.putInt("hidden"+Integer.toString(i),neuronNet.neuronLayers.get(i).size());
        }
        editor.apply();
    }

    protected void loadBrains(NeuronNet neuronNet){
        if (matrices.size()==0) {
            matrices.add(new ArrayList<Double>());
            int i=0;
            for (sch=1; sch<= brainsStorage.getAll().size()+i; sch++){
                if (!brainsStorage.contains(Integer.toString(sch))){
                    matrices.add(new ArrayList<Double>());
                    i++;
                } else matrices.get(i).add(Double.parseDouble(brainsStorage.getString(Integer.toString(sch),"0")));
            }
            sch=1;
        }

        int u=0;
        for (int i = 0; i < neuronNet.neuronLayers.size() - 1; i++) {
            for (int k = 0; k < neuronNet.neuronLayers.get(i).size(); k++) {
                for (int j = 0; j < neuronNet.neuronLayers.get(i).get(k).getLinks().size(); j++) {
                    neuronNet.neuronLayers.get(i).get(k).getLinks().get(j).setWeight(matrices.get(u).get(j));
                }
                u++;
            }
        }

    }

    private ArrayList<Double> getWeights(Neural neural){
        ArrayList<Double> arr = new ArrayList<>();
        for (Synapse synapse:neural.getLinks()){
            double d = synapse.getWeight();
            arr.add(d);
            brainsStorage.edit().putString(Integer.toString(sch++),Double.toString(d)).apply();
        }
        sch++;

        return arr;
    }

}
