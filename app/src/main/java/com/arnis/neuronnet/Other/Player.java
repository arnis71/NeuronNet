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
    private NeuralHelper helper;
    private ArrayList<Currency> chart;
    private double money;
    private double buyRate;
    private Context context;
    private ValueChangeListener rateChange;
    private boolean analyticsMode;

    public void setRateChangeListener(ValueChangeListener listener){
        rateChange = listener;
    }
    private ValueChangeListener predictionChange;
    public void setPredictionChangeListener(ValueChangeListener listener){
        predictionChange = listener;
    }
    private ValueChangeListener positionOpened;
    public void setPositionChangeListener(ValueChangeListener listener){
        positionOpened = listener;
    }
    private ValueChangeListener signal;
    public void setSignalListener(ValueChangeListener listener){
        signal = listener;
    }

    String direction;
    boolean canPredict;
    boolean canTrain;
    boolean active;
    boolean canBuy;


    public Player(Context context, NeuralHelper helper) {
        this.helper = helper;
        chart = new ArrayList<>();
        money = 100;//dollars
        this.context = context;
        buyRate = 0.1;
        canPredict=true;
        canTrain=true;
        active=false;
        canBuy=true;
    }




    public void play(){
        active=true;
        loadData();
        if (chart.size()>0){
            for (int i = 0; i < chart.size(); i++) {
                rateChange.onValueChange(chart.get(i).ask,chart.get(i).bid);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (helper.isTraining())
                    initiateTraining();
                while (active){
                    try {
                        String result = Utility.getPage("http://www.forexpf.ru/currency_usd.asp");
                        Currency currency = Utility.getUSDRUB(result);
                        if (currency!=null){
                            addCurrencyToChart(currency);
                            rateChange.onValueChange(currency.ask,currency.bid);
                            saveData(currency);
                            Log.d(TAG, "new entry "+String.format("%.4f",chart.get(chart.size()-1).average()) +" total: "+Integer.toString(chart.size())+" entries");
                            makePrediction();
                        }
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                helper.storeData();
            }
        }).start();
    }

    public void stop(){
        active=false;
    }

    private void addCurrencyToChart(Currency currency){
        if (chart.size()>helper.getFloatingWindow()) {
            for (int i = chart.size()-1; i > helper.getFloatingWindow()-2; i--) {
                chart.remove(0);
            }
        }
        Position.ask = currency.ask;
        chart.add(currency);
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
    public void deleteData(){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("currency.txt", Context.MODE_PRIVATE));
            outputStreamWriter.append("");
            outputStreamWriter.close();
//            Log.d(TAG, "data deleted");
        }
        catch (IOException e) {
        }
    }

    private Currency lineToCurrency(String[] string){
        Currency currency = new Currency();
        currency.symbol = string[0];
        currency.ask = Double.parseDouble(string[1]);
        currency.bid = Double.parseDouble(string[2]);
        return currency;
    }

    private void initiateTraining(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (active) {
                        if (canTrain&&chart.size()>10) {
                            Log.d(TAG, "initiateTraining...");
                            helper.setMode(NeuronNet.TRAINING_MODE);
                            helper.addCurrencyData(chart);
                            canPredict = false;
                            helper.run();
                            helper.join();
                            canPredict = true;
                        }
                        try {
                            Thread.sleep(30000);
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
//              TODO: 28/09/2016 for complex solution
//            predictionChange.onValueChange(helper.currencyPredictionsToChart(chart));

            ArrayList<Double> predictions = averageOfAllPredictions(helper.getPredictions());
            ArrayList<Double> results = new ArrayList<>();
            results.add(chart.get(chart.size() - 1).average());
            for (int i = 0; i < predictions.size(); i++) {
                if (i == 0) {
                    results.add(chart.get(chart.size() - 1).average() + (predictions.get(i) * chart.get(chart.size() - 1).average()));
                } else {
                    results.add(results.get(i - 1) + (predictions.get(i) * results.get(i - 1)));
                }
            }

            predictionChange.onValueChange(results);

            for (int i=0;i<results.size()-1;i++) {
                if (results.get(i) < results.get(i+1) && (direction == null || !direction.equals("down"))) {
                    direction = "up";
                } else if (results.get(i) > results.get(i+1) && (direction == null || !direction.equals("up"))) {
                    direction = "down";
                } else {
                    Log.d(TAG, "uncertain prediction");
                    direction = "";
                    signal.onValueChange(direction);
                    return;
                }
                buy();
            }
            signal.onValueChange(direction);
        }
    }

    private void buy(){
        if (!analyticsMode&&canBuy&&chart.size()>20){
            canBuy=false;
            final int id = new Position().setAction(Position.BUY).setInfo(chart.get(chart.size()-1).ask,direction,money*buyRate);
            Log.d(TAG, "buying "+ Integer.toString(id));
            positionOpened.onValueChange(id,money);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Position.getPositionById(id).join();
                    sell(id);
                    canBuy=true;
                }
            }).start();
        }
    }
    private void sell(int id){
        Position position = Position.getPositionById(id);
        if (position!=null) {
            Log.d(TAG, "selling "+ Integer.toString(id));
            money += position.getResult(chart.get(chart.size() - 1).ask);
            position.setAction(Position.SELL);
            positionOpened.onValueChange(id,money);
        }
    }



    private double average(ArrayList<Double> arr){
        Double sum=new Double("0");
        for (Double d:arr)
            sum+=d;
        sum-=arr.get(0);
        return sum/(arr.size()-1);
    }

    private ArrayList<Double> averageOfAllPredictions(ArrayList<double[]> predictions){
        ArrayList<Double> fin = new ArrayList<>();
        double sum=0;
        int sch=0;
        int i;
        for (int j = 0; j < predictions.get(predictions.size()-1).length; j++) {
            for (i = 0; i < predictions.size(); i++) {
                if (predictions.get(i).length>j){
                    sum+=predictions.get(i)[j];
                    sch++;
                }
            }
            fin.add(sum/sch);
            sch=0;
        }
        return fin;
    }

    public void newHelper(NeuralHelper helper) {
        this.helper = helper;
    }

    public void setMode(boolean analyticsMode) {
        this.analyticsMode = analyticsMode;
    }
}
