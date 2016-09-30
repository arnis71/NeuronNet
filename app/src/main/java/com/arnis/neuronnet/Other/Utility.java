package com.arnis.neuronnet.Other;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.arnis.neuronnet.Retrofit.Currency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by student on 2016.
 */
public class Utility {

//    public static void main(String[] args) {
//
//        String forex = getPage("http://www.forexpf.ru");
//
//        // System.out.println(forex);
//
//        System.out.println(getUSDRUB(forex));
//
//    }

    public static String getPage(String pageUrl)
    {
        StringBuffer buffer = new StringBuffer();
        InputStream is = null;
        BufferedReader br = null;

        try {
            URL url = new URL(pageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int resultCode = conn.getResponseCode();
            if(resultCode == 200)
            {
                is = conn.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line = "";

                while (( line = br.readLine()) != null)
                {
                    buffer.append(line);
                }
                br.close();
                is.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static Currency getUSDRUB(String text)
    {
        Currency currency = new Currency();

        String regexpUSD = "<font color=\"Red\"><b>(\\d+.\\d+)</b></font><b> / </b><font color='green'><b>(\\d+.\\d+)</b>";
        Pattern patternUSD = Pattern.compile(regexpUSD);

        Matcher matcherUSD = patternUSD.matcher(text);

        String result = "";
        if(matcherUSD.find())
        {
            currency.symbol = "USDRUB";
            currency.ask = Double.parseDouble(matcherUSD.group(1));
            currency.bid = Double.parseDouble(matcherUSD.group(2));
        }
        return currency;
    }

    public static void animateScrollView(HorizontalScrollView scrollView){
        ObjectAnimator animator=ObjectAnimator.ofInt(scrollView, "scrollX",HorizontalScrollView.FOCUS_RIGHT);
        animator.setDuration(300);
        animator.setInterpolator(new OvershootInterpolator());
        animator.start();
    }
    public static void animateDrawerOut(View listView){
        ObjectAnimator animator = ObjectAnimator.ofFloat(listView,View.TRANSLATION_X,-1440);
        animator.setDuration(300).setInterpolator(new AccelerateInterpolator());
        animator.start();
    }
    public static void animateDrawerIn(View listView){
        ObjectAnimator animator = ObjectAnimator.ofFloat(listView,View.TRANSLATION_X,0);
        animator.setDuration(300).setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
    public static void animateDrawerPop(View listView){
        ObjectAnimator animator = ObjectAnimator.ofFloat(listView,View.TRANSLATION_X,-1200);
        animator.setDuration(1000).setInterpolator(new BounceInterpolator());
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();
    }

}
