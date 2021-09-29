package com.example.botnets;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class NewGameActivity extends AppCompatActivity implements View.OnClickListener {

    public int numberOfCities = 66;         // This is the default amount of cities atm.
    private SQLiteHelper sqLiteHelper;
    SQLiteHelper citydb = new SQLiteHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        Button btnThirty = findViewById(R.id.buttonThirty);
        btnThirty.setOnClickListener(this);
        Button btnSixty = findViewById(R.id.buttonSixty);
        btnSixty.setOnClickListener(this);
        Button btnAll = findViewById(R.id.buttonAll);
        btnAll.setOnClickListener(this);
        Button btnStart = findViewById(R.id.buttonStart);
        btnStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String text = "";
        switch (view.getId()) {
            case R.id.buttonThirty:
                text = "30 Cities Selected";
                numberOfCities = 30;
                break;
            case R.id.buttonSixty:
                text = "60 Cities Selected";
                numberOfCities = 60;
                break;
            case R.id.buttonAll:
                text = "ALL Cities Selected";
                numberOfCities = 66;
                break;
            case R.id.buttonStart:
                text = startContinueActivity(view);
                break;
        }
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private String startContinueActivity(View view){
        int nosCities = numberOfCities;
        int friendlyCities = 0;
        boolean newgame = true;
        String returnText = "";

        EditText number = findViewById(R.id.inputNumber);
        String text = number.getText().toString();

        friendlyCities = Integer.parseInt(text);

        if(friendlyCities > ((numberOfCities / 2) - 1)){
            returnText = "You must enter a valid number of friendly cities, e,g  friendlies < " + ((numberOfCities / 2) - 1);
        }   // If the user has requested a number of friendly cities that would result in a crash
        else {

            Intent intent = new Intent(NewGameActivity.this, ContinueActivity.class);
            intent.putExtra("nosCities", nosCities);
            intent.putExtra("nosFriendlies", friendlyCities);
            intent.putExtra("newGame", newgame);

            wipeDB();       // Clear Previous Game

            returnText = "Establishing connection to Control Node";
            startActivity(intent);
        }
        return returnText;
    }

    private void wipeDB(){
        citydb.wipeTable();
    }
}