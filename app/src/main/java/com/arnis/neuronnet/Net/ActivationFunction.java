package com.arnis.neuronnet.Net;

/**
 * Created by arnis on 04.09.2016.
 */
public interface ActivationFunction {

    public static final String SIGMOID = "sigmoid";
    public static final String HYPERBOLIC_TANGENT = "tangent";

    double calculate(double x);
    double calculateDerivative(double x);

    class Sigmoid implements ActivationFunction{

        @Override
        public double calculate(double x) {
            return (1/( 1 + Math.exp(-x)));
        }

        @Override
        public double calculateDerivative(double out) {
            return out*(1-out);
        }
    }

    class HyperTangent implements ActivationFunction{

        @Override
        public double calculate(double x) {
            return Math.tanh(x);
        }

        @Override
        public double calculateDerivative(double out) {
            return 1-Math.pow(out,2);
        }
    }
}
