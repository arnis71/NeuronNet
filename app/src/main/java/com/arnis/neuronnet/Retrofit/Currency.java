package com.arnis.neuronnet.Retrofit;

public class Currency {

    public String symbol;
    public double ask;
    public double bid;
//    public String time;

    public double average(){
        return (ask+bid)/2;
    }
    
}
