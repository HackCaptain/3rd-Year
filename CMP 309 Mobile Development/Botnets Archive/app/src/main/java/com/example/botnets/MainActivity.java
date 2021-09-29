package com.example.botnets;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "Botnet_Debug_Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button buttonCon = findViewById(R.id.buttonContinue);
        buttonCon.setOnClickListener(this);
        Button buttonNew = findViewById(R.id.buttonNew);
        buttonNew.setOnClickListener(this);
        Button buttonOpt = findViewById(R.id.buttonOptions);
        buttonOpt.setOnClickListener(this);

        Log.d(TAG, "Main Activity booted");
    }

    @Override
    public void onClick(View view) {
        String text = "";
        switch (view.getId()) {
            case R.id.buttonContinue:
                text = "Continuing game...";
                startContinueActivity();
                break;
            case R.id.buttonNew:
                text = "Establishing botnet";
                startNewGameActivity();
                break;
            case R.id.buttonOptions:
                text = "Manage your Botnet management application";
                startOptionsActivity();
                break;
        }
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void startContinueActivity(){
        Intent intent = new Intent(MainActivity.this, ContinueActivity.class);
        startActivity(intent);
    }

    private void startNewGameActivity(){
        Intent intent = new Intent(MainActivity.this, NewGameActivity.class);
        startActivity(intent);
    }

    private void startOptionsActivity(){
        Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
        startActivity(intent);
    }


}