package com.arnis.neuronnet.Other;

import com.arnis.neuronnet.Retrofit.Data;
import com.arnis.neuronnet.Retrofit.Stock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by arnis on 05.09.2016.
 */
public class TrainingSet {
    public int getSetEntries() {
        return setEntries;
    }

    private int setEntries;
    private ArrayList<Set> trainSet;

    public TrainingSet() {
        trainSet = new ArrayList<>();
        setEntries=0;
    }

    public void addEntry(Set set){
        trainSet.add(set);
        setEntries++;
    }

    public Set getEntry(int index){
        return trainSet.get(index);
    }

    public void addTrainStocks(List<Stock> stocks){
        List<Double> percentage = new ArrayList<>();

        double difference;
        double res;

        for (int i = 0; i < stocks.size() - 1; i++) {
            difference = (stocks.get(i+1).average()-stocks.get(i).average());
            res = difference/stocks.get(i).average();
            if (res>1)
                res=1;
            percentage.add(res);
        }

        for (int i = 0; i < percentage.size() - 3; i++) {
            addEntry(new Set(
                    stocks.get(i).Symbol+" "+stocks.get(i+4).Date,
                    new double[]{percentage.get(i),percentage.get(i+1),percentage.get(i+2)},
                    new double[]{percentage.get(i+3)}));
        }

    }
    public void addWorkStocks(List<Stock> stocks){
        List<Double> percentage = new ArrayList<>();

        double difference;
        double res;

        for (int i = 0; i < stocks.size() - 1; i++) {
            difference = (stocks.get(i+1).average()-stocks.get(i).average());
            res = difference/stocks.get(i).average();
            if (res>1)
                res=1;
            percentage.add(res);
        }

        for (int i = 0; i < percentage.size() - 2; i++) {
            addEntry(new Set(stocks.get(i).Symbol, new double[]{percentage.get(i),percentage.get(i+1),percentage.get(i+2)}));
        }
        res=0;

    }

    public static class Set{
        private String description;
        private double[] inputValues;
        private double[] desiredOutput;

        public Set(String description, double[] inputValues, double[] desiredOutput) {
            this.description = description;
            this.inputValues = inputValues;
            this.desiredOutput = desiredOutput;
        }
        public Set(String description, double[] inputValues) {
            this.description = description;
            this.inputValues = inputValues;
        }

//        public Set() {
//            inputValues
//        }
//
//        public void addInput(double input){
//            inputValues
//        }

        public String getDescription() {
            return description;
        }

        public double getInputValue(int index){
            return inputValues[index];
        }

        public double getOutputValue(int index){
            return desiredOutput[index];
        }

        public double[] getInputValues() {
            return inputValues;
        }

        public double[] getDesiredOutput() {
            return desiredOutput;
        }


    }
}
