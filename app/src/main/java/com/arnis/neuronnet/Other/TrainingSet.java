package com.arnis.neuronnet.Other;

import java.util.ArrayList;

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
