package com.arnis.neuronnet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Collections;
import java.util.Set;

import static com.arnis.neuronnet.MainActivity.COMPLEX_ANALYSIS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_BRAINS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ERROR;
import static com.arnis.neuronnet.MainActivity.SETTINGS_ITERATIONS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_PREDICTION;
import static com.arnis.neuronnet.MainActivity.SETTINGS_PREFS;
import static com.arnis.neuronnet.MainActivity.SETTINGS_STOCKS_TYPE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_TRAIN;
import static com.arnis.neuronnet.MainActivity.SETTINGS_TYPE;
import static com.arnis.neuronnet.MainActivity.SETTINGS_WINDOW;

public class Settings extends AppCompatActivity {

    private Button brains;
    private Spinner error;
    private Spinner type;
    private EditText iter;
    private CheckBox train;
    private EditText stockType;
    private CheckBox complex;
    private EditText window;
    private EditText prediction;

    private SharedPreferences prefs;
    private boolean isTrain;
    private boolean isComplex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        brains = (Button)findViewById(R.id.load_brains);
        error = (Spinner)findViewById(R.id.error_calc);
        type = (Spinner)findViewById(R.id.enn_type);
        iter = (EditText)findViewById(R.id.max_epoch);
        stockType = (EditText)findViewById(R.id.stocks);
        train = (CheckBox)findViewById(R.id.train_nn);
        complex = (CheckBox)findViewById(R.id.complex);
        window = (EditText)findViewById(R.id.window);
        prediction = (EditText)findViewById(R.id.prediction);
//        momentum = (EditText)findViewById(R.id.momentum);
//        learning = (EditText)findViewById(R.id.learning_rate);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.error, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        error.setAdapter(adapter2);

        brains.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        train.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTrain=isChecked;
            }
        });
        complex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isComplex = isChecked;
            }
        });


    }

    private void showDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Select brains");
        SharedPreferences brains = getSharedPreferences(MainActivity.BRAINS_STORAGE,MODE_PRIVATE);
        Set<String> set = brains.getAll().keySet();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_item);
        arrayAdapter.add("default");
        arrayAdapter.addAll(set);

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String strName = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(Settings.this);
                        SharedPreferences preferences = getSharedPreferences(strName+"_info",MODE_PRIVATE);
                        int epoch =preferences.getInt("epoch",0);
                        String error = preferences.getString("error","0");
                        error = String.format("%.4f",Double.parseDouble(error)*100)+"%";
                        builderInner.setMessage("This brain epoch is "+Integer.toString(epoch)+ " and approximate error is "+ error);
                        builderInner.setTitle(strName);
                        builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        prefs.edit().putString(SETTINGS_BRAINS,strName).apply();
                                    }
                                });
                        builderInner.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getSharedPreferences(MainActivity.BRAINS_STORAGE, Context.MODE_PRIVATE).edit().remove(strName).apply();
                                getSharedPreferences(strName+"_info",Context.MODE_PRIVATE).edit().clear().apply();
                                getSharedPreferences(strName+"_brains",Context.MODE_PRIVATE).edit().clear().apply();
                                getSharedPreferences(strName+"_storage",Context.MODE_PRIVATE).edit().clear().apply();
                                arrayAdapter.remove(strName);
                                dialog.dismiss();
                            }
                        });
                        builderInner.show();
                    }
                });
        builderSingle.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs = getSharedPreferences(SETTINGS_PREFS,MODE_PRIVATE);
        loadSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    private void saveSettings(){
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(COMPLEX_ANALYSIS,isComplex);
        edit.putBoolean(SETTINGS_TRAIN,isTrain);
        edit.putInt(SETTINGS_ERROR,error.getSelectedItemPosition());
        edit.putInt(SETTINGS_TYPE,type.getSelectedItemPosition());
        String check = iter.getText().toString();
        if (!check.equals(""))
            edit.putInt(SETTINGS_ITERATIONS,Integer.parseInt(check));
        check = stockType.getText().toString();
        if (!check.equals(""))
            edit.putString(SETTINGS_STOCKS_TYPE,check);
        check = window.getText().toString();
        if (!check.equals(""))
            edit.putInt(SETTINGS_WINDOW,Integer.parseInt(check));
        check = prediction.getText().toString();
        if (!check.equals(""))
            edit.putInt(SETTINGS_PREDICTION,Integer.parseInt(check));
        edit.apply();
    }
    private void loadSettings(){
        complex.setChecked(prefs.getBoolean(COMPLEX_ANALYSIS,false));
        train.setChecked(prefs.getBoolean(SETTINGS_TRAIN,false));
        iter.setText(Integer.toString(prefs.getInt(SETTINGS_ITERATIONS,5000)));
        type.setSelection(prefs.getInt(SETTINGS_TYPE,0));
        error.setSelection(prefs.getInt(SETTINGS_ERROR,0));
        stockType.setText(prefs.getString(SETTINGS_STOCKS_TYPE,"AAPL"));
        window.setText(Integer.toString(prefs.getInt(SETTINGS_WINDOW,2)));
        prediction.setText(Integer.toString(prefs.getInt(SETTINGS_PREDICTION,1)));
    }
    @Override
    public void finish() {
        super.finish();
        // кастомная анимация
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
    }
}
