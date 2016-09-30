package com.arnis.neuronnet.Other;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arnis.neuronnet.R;

import java.util.ArrayList;

/**
 * Created by arnis on 30/09/2016.
 */

public class ListAdapter extends BaseAdapter {

    private Activity activity;

    public ListAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return Position.size();
    }

    @Override
    public Position getItem(int position) {
        return Position.getPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return Position.getPosition(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            convertView = LayoutInflater.from(activity).inflate(R.layout.info_tab,null);
        }

        final TextView timer = (TextView)convertView.findViewById(R.id.timer);
        TextView pos = (TextView)convertView.findViewById(R.id.position_open);
        TextView direction = (TextView)convertView.findViewById(R.id.position_direction);
        final TextView amount = (TextView)convertView.findViewById(R.id.position_amount);
        final RelativeLayout relativeLayout = (RelativeLayout)convertView.findViewById(R.id.tab);

        Position position1 = getItem(position);
        position1.setTtlListener(new ValueChangeListener() {
            @Override
            public void onValueChange(final double value) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timer.setText(Integer.toString((int)(value/1000)));
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onValueChange(String value) {

            }

            @Override
            public void onValueChange(double ask, double bid) {

            }

            @Override
            public void onValueChange(ArrayList<Double> values) {

            }
        });
        final View finalConvertView = convertView;
        position1.setStateListener(new ValueChangeListener() {
            @Override
            public void onValueChange(double value) {

            }

            @Override
            public void onValueChange(final String value) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        finalConvertView.setBackgroundColor();
                        relativeLayout.setBackgroundColor(getColor(value));
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onValueChange(double ask, double bid) {

            }

            @Override
            public void onValueChange(ArrayList<Double> values) {

            }
        });

        pos.setText(Double.toString(position1.getOpen()));
        direction.setText(position1.getDirection());
        String am = String.format("%.2f", position1.getAmount())+"$";
        amount.setText(am);

        return convertView;
    }
    private int getColor(String direction){
        switch (direction){
            case "up": return Color.GREEN;
            case "success": return Color.GREEN;
            case "down": return Color.RED;
            case "fail": return Color.RED;
            default: return Color.LTGRAY;
        }
    }
}
