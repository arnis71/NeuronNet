package com.arnis.neuronnet.Net;

import android.content.SharedPreferences;

/**
 * Created by arnis on 05.09.2016.
 */
public interface Error {
    double calculate(int outputs,double totalError);

    class MeanSquaredError implements Error{

        @Override
        public double calculate(int outputs,double totalError) {
            return totalError/outputs;
        }

        public double squareError(double[] ideal,double[] actual){
            double sum=0;
            for (int i = 0; i < actual.length; i++) {
                sum+=Math.pow(ideal[i]-actual[i],2);
            }
            return sum/actual.length;
        }
    }

    class SimpleError implements Error{

        @Override
        public double calculate(int outputs, double totalError) {
            return totalError;
        }
        public double absError(double[] ideal,double[] actual){
            double sum=0;
            for (int i = 0; i < actual.length; i++) {
                sum+=Math.abs(ideal[i]-actual[i]);
            }
            return sum/actual.length;
        }
    }

    class ArctanError implements Error{

        @Override
        public double calculate(int outputs,double totalError) {
            return 0;
        }
    }
}
