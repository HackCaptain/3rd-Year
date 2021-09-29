package com.example.botnets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class ContinueActivity extends AppCompatActivity implements

    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
        View.OnClickListener,
        OnSuccessListener<Location> {

        // Map Stuff
        private static final int LOCATION_REQUEST = 1;
        private GoogleMap miniMapView;
        private FusedLocationProviderClient fusedLocationClient;
        private LocationCallback locationCallback;

        private Marker markerMyLocation;
        private Marker markerRandomLocation;
        private Marker markerTargetLocation;

        // SQL Stuff
        public static final String DEBUG_TAG = "Botnet_Debug_Storage";
        //private SQLiteHelper sqLiteHelper;

        protected SQLiteHelper citydb = new SQLiteHelper(this);

        public static final String TAG = "Botnet_Debug";
        public static final String TAG_ENEMY = "Botnet_Debug_Enemy";
        public static final String TAG_SCAN = "Botnet_Debug_Scan";

        // The circles that represent dynamic scan radii (<- plural for "radius"... didn't have to google that)
        private Circle circleTarget;
        private Circle circleEnemy;

        // For formatting the numerous decimal places in this app
        private static DecimalFormat df = new DecimalFormat("0.00");

        // Buttons used in Continue Activity
        public Button buttonScan;
        public Button buttonEndTurn;

        // Default values for a game. (This is so ContinueGame doesn't crash on boot)
        int playableCities = 30;                // Default Game Params, nos cities
        int homeCities = 5;                     // Default number of home cities
        int winCondition = 16;                  // 1 more than half of default all
        boolean newgame = false;                // Allows the user to continue Activity


        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_continue);

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.miniMapView);
                mapFragment.getMapAsync(this);

                buttonScan = findViewById(R.id.buttonScanArea);
                buttonScan.setOnClickListener(this);
                buttonEndTurn = findViewById(R.id.buttonEnd);
                buttonEndTurn.setOnClickListener(this);

                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                        playableCities = extras.getInt("nosCities");
                        homeCities = extras.getInt("nosFriendlies");
                        newgame = extras.getBoolean("newGame");

                        Log.d(TAG, " Received, " + playableCities + " and " + homeCities + " from the previous activity, this will be a new game");
                }

                winCondition = (playableCities / 2) + 1 ;        // Default gamemode, own more than a half of the world to win, 33, 30 or 15 + 1
                Log.d(TAG, " The game will end when a botnet controls " + winCondition + " cities");

                createCities();         // Add cities to map from SQL database

                if (newgame) {

                        List<Integer> allIDs = new ArrayList<Integer>();
                        List<Integer> reservedIDs = new ArrayList<Integer>();

                        for (int h = 0; h < playableCities; h++) {
                                allIDs.add(h);
                        }
                        citydb.updateStatus(allIDs, 0);

                        List<Integer> friendlyIDs = new ArrayList<Integer>();
                        friendlyIDs = citydb.assignRandomCities(homeCities, playableCities, reservedIDs);
                        Log.d(TAG, " Random friendly cities pool: " + friendlyIDs.size() + " received");
                        citydb.updateStatus(friendlyIDs, 1);

                        // Ensure that an equal number of cities are distributed
                        for(int m = 0; m < friendlyIDs.size(); m++){
                                reservedIDs.add(friendlyIDs.get(m));
                                Log.d(TAG, " Adding random " + friendlyIDs.get(m) + " from friendly to reserved list");
                        }

                        List<Integer> enemyIDs = new ArrayList<Integer>();
                        enemyIDs = citydb.assignRandomCities(homeCities, playableCities, reservedIDs);
                        Log.d(TAG, " Random enemy cities pool: " + enemyIDs.size() + " received");
                        citydb.updateStatus(enemyIDs, 2);

//                        if(playableCities == 66) {
//                                reservedIDs.add(61);                                    // This is mainly for debugging, plus it gives you full control of Scotland
//                                reservedIDs.add(62);
//                                reservedIDs.add(63);
//                                reservedIDs.add(64);
//                                reservedIDs.add(65);
//                                citydb.updateStatus(reservedIDs, 1);
//                        }
                }                       // NewGame, will handle the reset of the sql Database

                int enemyCityCount = citydb.getCityCountByType(2);
                Log.d(TAG, "There are  : " + enemyCityCount + " enemy cities");

                int friendlyCityCount = citydb.getCityCountByType(1);
                Log.d(TAG, "There are  : " + friendlyCityCount + " friendly cities");


        }

        @Override
        protected void onStart() {
                super.onStart();

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                }
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, this);
        }       // Add location Callback

        @Override
        protected void onResume() {
                super.onResume();

                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setInterval(60000);             // 1 minute, up from 10 sec
                locationRequest.setFastestInterval(20000);      // 20 sec, up from 5 sec
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                                if (locationResult != null) {
                                        Log.d(TAG + " Current LatLng", locationResult.getLastLocation().toString());
                                        if (miniMapView != null) {
                                                double lat = locationResult.getLastLocation().getLatitude();
                                                double lng = locationResult.getLastLocation().getLongitude();
                                                LatLng myLatLng = new LatLng(lat, lng);
                                                markerMyLocation.setPosition(myLatLng);
                                        }
                                }
                        }
                };

                boolean checkResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (fusedLocationClient != null && checkResult) {
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }


        }       // Re - enable location Callback

        @Override
        protected void onPause() {
                super.onPause();
                if (fusedLocationClient != null && locationCallback != null) {
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                }
        }       // Remove location Callback


        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
                this.miniMapView = googleMap;

                // Customise the styling of the base map using a JSON object defined  in a raw resource file.
                try {
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        this, R.raw.google_style_json));
                        Log.d(TAG, "Style parsing was a success, custom style loaded.");

                        if (!success) {
                                Log.e(TAG, "Style parsing failed.");
                        }
                } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                }

                // Marker for Abertay, we dont need this however it helps with debugging
                LatLng abertay = new LatLng(56.4633, -2.9739);
                this.miniMapView.addMarker(new MarkerOptions()
                        .position(abertay)
                        .title("Abertay University")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));


                // Marker for user location, defaults at abertay before being changed
                markerMyLocation = this.miniMapView.addMarker(new MarkerOptions()
                        .position(abertay)
                        .title("BOTNET CONTROL \nMY LOCATION")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));


                // Marker for a random Location
                LatLng RandomLocation = createNearbyRandomLocation(abertay, 10000000);
                markerRandomLocation = this.miniMapView.addMarker(
                        new MarkerOptions()
                                .position(RandomLocation)
                                .title("Random Position")
                                .visible(false)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));


                // Marker for Target Icon Location
                final LatLng targetLocation = new LatLng(59, -4);
                markerTargetLocation = this.miniMapView.addMarker(
                        new MarkerOptions()
                                .position(targetLocation)
                                .title("Target Position")
                                .draggable(true)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                this.miniMapView.moveCamera(CameraUpdateFactory.zoomTo(3));
                this.miniMapView.moveCamera(CameraUpdateFactory.newLatLng(targetLocation));


                drawCircle(markerTargetLocation.getPosition(), getModDistance(1));

                drawEnemyCircle(markerRandomLocation.getPosition(), getModDistance(2));

                mapCities();                            // Update the map with all the marker

                // Set Listener for Clicks and Drags
                this.miniMapView.setOnMarkerClickListener(this);

                // Drag Listener, does stuff on start, during and end of drag
                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDragStart(Marker marker) {
                                Log.d(TAG, "Marker is being dragged");
                                updateCircleColors();                           // American spelling. Ikr.
                        }

                        @Override
                        public void onMarkerDrag(Marker marker) {
                                Log.d(TAG, "Tracking Marker");
                        }

                        @Override
                        public void onMarkerDragEnd(Marker marker) {
                                double distance = getModDistance(1);
                                double lat = 0.0;
                                double lng = 0.0;
                                lat = markerTargetLocation.getPosition().latitude;
                                lng = markerTargetLocation.getPosition().longitude;
                                Log.d(TAG, "Marker has stopped, " + lat + ". " + lng);
                                updateCircle(markerTargetLocation.getPosition(), distance);
                        }
                });

                /** Land detection, using get local feature. Doesn't work
                 It has been included here anyway should botnet decide to incorparate
                 ships or planes someday
                 */
                /*                 // Marker for a random location that is placed within a country
               boolean onLand = false;
               boolean flag = false;
               LatLng GeneratedLocation = null;
               while (flag != true) {
                        Location generated = new Location("Random Location");
                        GeneratedLocation = createNearbyRandomLocation(abertay, 10000000);
                        generated.setLatitude(GeneratedLocation.latitude);
                        generated.setLongitude(GeneratedLocation.longitude);
                        onLand = isLand(generated, this);
                        if (onLand == true){
                                flag = true;
                                Log.d(TAG, "Random Location is on Land.");
                        }
                        Log.d(TAG, "Random Location is not on Land.");
                }
                LatLng RandomLocation = GeneratedLocation;
*/
        }

        /** Called when the user clicks a marker. */
        @Override
        public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().equals("Abertay University")) {
                        Intent abertayIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.abertay.ac.uk"));
                        startActivity(abertayIntent);
                }
                if (marker.getTitle().equals("BOTNET CONTROL \nMY LOCATION")){
                        LatLng myLatLng = markerMyLocation.getPosition();
                        double lat = myLatLng.latitude;
                        double lng = myLatLng.longitude;

                        getGmap(lat, lng);
                }
                return false;
        }       // Could be modified to parse a URL about the city.

        @Override
        public void onSuccess(Location location) {
                if (location != null) {
                        String locationText = location.getLatitude() + ", " + location.getLongitude();
                        Toast.makeText(this, "Botnet receiving commands from: " + locationText, Toast.LENGTH_SHORT).show();
                }
                getAddressFromLocation(location, this, new GeocoderHandler());
        }       // Shows users initial Location

        /** This method is invoked whenever ANYTHING on
         * the map is clicked, this is good as it updates there and then.
         * For example the draggable target marker */
        @Override
        public void onClick(View view) {

                double distance = getModDistance(1);

                onResume();     // Update Control Marker

                Location randomLocation = new Location("Default randomLocation");
                LatLng randoLatLng = markerRandomLocation.getPosition();
                randomLocation.setLatitude(randoLatLng.latitude);
                randomLocation.setLongitude(randoLatLng.longitude);

                Location targetLocation = new Location("Default targetLocation");
                LatLng targetLatLng = markerTargetLocation.getPosition();
                targetLocation.setLatitude(targetLatLng.latitude);
                targetLocation.setLongitude(targetLatLng.longitude);

                String text = "";

                switch (view.getId()) {
                        case R.id.buttonScanArea:
                                ((Button)findViewById(R.id.buttonScanArea)).setEnabled(false);  // Disable user from hitting
                                ((Button)findViewById(R.id.buttonScanArea)).setBackgroundColor(0xff000700);     // Light Green
                                winnerYet();    // Check for a winner

                                text = "Scanning the designated Area for interesting Targets";
                                getAddressFromLocation(targetLocation, this, new GeocoderHandler());
                                Toast.makeText(getApplicationContext(), distance+"", Toast.LENGTH_LONG).show();

                                List<City> returnedCities =  withinRadius(markerTargetLocation, distance, 1);
                                updateCircleScan(targetLatLng, distance);
                                Log.d(TAG, "Returned " + returnedCities.size() + " cities within radius");

                                int k = showpopup(returnedCities, this);

                                text = updateCounter(2, view);

                                ((Button)findViewById(R.id.buttonEnd)).setEnabled(true);
                                ((Button)findViewById(R.id.buttonEnd)).setBackgroundColor(0x99007700);     // Dark Green
                                break;

                        case R.id.buttonEnd:
                                ((Button)findViewById(R.id.buttonEnd)).setEnabled(false);
                                ((Button)findViewById(R.id.buttonEnd)).setBackgroundColor(0xff000700);     // Light Green
                                winnerYet();    // Check for a winner

                                circleEnemy.remove();                   // Remove previous circle

                                text = enemyGuess(this);                // Get the activity detected string

                                updateEnemyCircle(targetLatLng, distance);                                      // move enemy circle
                                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();        // Make the activity detected toast

                                text = updateCounter(1, view);

                                ((Button)findViewById(R.id.buttonScanArea)).setEnabled(true);
                                ((Button)findViewById(R.id.buttonScanArea)).setBackgroundColor(0x99007700);     // Dark Green
                                break;
                }


                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }


        // METHODS (stuff that makes the app work) -----------------------------------------------------------------------

        private void returnToMain(){
                citydb.wipeTable();
                Intent intent = new Intent(ContinueActivity.this, MainActivity.class);
                startActivity(intent);
        }       // Used at the end of the game to clear the table for next game

        public String updateCounter(int type, View view){

                String[] cityType = {"neutral", "Friendly", "Enemy"};
                int counter = 0;
                String text = "";

                TextView textCount = findViewById(R.id.textCounter);

                if(type == 1){
                        counter = citydb.getCityCountByType(1);
                        text = cityType[type] + " : Cities : \n\n" + counter;
                }
                if(type == 2){
                        counter = citydb.getCityCountByType(2);
                        text =  cityType[type] + " Cities : \n\n" + counter;
                }
                Log.d(TAG, cityType[type] +  " Returned for text " + counter);

                textCount.setText(text);

                return text;
        }       // Counts number of cities by type

        public void winnerYet(){
                AlertDialog.Builder builder;

                int friendlyCount = citydb.getCityCountByType(1);
                int enemyCount = citydb.getCityCountByType(2);
                String player = "";

                if((friendlyCount > winCondition || enemyCount > winCondition) ||
                        (friendlyCount == 0 || enemyCount == 0)) {

                        if(friendlyCount > winCondition || enemyCount == 0){
                                player = "YOU";
                        }
                        else if(enemyCount > winCondition || friendlyCount == 0){
                                player = "The enemy";
                        }       // This might always be ture... until game is initialised through newGame

                        builder = new AlertDialog.Builder(this);
                        builder.setMessage(player + " won the game!!!")
                                .setTitle("A BOTNET HAS CRASHED THE INTERNET... FINALLY")
                                .setCancelable(false)
                                .setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                                finish();
                                                Toast.makeText(getApplicationContext(),"Returning",
                                                Toast.LENGTH_SHORT).show();
                                                returnToMain();         // Wipe tables, go home
                                        }
                                })
                                .setNegativeButton("RETURN", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                                //  Action for 'NO' Button
                                                dialog.cancel();
                                                Toast.makeText(getApplicationContext(),"The internet is under your control",
                                                        Toast.LENGTH_SHORT).show();
                                        }
                                });
                        AlertDialog alert = builder.create();
                        alert.setTitle("A BOTNET NOW CONTROLS THE WORLD");
                        alert.show();

                        Log.d(TAG, player + " won the game!!!");
                }

        }       // Checks if the above counter has reached a win condition

        public String enemyGuess(Context context){                              // Returns a string for toasting... mmm.
                String text = "";
                int randomDistance = 0;
                List<City> enemyCities = new ArrayList<City>();
                int factor = citydb.getCityCountByType(1);
                int nosEnemyCities = citydb.getCityCountByType(2);

                // Decide which type of city to attack. E.g:
                // If more than 2/3 of the win condition are friendly, or friendly count
                // is critical, be offensive. Else play passively
                if((factor > 2 * (winCondition / 3) || nosEnemyCities < 5)){
                        enemyCities = citydb.getCitiesByType(1);
                }
                else {            // else select neutral city
                        enemyCities = citydb.getCitiesByType(0);
                }

                // Get Enemy Cities and associated radius----

                City enemyCity = new City();
                int radius = (int) getModDistance(2);
                Log.d(TAG_ENEMY, "There are currently : " + nosEnemyCities + " enemy cities, Improving the scan radius for the enemy");

                // Pick random city--------------------------

                Random numberGenerator = new Random();                                                  // Initialise a new random number generator
                int randomNumber = numberGenerator.nextInt(enemyCities.size());                         // Generate the number of a hostile city
                Log.d(TAG_ENEMY, "Random city ID selected successfully: " + randomNumber);
                enemyCity = enemyCities.get(randomNumber);                                              // Grab the details of that city
                Log.d(TAG_ENEMY, "Random city selected successfully: " + enemyCity.getName() + ", ID:" + randomNumber);
                LatLng enemyLatLng = new LatLng(enemyCity.getLatitude(), enemyCity.getLongitude());     // Use that city as the base for random location generation

                // Guess in radius at random point from city----------

                Marker enemyTargetMarker;
                randomDistance = numberGenerator.nextInt(100) * 10000;

                Log.d(TAG_ENEMY, "Generating a random point " + randomDistance + " away from "
                        + df.format(enemyCity.getLatitude()) + ". " + df.format(enemyCity.getLongitude())
                        + " : " + enemyCity.getName());

                LatLng enemyTarget = createNearbyRandomLocation(enemyLatLng, randomDistance);   // Generate a random point at a random distance from a defined point
                enemyTargetMarker = this.miniMapView.addMarker(
                        new MarkerOptions()
                                .position(enemyTarget)
                                .title("Enemy Focus")
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));   // Adds a marker for debugging purposes, this appears for millisecond

                drawEnemyCircle(enemyTarget, getModDistance(2));

                // Assimilate cities if any

                List<City> citiesToAssimilateEnemy = new ArrayList<City>();

                /*
                TODO add a targeting routine? A variation of withinRadius.
                TODO If within radius > 0 && status != 2.
                TODO This would ensure a hit on a secondary valid target by the bot.
                 */

                citiesToAssimilateEnemy = withinRadius(enemyTargetMarker, radius, 2);

                List<Integer> IDToAssimilateEnemy = new ArrayList<Integer>();

                for (int h = 0; h < citiesToAssimilateEnemy.size(); h++){
                        IDToAssimilateEnemy.add((citiesToAssimilateEnemy.get(h)).getID());
                }

                citydb.updateStatus(IDToAssimilateEnemy, 2);

                enemyTargetMarker.remove();

                mapCities();

                Location enemyActivity = new Location("Enemy Activity");
                enemyActivity.setLatitude(enemyCity.getLatitude());
                enemyActivity.setLongitude(enemyCity.getLongitude());
                getAddressFromLocation(enemyActivity, this, new GeocoderHandler());

                text = " Activity detected within a " + radius + "m radius of the point: "
                        + df.format(enemyCity.getLatitude()) + ", " + df.format(enemyCity.getLongitude());

                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                // Remove base city from roster
                return text;
        }    // Decides what city the enemy should attack

        public List<City> withinRadius(Marker givenMarker, double radius, int type){

                List<City> citiesfromdb = citydb.getAllCities();
                List<City> citiesReturn = new ArrayList<City>();
                int cityAmount = citiesfromdb.size();
                double distance = 0.0;
                String[] cityType = {"neutral", "friendly", "enemy"};
                String[] inOut = {"OUTSIDE", "INSIDE"};
                int flag = 0;
                double lat, lng;
                lat =givenMarker.getPosition().latitude;
                lng =givenMarker.getPosition().longitude;
                Log.d(TAG_SCAN, "Target Marker is at: " + lat + ". " + lng);

                Log.d(TAG_SCAN, cityAmount + " total cities, checking within distance: " + distance);

                if(radius == 0.0){                                              // Does not cause an error, but can cause the circle to disappear
                        Log.d(TAG_SCAN, " Button hit before marker moved");
                        return citiesReturn;
                }

                for (int y = 0; y < cityAmount; y++){

                        City currentCity = citiesfromdb.get(y);

                        Log.d(TAG_SCAN, "Looking at : " + currentCity.getName());

                        int id = y; //currentCity.getID();
                        String name = currentCity.getName();
                        float latToCheck = currentCity.getLatitude();
                        float lngToCheck = currentCity .getLongitude();
                        int status = currentCity.getStatus();

                        distance = calcDistance(lat, lng, latToCheck, lngToCheck);

                        Log.d(TAG_SCAN, y + " : Given Marker at: " + df.format(lat) + ". " + df.format(lng) + ", Checking for " + df.format(latToCheck) + ". "
                                + df.format(lngToCheck) + " within radius: " + radius + " distance retrieved was " + distance);

                        if(distance > radius){
                                flag = 0;
                                //Log.d(TAG, " The " + cityType[status]  + "city :" + id  + ", " + name + " is outside of the radius.");
                        }
                        else if (distance < radius){
                                citiesReturn.add(currentCity);
                                flag = 1;
                        }
                        Log.d(TAG_SCAN, "The " + cityType[status]  + " city :" + id  + ", " + name + " is " + inOut[flag] + " of the radius.");
                }
                return  citiesReturn;
        }       // Checks if city within radius

        private void createCities(){
                SQLiteHelper sqLiteHelper;

                /** This could in time be developed further to only upload a certain type or selection
                 Saving space when the game is running. E.G:
                 specify all cities in Europe, only cities from add-on (maybe)
                 */

                // Obtain Instance of SQL DB and deal with Storage Perms
                sqLiteHelper = SQLiteHelper.getInstance(getApplicationContext());
                int check1 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                int check2 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);



                boolean needToAsk = !((check1 == PackageManager.PERMISSION_GRANTED) & (check2 == PackageManager.PERMISSION_GRANTED));

                if(needToAsk){
                        requestPermissions(new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 0);
                }

                Thread threadAddCities = new Thread() {
                        @Override
                        public void run() {

                                Log.d(DEBUG_TAG, "City adding Thread running, Inserting ..");

                                if (playableCities > 0) {
                                        // Europe
                                        citydb.addCity(new City(1, "London", "51.509865", "-0.118092", 0));
                                        citydb.addCity(new City(2, "Paris", "48.864716", "2.349014", 0));
                                        citydb.addCity(new City(3, "Berlin", "52.520008", "13.404954", 0));
                                        citydb.addCity(new City(4, "Warsaw", "52.237049", "21.017532", 0));
                                        citydb.addCity(new City(5, "Lisbon", "38.736946", "-9.142685", 0));
                                        citydb.addCity(new City(6, "Madrid", "40.416775", "-3.703790", 0));
                                        citydb.addCity(new City(7, "Rome", "41.902782", "12.496366", 0));
                                        citydb.addCity(new City(8, "Moscow", "55.751244", "37.618423", 0));
                                        citydb.addCity(new City(9, "Athens", "37.983810", "23.727539", 0));
                                        citydb.addCity(new City(10, "Kiev", "50.431759", "30.517023", 0));

                                        // North America
                                        citydb.addCity(new City(11, "Mexico City", "19.432608", "-99.133209", 0));
                                        citydb.addCity(new City(12, "Ney York City", "40.730610", "-73.935242", 0));
                                        citydb.addCity(new City(13, "Los Angeles", "34.052235", "-118.243683", 0));
                                        citydb.addCity(new City(14, "Toronto", "43.651070", "-79.347015", 0));
                                        citydb.addCity(new City(15, "Chicago", "41.881832", "-87.623177", 0));
                                        citydb.addCity(new City(16, "Houston", "29.749907", "-95.358421", 0));
                                        citydb.addCity(new City(17, "Havana", "23.113592", "-82.366592", 0));
                                        citydb.addCity(new City(18, "Ecatepec", "19.609722", "-99.059998", 0));
                                        citydb.addCity(new City(19, "Montreal", "45.508888", "-73.561668", 0));
                                        citydb.addCity(new City(20, "Philadelphia", "39.952583", "-75.165222", 0));

                                        // Asia
                                        citydb.addCity(new City(21, "Tokyo", "35.652832", "139.839478", 0));
                                        citydb.addCity(new City(22, "Delhi", "28.644800", "77.216721", 0));
                                        citydb.addCity(new City(23, "Shanghai", "31.224361", "121.469170", 0));
                                        citydb.addCity(new City(24, "Beijing", "39.916668", "116.383331", 0));
                                        citydb.addCity(new City(25, "Abu Dhabi", "24.466667", "54.366669", 0));
                                        citydb.addCity(new City(26, "Jerusalem", "31.771959", "35.217018", 0));
                                        citydb.addCity(new City(27, "Dhaka", "23.777176", "90.399452", 0));
                                        citydb.addCity(new City(28, "Pyongyang", "39.019444", "125.738052", 0));
                                        citydb.addCity(new City(29, "Seoul", "37.532600", "127.024612", 0));
                                        citydb.addCity(new City(30, "Bangkok", "13.736717", "100.523186", 0));

                                }
                                if (playableCities > 30) {

                                        // Africa
                                        citydb.addCity(new City(31, "Lagos", "6.465422", "3.406448", 0));
                                        citydb.addCity(new City(32, "Kinshasa", "-4.322447", "15.307045", 0));
                                        citydb.addCity(new City(33, "Cairo", "30.033333", "31.233334", 0));
                                        citydb.addCity(new City(34, "Alexandria", "31.205753", "29.924526", 0));
                                        citydb.addCity(new City(35, "Abidjan", "5.345317", "-4.024429", 0));
                                        citydb.addCity(new City(36, "Kano", "12.000000", "8.516667", 0));
                                        citydb.addCity(new City(37, "Ibadan", "7.376736", "3.939786", 0));
                                        citydb.addCity(new City(38, "Cape Town", "-33.918861", "18.423300", 0));
                                        citydb.addCity(new City(39, "Casablanca", "33.589886", "-7.603869", 0));
                                        citydb.addCity(new City(40, "Durban", "-29.883333", "31.049999", 0));

                                        // South America
                                        citydb.addCity(new City(41, "Sao Paulo", "-23.533773", "-46.625290", 0));
                                        citydb.addCity(new City(42, "Lima", "-12.046374", "-77.042793", 0));
                                        citydb.addCity(new City(43, "Bogota", "4.624335", "-74.063644", 0));
                                        citydb.addCity(new City(44, "Rio de Janeiro", "-22.970722", "-43.182365", 0));
                                        citydb.addCity(new City(45, "Santiago", "-33.447487", "-70.673676", 0));
                                        citydb.addCity(new City(46, "Caracas", "10.482031", "-66.916664", 0));
                                        citydb.addCity(new City(47, "Buenos Aires", "-34.603722", "-58.381592", 0));
                                        citydb.addCity(new City(48, "Salvador", "-12.974722", "-38.476665", 0));
                                        citydb.addCity(new City(49, "Brasilia", "-15.793889", "-47.882778", 0));
                                        citydb.addCity(new City(50, "Fortaleza", "-3.731862", "-38.526669", 0));

                                        // Oceania
                                        citydb.addCity(new City(51, "Manila", "14.599512", "120.984222", 0));
                                        citydb.addCity(new City(52, "Jakarta", "-6.225588", "106.798553", 0));
                                        citydb.addCity(new City(53, "Sydney", "-33.865143", "151.209900", 0));
                                        citydb.addCity(new City(54, "Melbourne", "-37.815018", "144.946014", 0));
                                        citydb.addCity(new City(55, "Auckland", "-36.848461", "174.763336", 0));
                                        citydb.addCity(new City(56, "Port Moresby", "-9.4431000", "147.179700", 0));
                                        citydb.addCity(new City(57, "Perth", "-31.953512", "115.857048", 0));
                                        citydb.addCity(new City(58, "Suva", "-18.141600", "178.441895", 0));
                                        citydb.addCity(new City(59, "McMurdo Station", "-77.846323", "166.668235", 0));   //Threw this in, why not. bit of a cheat
                                        citydb.addCity(new City(60, "Honolulu", "21.315603", "-157.858093", 0));

                                }
                                if (playableCities > 60) {

                                        // test Cities
                                        // Scotland and "Atlantis" really

                                        citydb.addCity(new City(61, "Dundee", "56.462002", "-2.970700", 0));
                                        citydb.addCity(new City(62, "Edinburgh", "55.953251", "-3.188267", 0));
                                        citydb.addCity(new City(63, "Aberdeen", "57.149651", "-2.099075", 0));
                                        citydb.addCity(new City(64, "Glasgow", "55.860916", "-4.251433", 0));
                                        citydb.addCity(new City(65, "Inverness", "57.477772", "-4.224721", 0));
                                        citydb.addCity(new City(66, "Test City", "0", "0", 0));
                                }

                                        /*
                                        TODO add more cities?
                                        TODO Find a better way of adding LatLngs for location - Geocoder can be slow
                                        */
                        }
                };
                threadAddCities.start();
                try{
                        threadAddCities.join();
                        Log.d(TAG, " !SUCCESS! City adding thread returned to main thread");
                } catch(InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error waiting for city adding thread to join main thread");
                }
        }       // Uploads cities to DB for each new game

        private void mapCities(){
                // Get all the locations from the SQL database, and add to map
                List<City> cities = citydb.getAllCities();
                int nosCities = citydb.getCityCount();
                Log.d(DEBUG_TAG, "Adding Cities (" + nosCities + ") to Map");
                for (int x = 0; x < nosCities; x++) {

                        City currentCity = cities.get(x);

                        int id = currentCity.getID();
                        String name = currentCity.getName();
                        float Lat = currentCity.getLatitude();
                        float Lng = currentCity.getLongitude();
                        int status = currentCity.getStatus();


                        if (status == 0) {
                                miniMapView.addMarker(new MarkerOptions()
                                        .visible(true)
                                        .title(name)
                                        .position(new LatLng(Lat, Lng))
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                        } else if (status == 1) {
                                miniMapView.addMarker(new MarkerOptions()
                                        .visible(true)
                                        .title(name)
                                        .position(new LatLng(Lat, Lng))
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        } else if (status == 2) {
                                miniMapView.addMarker(new MarkerOptions()
                                        .visible(true)
                                        .title(name)
                                        .position(new LatLng(Lat, Lng))
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                }
        }       // Adds the cities to the map from within the database

        /* Code predominantly from outside, mainly stack-overflow. Most of it has been heavily modified to be compatible  */

        // Circle stuff ----------------------------------------------------------O
        private void updateCircle(LatLng position, double distance){

                circleTarget.setCenter(position);
                circleTarget.setRadius(distance);
                circleTarget.setFillColor(0x44004444);         //green outline
                circleTarget.setStrokeColor(0xff00ff00);        //opaque aqua fill
                circleTarget.setStrokeWidth(8);
                Log.d(TAG, "Updating Circle");
        }

        private void updateCircleColors(){
                circleTarget.setFillColor(0x11110000);          // dark red fill
                circleTarget.setStrokeColor(0xBBBBFF00);        // Warm yellow opaque outline
                Log.d(TAG, "Updating Circle Colors");
        }       // Called when the Target Marker moves awy from the centre of the circle

        private void drawCircle(LatLng position, double distance){
                double radiusInMeters = distance;
                int strokeColor = 0xff00ff00; //green outline
                int shadeColor = 0x44004444; //opaque aqua fill

                CircleOptions circleOptions = new CircleOptions()
                        .center(position).radius(radiusInMeters)
                        .fillColor(shadeColor)
                        .strokeColor(strokeColor)
                        .strokeWidth(8);

                circleTarget= miniMapView.addCircle(circleOptions);
                Log.d(TAG, "Drawing Circle");
        }

        private void updateCircleScan(LatLng position, double radius){


                int strokeColor = 0xff00ffff; //cyan outline
                int shadeColor = 0x44000044; //opaque blue fill

                circleTarget.setRadius(radius);
                circleTarget.setFillColor(shadeColor);
                circleTarget.setStrokeColor(strokeColor);
                circleTarget.setStrokeWidth(20);
                Log.d(TAG, "Updating Circle with scanner colours");

        }

        private void drawEnemyCircle(LatLng position, double distance){

                double radiusInMeters = distance;
                int strokeColor = 0xffff0000; //red outline
                int shadeColor = 0x44ff0000; //opaque red fill

                CircleOptions enemyCircleOptions = new CircleOptions()
                        .center(position)
                        .radius(radiusInMeters)
                        .fillColor(shadeColor)
                        .strokeColor(strokeColor)
                        .strokeWidth(8);

                circleEnemy = miniMapView.addCircle(enemyCircleOptions);
                Log.d(TAG, "Drawing Circle");
        }

        private void updateEnemyCircle(LatLng position, double radius){

                circleTarget.setCenter(position);
                circleEnemy.setRadius(radius);
                circleEnemy.setStrokeWidth(20);
                Log.d(TAG, "Updated Enemy Circle with new activity location");

        }
        // End of Circle stuff ---------------------------------------------------O

        public double getModDistance(int type){

                String[] cityType = {"Neutral", "Friendly", "Enemy"};

                double distanceOriginal = 500000;
                double distanceMultiplier = 10000 * citydb.getCityCountByType(type);
                double distance = distanceOriginal + distanceMultiplier;

                Log.d(TAG, cityType[type] + " cities : " + citydb.getCityCountByType(type) + ", scan radius has increased by "
                        + distanceMultiplier + ", total scan radius is now: " + distance);

                return distance;
        }       // Modified Distance for scan radius, based on number cities

        public int showpopup(List<City> cities, Context context) {

                int capturedTally = 1;
                double distance = getModDistance(1);
                List<Integer> IDsToAssimilate = new ArrayList<Integer>();

                if (cities.size() > 0) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
                        builder.setTitle("Choose cities to assimilate into the Botnet");

                        List<Integer> selectedID = new ArrayList<Integer>();

                        String names[] = new String[cities.size()];

                        List<String> withdrawnNames = new ArrayList<String>();
                        boolean checkedItems[] = new boolean[cities.size()];

                        for (int d = 0; d < cities.size(); d++) {
                                withdrawnNames.add((cities.get(d)).getName());
                                //Log.d(TAG, (cities.get(d)).getName());
                                names[d] = (withdrawnNames.get(d));
                                //Log.d(TAG, withdrawnNames.get(d));
                                checkedItems[d] = false;
                        }       // Get names via ID, from selected cities

                        builder.setMultiChoiceItems(names, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        // user checked or unchecked a box
                                        if (isChecked){
                                                selectedID.add(which);
                                                //Log.d(TAG, "Id ticked was " + which);
                                        }

                                }
                        });

                        // add OK and Cancel buttons
                        builder.setPositiveButton("Assimilate", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                        for (int f = 0; f < selectedID.size(); f++) {                       // For each city
                                                for (int e = 0; e < cities.size(); e++) {                       // For each city
                                                        //Log.d(TAG, "Id added was " + selectedID.get(f) + " checking for match with e: " + e);
                                                        if (selectedID.get(f) == e) {
                                                                IDsToAssimilate.add((cities.get(e)).getID());
                                                                Log.d(TAG, "The selected city: " + selectedID.get(f) + " is actually: " + (cities.get(e)).getID()
                                                                        + " on the list. NAME : " + (cities.get(e)).getName());
                                                        }
                                                }
                                        }
                                        for (int e = 0; e < IDsToAssimilate.size(); e++) {
                                                Log.d(TAG, "Cities status will be updated: " + IDsToAssimilate.get(e));
                                        }
                                        citydb.updateStatus(IDsToAssimilate, 1);
                                        mapCities();
                                }
                        });

                        builder.setNegativeButton("Back to Map", null);

                        // create and show the alert dialog
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        // Granted, could use a custom style, but there seems to be an issue with the wrapper
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFFFF0000);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF00FF00);


                }
                Log.d(TAG, IDsToAssimilate.size() + " cities identified for ASSIMILATION!");
                return IDsToAssimilate.size();
        } // This creates a dialogue box where the user can select what cities to assimilate.

        public void getGmap(double lat, double lng){

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
                builder.setTitle("Leave the current activity and view Local Map?");

                builder.setPositiveButton("LEAVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                Intent getLocalInfo = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lng));
                                startActivity(getLocalInfo);
                        }
                });
                builder.setNegativeButton("RETURN", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();

                // Granted, could use a custom style, but there seems to be an issue with the wrapper
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFF0000FF);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFFFFF00);
        }

        // Distance function, between to points
        private double calcDistance(double latitude_a, double longitude_a, double latitude_b, double longitude_b) {

                float lat_a = (float)latitude_a;
                float lng_a = (float)longitude_a;
                float lat_b = (float)latitude_b;
                float lng_b = (float)longitude_b;

                float pk = (float) (180.f/Math.PI);

                float a1 = lat_a / pk;
                float a2 = lng_a / pk;
                float b1 = lat_b / pk;
                float b2 = lng_b / pk;

                double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
                double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
                double t3 = Math.sin(a1) * Math.sin(b1);
                double tt = Math.acos(t1 + t2 + t3);

                return 6366000 * tt;
        }       // Used for calculaing distance between points

        /* Geocoder to get city address. This should hopefully be used in conjuction with the target area function
         for instance, should a player made guess detect that "London" is nearby. */
        public static void getAddressFromLocation(
                final Location location, final Context context, final Handler handler) {
                Thread thread = new Thread() {
                        @Override
                        public void run() {
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                String result = null;
                                try {
                                        List<Address> list = geocoder.getFromLocation(
                                                location.getLatitude(), location.getLongitude(), 1);
                                        if (list != null && list.size() > 0) {
                                                Address address = list.get(0);
                                                // sending back first address line and locality
                                                result = address.getAddressLine(0)
                                                        + ", " + address.getLocality();
                                        }
                                } catch (IOException e) {
                                        Log.e(TAG, "Impossible to connect to Geocoder", e);
                                } finally {
                                        Message msg = Message.obtain();
                                        msg.setTarget(handler);
                                        if (result != null) {
                                                msg.what = 1;
                                                Bundle bundle = new Bundle();
                                                bundle.putString("address", result);
                                                msg.setData(bundle);
                                        } else
                                                msg.what = 0;
                                        msg.sendToTarget();
                                }
                        }
                };
                thread.start();
                // Whilst having a catch case and join here might be considered proper, the application freezes if the geocoder fails to connect.
//                try{
//                        thread.join();
//                        Log.d(TAG, " !SUCCESS! Geocoder thread has returned to the main thread");
//                } catch(InterruptedException e) {
//                        e.printStackTrace();
//                        Log.e(TAG, "Error waiting for geocoder thread to join main thread");
//                }

        }       // This holds the geocoder, the handler receives the address sent back.

        // Geocoder Handler
        private class GeocoderHandler extends Handler {
                @Override
                public void handleMessage(Message message) {
                        String result;
                        switch (message.what) {
                                case 1:
                                        Bundle bundle = message.getData();
                                        result = bundle.getString("address");
                                        break;
                                default:
                                        result = null;
                        }
                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                        TextView address = (TextView) findViewById(R.id.textAddress);
                        address.setText( getString(R.string.TextViewAddress) + result);
                }
        }

        // Create a nearby Location, used for debugging early on Hostile NPC could use this to "target" cities
        public LatLng createNearbyRandomLocation(LatLng point, int radius) {
                List<LatLng> randomPoints = new ArrayList<>();
                List<Float> randomDistances = new ArrayList<>();
                Location myLocation = new Location("");
                myLocation.setLatitude(point.latitude);
                myLocation.setLongitude(point.longitude);

                //This is to generate 10 random points
                for (int i = 0; i < 10; i++) {
                        double x0 = point.latitude;
                        double y0 = point.longitude;

                        Random random = new Random();

                        // Convert radius from meters to degrees
                        double radiusInDegrees = radius / 111000f;

                        double u = random.nextDouble();
                        double v = random.nextDouble();
                        double w = radiusInDegrees * Math.sqrt(u);
                        double t = 2 * Math.PI * v;
                        double x = w * Math.cos(t);
                        double y = w * Math.sin(t);

                        // Adjust the x-coordinate for the shrinking of the east-west distances
                        double new_x = x / Math.cos(y0);

                        double foundLatitude = new_x + x0;
                        double foundLongitude = y + y0;
                        LatLng randomLatLng = new LatLng(foundLatitude, foundLongitude);
                        randomPoints.add(randomLatLng);
                        Location l1 = new Location("");
                        l1.setLatitude(randomLatLng.latitude);
                        l1.setLongitude(randomLatLng.longitude);
                        randomDistances.add(l1.distanceTo(myLocation));
                }
                //Get nearest point to the centre
                int indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances));

                //getAddressFromLocation(myLocation, this, new GeocoderHandler());

                // TODO INSERT CODE TO DETECT IF THE RANDOM LOCATION IS VALID (ON LAND)
                // Attempted; results inconclusive, more research required

                return randomPoints.get(indexOfNearestPointToCentre);
        }

        /** Buggy attempt at correcting for random guess to land in the ocean*/
        //        public static Boolean isLand(final Location location, final Context context) {
//                Geocoder getCountry = new Geocoder(context, Locale.getDefault());
//                boolean isLand = false;
//                try {
//                        List<Address> land = getCountry.getFromLocation(
//                                location.getLatitude(), location.getLongitude(), 1);
//                        String Country =land.get(0).getCountryName();
//                        if (Country != null) {
//                                // could add code here to determine if its in a list of selected countries
//                                isLand = true;
//                        }
//                } catch (IOException e) {
//                        Log.e(TAG, "Impossible to connect to Geocoder", e);
//                        Log.d(TAG, "Random Location generated was not in a country in water");
//                }
//                return isLand;
//        }
}