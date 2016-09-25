package com.arnis.neuronnet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

public class Settings extends AppCompatActivity {


    public static boolean USE_BRAINS;
    public static int MAX_EPOCH;
    public static String NN_TYPE;
    public static String ERROR_CALC;

    private CheckBox brains;
    private Spinner error;
    private Spinner type;
    private EditText epoch;
    private EditText momentum;
    private EditText learning;
    private EditText window;
    private EditText predict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        brains = (CheckBox)findViewById(R.id.load_brains);
        error = (Spinner)findViewById(R.id.error_calc);
        type = (Spinner)findViewById(R.id.enn_type);
        epoch = (EditText)findViewById(R.id.max_epoch);
        momentum = (EditText)findViewById(R.id.momentum);
        learning = (EditText)findViewById(R.id.learning_rate);
        window = (EditText)findViewById(R.id.prediction_window);
        predict = (EditText)findViewById(R.id.predict);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.error, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        error.setAdapter(adapter2);

        brains.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                USE_BRAINS =isChecked;
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        // кастомная анимация
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
    }

    public void apply(View view) {
        MAX_EPOCH = Integer.parseInt(epoch.getText().toString());
        NN_TYPE = (String) type.getItemAtPosition(type.getSelectedItemPosition());
        ERROR_CALC = (String) error.getItemAtPosition(error.getSelectedItemPosition());
    }
}
