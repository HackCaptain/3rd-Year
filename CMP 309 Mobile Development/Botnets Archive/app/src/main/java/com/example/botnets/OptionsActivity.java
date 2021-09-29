package com.example.botnets;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.botnets.R.id.etCityID;
import static com.example.botnets.R.id.etCityName;
import static com.example.botnets.R.id.etLatitude;
import static com.example.botnets.R.id.etLongitude;
import static com.example.botnets.R.id.etStatus;

public class OptionsActivity extends AppCompatActivity implements  View.OnTouchListener, View.OnClickListener, SensorEventListener {


    SQLiteHelper citydb = new SQLiteHelper(this);

    private SensorManager sensorManager;
    private Sensor accelerometer;

    float[] hsv = new float[3];
    View layoutRef;

    Button buttonAdd;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        layoutRef = findViewById(R.id.mylayout);
        layoutRef.setOnTouchListener(this);

        Button buttonGetCity = findViewById(R.id.btnGet);
        buttonGetCity.setOnClickListener(this);
        Button buttonUpdate = findViewById(R.id.btnUpdateCity);
        buttonUpdate.setOnClickListener(this);
        Button btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);

        buttonAdd = findViewById(R.id.btnAddCity);
        buttonAdd.setOnClickListener(this);
        //buttonAdd.setOnTouchListener(this);

        hsv[0] = 0.0f; // Hue
        hsv[1] = 0.0f; // Saturation
        hsv[2] = 1.0f; // Value
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(accelerometer != null){
            sensorManager.registerListener( this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setMessage("Accelerometer is not available on this device!")
                    .setTitle("Sensor Unavailable")
                    .setCancelable(true);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onClick(View view) {
        String text = "";
        switch (view.getId()) {
            case R.id.btnAddCity:
                text = "Adding city to table";
                addACity(this);
                break;
            case R.id.btnGet:
                text = getACity(this);
                break;
            case R.id.btnUpdateCity:
                text = updateACity(this);
                break;
            case R.id.btnHome:
                text = "Going Home";
                returnHome();
                break;
        }
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            ((TextView)findViewById(R.id.textAccelX)).setText("X : " + sensorEvent.values[0]);
            ((TextView)findViewById(R.id.textAccelY)).setText("Y : " + sensorEvent.values[1]);
            ((TextView)findViewById(R.id.textAccelZ)).setText("Z : " + sensorEvent.values[2]);
        }
    }   // Gyro Sensor

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void changeBackgroundColour(MotionEvent event) {

        float eventX = event.getX();
        float eventY = event.getY();
        float height = layoutRef.getHeight(); // make sure the ref is declared and initialised (this is a reference to your root layout)
        float width = layoutRef.getWidth();
        hsv[0] = eventY / height * 360; // (0 to 360)
        hsv[1] = eventX / width + 0.1f; // (0.1 to 1)
        layoutRef.setBackgroundColor(Color.HSVToColor(hsv));
    }   // Changes HSV of the

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                hsv[2] += 0.1f;
                layoutRef.setBackgroundColor(Color.HSVToColor(hsv));
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && hsv[2] > 0.0f) {
                hsv[2] -= 0.1f;
                layoutRef.setBackgroundColor(Color.HSVToColor(hsv));
            }
        }
        return true;
    }   // Lighten Darken Screen

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                Toast.makeText(getApplicationContext(),
                        "Did you miss the element?",
                        Toast.LENGTH_SHORT).show();
                return true;
            case (MotionEvent.ACTION_UP):
                Toast.makeText(getApplicationContext(),
                        "Stop playing with your phone! You have a botnet to manage!",
                        Toast.LENGTH_SHORT).show();
                return true;
            case (MotionEvent.ACTION_MOVE):
                changeBackgroundColour(event);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }       // Annoying hints

    public String addACity(Context context){

        String text = "";

        EditText textID = findViewById(etCityID);
        EditText textName = findViewById(etCityName);
        EditText textLat = findViewById(etLatitude);
        EditText textLng = findViewById(etLongitude);
        EditText textStatus = findViewById(etStatus);

        String idstr = textID.getText().toString();
        String name = textName.getText().toString();
        String lat = textLat.getText().toString();
        String lng = textLng.getText().toString();
        String statusstr = textStatus.getText().toString();

        int id = Integer.parseInt(idstr);
        int status = Integer.parseInt(statusstr);

        City newCity = new City(id, name, lat, lng, status);

        if ( citydb.getCity(id).getID() == id){
            text = " This city already exists";
        } else{
            citydb.addCity(newCity);
            text = " Adding city";
        }
        return text;
    }

    public String getACity(Context context){

        int id = 1;
        String text = "";

        EditText textID = findViewById(etCityID);
        EditText textName = findViewById(etCityName);
        EditText textLat = findViewById(etLatitude);
        EditText textLng = findViewById(etLongitude);
        EditText textStatus = findViewById(etStatus);

        TextView textIDView = findViewById(etCityID);
        TextView textNameView = findViewById(etCityName);
        TextView textLatView = findViewById(etLatitude);
        TextView textLngView = findViewById(etLongitude);
        TextView textStatusView = findViewById(etStatus);

        String idstr = textID.getText().toString();

        if (idstr.isEmpty()){
            text = "Enter a valid ID";
            return text;
        }

        id = Integer.parseInt(idstr);

        if (id > citydb.getAllCities().size()){
            text = "Enter a valid ID in the table";
            return text;
        }               // Input Validation

        City newCity = citydb.getCity(id);

        textIDView.setText(Integer.toString(newCity.getID()));
        textNameView.setText(newCity.getName());
        textLatView.setText(String.valueOf(newCity.getLatitude()));
        textLngView.setText(String.valueOf(newCity.getLongitude()));
        textStatusView.setText(Integer.toString(newCity.getStatus()));

        text = "The city " + newCity.getName() + " has been returned from the Database";

        return text;
    }

    public String updateACity(Context context){

        String text = "";

        EditText textID = findViewById(etCityID);
        EditText textName = findViewById(etCityName);
        EditText textLat = findViewById(etLatitude);
        EditText textLng = findViewById(etLongitude);
        EditText textStatus = findViewById(etStatus);

        String idstr = textID.getText().toString();
        String name = textName.getText().toString();
        String lat = textLat.getText().toString();
        String lng = textLng.getText().toString();
        String statusstr = textStatus.getText().toString();

        int id = Integer.parseInt(idstr);
        int status = Integer.parseInt(statusstr);

        City cityToUpdate = new City(id, name, lat, lng, status);

        int success = citydb.updateCity(cityToUpdate);

        if ( success == 1) {
            text = "The city " + cityToUpdate.getName() + "  was updated succesfully";
        }else{
            text = "The city" + cityToUpdate.getName() + " was not updated";
        }

        return text;
    }

    public void returnHome(){
        Intent intent = new Intent(OptionsActivity.this, MainActivity.class);
        startActivity(intent);
    }

}
