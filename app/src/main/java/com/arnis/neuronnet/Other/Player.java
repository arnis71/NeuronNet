package com.arnis.neuronnet.Other;

import android.content.Context;
import android.util.Log;

import com.arnis.neuronnet.Net.NeuralHelper;
import com.arnis.neuronnet.Net.NeuronNet;
import com.arnis.neuronnet.Retrofit.Currency;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by arnis on 26/09/2016.
 */

public class Player {
    private static final String TAG = "happytech";
    private Currency currency;
    private NeuralHelper helper;
    private ArrayList<Currency> chart;
    private double money;
    double openedPosition;
    double amount;
    String decision;
    private Context context;

    String direction;
    boolean canPredict;
    boolean canTrain;


    public Player(Context context, NeuralHelper helper) {
        this.helper = helper;
        chart = new ArrayList<>();
        money = 100;//dollars
        this.context = context;
        openedPosition =0;
        decision="";
        amount=0;
        canPredict=false;
        canTrain=true;
    }

    // TODO: 28/09/2016 apply floating window e.g. 100 entries 


    public void play(){
        loadData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                training();
                while (true){
                    try {
                        String result = Utility.getPage("http://www.forexpf.ru/currency_usd.asp");
                        Currency currency = Utility.getUSDRUB(result);
                        if (currency!=null){
                            chart.add(currency);
                            saveData(currency);
                            Log.d(TAG, "new entry "+String.format("%.4f",chart.get(chart.size()-1).average()) +" total: "+Integer.toString(chart.size())+" entries");
                            makePrediction();
                        }
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void saveData(Currency currency) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("currency.txt", Context.MODE_APPEND));
            String append = currency.symbol+" "+ Double.toString(currency.ask)+" "+Double.toString(currency.bid)+"\n";
            outputStreamWriter.append(append);
            outputStreamWriter.close();
//            Log.d(TAG, "data saved");
        }
        catch (IOException e) {
        }
    }
    private void loadData(){
        try {
            InputStream inputStream = context.openFileInput("currency.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    String[] stre = receiveString.split(" ");
                    chart.add(lineToCurrency(stre));
                }

                inputStream.close();
                Log.d(TAG, "data loaded");
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
    }

    private Currency lineToCurrency(String[] string){
        Currency currency = new Currency();
        currency.symbol = string[0];
        currency.ask = Double.parseDouble(string[1]);
        currency.bid = Double.parseDouble(string[2]);
        return currency;
    }
    private int trimTime(String time){
        time = time.replaceAll(":","");
        return Integer.parseInt(time);
    }
    private String toTime(int time){
        String res = Integer.toString(time);
        res = res.substring(0,2)+":"+res.substring(2,4);
        return res;
    }

    private void training(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (canTrain&&chart.size()>10) {
                            Log.d(TAG, "training...");
                            helper.setMode(NeuronNet.TRAINING_MODE);
                            helper.addCurrencyData(chart);
                            canPredict = false;
                            helper.run();
                            helper.join();
                            canPredict = true;
                        }
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

    }
    private void makePrediction(){
        if (chart.size()>10&&canPredict) {
            Log.d(TAG, "predicting..");
            helper.setMode(NeuronNet.WORKING_MODE);
            helper.addCurrencyData(chart);
            canTrain=false;
            helper.run();
            helper.join();
            canTrain=true;

            ArrayList<Double> predictions = handlePrediction(helper.getPredictions());
            ArrayList<Double> changes = new ArrayList<>();
            ArrayList<Double> results = new ArrayList<>();
            for (int i = 0; i < predictions.size(); i++) {
                if (i == 0) {
                    changes.add(predictions.get(i) * chart.get(chart.size() - 1).ask);// TODO: 27/09/2016
                    results.add(chart.get(chart.size() - 1).ask + changes.get(i));
                } else {
                    changes.add(predictions.get(i) * results.get(i - 1));
                    results.add(results.get(i - 1) + changes.get(i));
                }
            }

            for (Double d : changes) {
                if (d > 0 && (direction == null || !direction.equals("down"))) {
                    direction = "up";
                    buy();
                } else if (d < 0 && (direction == null || !direction.equals("up"))) {
                    direction = "down";
                    buy();
                } else {
                    Log.d(TAG, "uncertain prediction");
                    direction = "";
//                    sell(false);
                    return;
                }
//                if (!decision.equals("") && !direction.equals("") && !direction.equals(decision))
//                    sell(false);
            }

            Log.d(TAG, "prediction " + direction.toUpperCase() + " from " +
                    String.format("%.4f", chart.get(chart.size() - 1).average()) + " to " + String.format("%.4f", average(results)));
        }
    }

    private void buy(){
        if (openedPosition==0&&chart.size()>20){
            openedPosition = chart.get(chart.size()-1).ask;// TODO: 28/09/2016 ask for binary
            decision=direction;
            amount = (money*0.05);
            Log.d("happytechcheck", "POSITION OPENED at "+ String.format("%.4f",openedPosition)+ ", decision - "+ decision);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("happytechcheck", "30 sec to sell manually");
                        Thread.sleep(30000);
                        Log.d("happytechcheck", "Time is up, order close in 30 sec");
                        Thread.sleep(30000);
                        sell();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }
    private void sell(){
        if (openedPosition!=0) {
            double result = chart.get(chart.size() - 1).ask - openedPosition;
            if (result > 0 && decision.equals("up")) {
                result = amount+(amount*0.7);
                money += result;
                Log.d("happytechcheck", "SUCCESS, earned " + String.format("%.2f",result) + "$");
            } else if (result < 0 && decision.equals("down")) {
                result = amount+(amount*-0.7);
                money += result;
                Log.d("happytechcheck", "SUCCESS, earned " + String.format("%.2f",result) + "$");
            } else {
                money -= amount;
                Log.d("happytechcheck", "FAIL, lost " + String.format("%.1f",amount) + "$");
            }
            Log.d("happytechcheck", "POSITION CLOSED at " + String.format("%.4f",chart.get(chart.size() - 1).ask) + ", current money " + String.format("%.2f",money)+"$");
            decision = "";
            openedPosition = 0;
        }
    }


    private double average(ArrayList<Double> arr){
        Double sum=new Double("0");
        for (Double d:arr)
            sum+=d;
        return sum/arr.size();
    }

    private ArrayList<Double> handlePrediction(ArrayList<double[]> predictions){
        ArrayList<Double> fin = new ArrayList<>();
        double sum=0;
        for (int j = 0; j < predictions.get(0).length; j++) {
            for (int i = 0; i < predictions.size(); i++) {
                sum+=predictions.get(i)[j];
            }
            fin.add(sum/predictions.size());
        }
        return fin;
    }
}
