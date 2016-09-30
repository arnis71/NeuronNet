package com.arnis.neuronnet.Other;


import java.util.ArrayList;
import java.util.Random;

/**
 * Created by arnis on 29/09/2016.
 */

public class Position {
    private static ArrayList<Position> positions = new ArrayList<>();
    public static double ask;
    public static final String SELL ="sell";
    public static final String BUY ="buy";
    public static Position getPositionById(int id){
        for (Position position:positions)
            if (position.id==id)
                return position;

        return null;
    }
    protected static Position getPosition(int index){
        return positions.get(index);
    }
    public static int size(){
        return positions.size();
    }

    private Random rnd = new Random();
    private int id;
    private double open;
    private double close;
    private String action;
    private String direction;
    private double amount;

    private ValueChangeListener ttlListener;
    public void setTtlListener(ValueChangeListener listener){
        ttlListener=listener;
    }
    private ValueChangeListener stateListener;
    public void setStateListener(ValueChangeListener listener){
        stateListener=listener;
    }

    private int ttl;
    private Thread living;

    public Position() {
        positions.add(this);
    }

    public int setInfo(double at, String direction, double amount){
        this.open = at;
        this.direction = direction;
        this.amount = amount;
        id = rnd.nextInt(1000000)+1;
        ttl=60000;
        countdown();
        return id;
    }
    public Position setAction(String action){
        this.action = action;
        return this;
    }

    private void countdown(){
        living = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 60000; i >=0 ; i-=1000) {
                    ttl=i;
                    if (ttlListener!=null){
                        ttlListener.onValueChange(ttl);
                        if (ask>open&&direction.equals("up")){
                            stateListener.onValueChange("success");
                        } else if (ask<open&&direction.equals("down")){
                            stateListener.onValueChange("succes");
                        } else if (ask==open) {
                            stateListener.onValueChange("draw");
                        } else stateListener.onValueChange("fail");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        living.start();
    }
    public void join(){
        if (living!=null)
            try {
                living.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    public String getDirection() {
        return direction;
    }
    public double getResult(double ask){
        close=ask;
        double result = close - open;
        if (result > 0 && direction.equals("up")) {
            return amount * 0.7;
        } else if (result < 0 && direction.equals("down")) {
            return amount * 0.7;
        } else if (result == 0) {
            return result;
        } else {
            return amount*-1;
        }
    }

    public String getResult(){
        if (action.equals(SELL)){
            double result = close - open;
            if (result > 0 && direction.equals("up")) {
                return "success";
            } else if (result < 0 && direction.equals("down")) {
                return "success";
            } else if (result == 0) {
                return "draw";
            } else {
                return "fail";
            }
        }
        return null;
    }
    public double getClose() {
        return close;
    }
    public String getAction() {
        return action;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public boolean checkDirection(String direction){
        if (this.direction.equals(direction))
            return true;
        else return false;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public static void clear() {
        positions.clear();
    }
}
