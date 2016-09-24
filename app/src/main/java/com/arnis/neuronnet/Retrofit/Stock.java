package com.arnis.neuronnet.Retrofit;

public class Stock {
    public String Symbol;
    public String Date;
    public String Time;
    public double Open;
    public double Close;

    public double average(){
        return (Open+Close)/2;
    }
}
