package com.arnis.neuronnet.Net;

import android.content.SharedPreferences;

/**
 * Created by arnis on 05.09.2016.
 */
public interface Error {
    double calculate(int predictions,double idealOut,double actualOut);

    class MeanSquaredError implements Error{

        @Override
        public double calculate(int predictions,double idealOut,double actualOut) {
            double sum = 0;
            for (int i = 0; i < predictions; i++) {
                sum+=Math.pow(idealOut-actualOut,2);
            }
            return sum/predictions;
        }
    }

    class ArctanError implements Error{
        @Override
        public double calculate(int predictions,double idealOut,double actualOut) {
            double sum = 0;
            for (int i = 0; i < predictions; i++) {
                sum+=Math.pow(Math.atan(idealOut-actualOut),2);
            }
            return sum/predictions;
        }
    }
}
