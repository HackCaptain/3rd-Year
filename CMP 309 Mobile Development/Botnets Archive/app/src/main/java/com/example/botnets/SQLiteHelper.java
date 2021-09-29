package com.example.botnets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BOTNETS_DB";
    private static final String TABLE_NAME = "Cities";
    public static final String DEBUG_UPLOAD_TAG = "Botnet_Debug_Storage, Uploaded ";
    private static final String[] COLUMN_NAMES = {"ID", "Name", "Latitude", "Longitude", "Status" };

    public static final String TAG = "Botnet_Debug_SQL";

    private static SQLiteHelper instance = null;

    // Build a table creation query string
    private String createCreateString(String[] colNames){
        String s = "CREATE TABLE " + TABLE_NAME + " (";
        for (int i = 0; i < colNames.length; i++) {
            s += colNames[i] + " TEXT";
            if(i < colNames.length - 1){
                s+= ", ";
            } else {
                s+= ");";
            }
        }
        return s;
    }

    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SQLiteHelper getInstance(Context context){
        if(instance == null){
            instance = new SQLiteHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createCreateString(COLUMN_NAMES));
        Log.d(TAG, "Created a table in the DB!!!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.e(TAG, "Updating table from " + i + " to " + i1);

        // if(1 == 1) {                    //EDIT DO THIS AND CRASH THE APP: 1=1 ALL THE TIME
        // Drop Older Table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create new table
        onCreate(db);
        // }
    }

    // used for cleanup
    public void wipeTable(){

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tables = new ArrayList<>();

        // iterate over the result set, adding every table name to a list
        while (c.moveToNext()) {
            tables.add(c.getString(0));
        }

        // call DROP TABLE on every table name
        for (String table : tables) {
            String dropQuery = "DROP TABLE IF EXISTS " + table;
            db.execSQL(dropQuery);
        }
        Log.d(TAG, "Database wiped, creating a new table");

        db.execSQL(createCreateString(COLUMN_NAMES));


    }

    // Using the city helper class, add a city to db.
    public void addCity(City city){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues row = new ContentValues();

        // Id check before adding more cities
        int idCheck = city.getID();
        String countQuery = " SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAMES[0] + " = " + idCheck;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        if( count > 0) {
            //Log.d(TAG, "There already appears to be a city with the ID : " + idCheck + ", " + city.getName());
            return;
        }

        row.put(COLUMN_NAMES[0], city.getID());
        row.put(COLUMN_NAMES[1], city.getName());
        row.put(COLUMN_NAMES[2], city.getLatitude());
        row.put(COLUMN_NAMES[3], city.getLongitude());
        row.put(COLUMN_NAMES[4], city.getStatus());

        // Inserting Row
        db.insert(TABLE_NAME, null, row);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection

        Log.d(DEBUG_UPLOAD_TAG, city.getID() + " , " + city.getName() + ", " + city.getLatitude() +" , " + city.getLongitude() + " , " + city.getStatus());
    }

    // Add all cities to a list
    public List<City> getAllCities(){

        List<City> cityList = new ArrayList<City>();

        // SELECT everything FROM table
        String selectQuery = " SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Thread threadGetCities = new Thread() {
            @Override
            public void run() {
                // Loop through the table
                if (cursor.moveToFirst()) {
                    do {
                        City city = new City();
                        city.setID(Integer.parseInt(cursor.getString(0)));
                        city.setName(cursor.getString(1));
                        city.setLatitude(cursor.getString(2));
                        city.setLongitude(cursor.getString(3));
                        city.setStatus(Integer.parseInt(cursor.getString(4)));
                        //Log.d(TAG, "City counter found  : " + cursor.getString(0));
                        cityList.add(city);
                    } while (cursor.moveToNext());
                }
            }
        };
        threadGetCities.start();
        try{
            threadGetCities.join();
            Log.d(TAG, " !SUCCESS! City getting thread returned to main thread");
        } catch(InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "Error waiting for city adding thread to join main thread");
        }
        Log.d(TAG, "City counter found  : " + cityList.size() + "  cities");
        return cityList;
    }

    // Count cities... useful for debugging.
    public int getCityCount(){
        String countQuery = " SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        //Log.d(TAG, "There are : " + count + " cities.");
        cursor.close();
        db.close();
        // return count
        return count;
    }

    // Count a type of by status... this is used to determine the overall strength of a botnet
    public int getCityCountByType(int type){
        String[] cityType = {"neutral", "friendly", "enemy"};

        String countQuery = " SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAMES[4] + " = " + type;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        Log.d(TAG, "There are : " + count + " " + cityType[type] + "  cities.");
        cursor.close();
        db.close();
        return count;
    }

    // Return a type of by status
    public List<City> getCitiesByType(int type){

        List<City> cityList = new ArrayList<City>();
        String[] cityType = {"neutral", "friendly", "enemy"};

        String selectCityQuery = " SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAMES[4] + " = " + type;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectCityQuery, null);

        if(cursor.moveToFirst()){
            do {
                City city = new City();
                city.setID(Integer.parseInt(cursor.getString(0)));
                city.setName(cursor.getString(1));
                city.setLatitude(cursor.getString(2));
                city.setLongitude(cursor.getString(3));
                city.setStatus(Integer.parseInt(cursor.getString(4)));

                cityList.add(city);
            }while (cursor.moveToNext());
        }

        int count = cursor.getCount();
        Log.d(TAG, "Returning : " + count + " " + cityType[type] + "  cities.");
        cursor.close();
        db.close();

        return cityList;
    }

    public List<Integer> assignRandomCities(int amount, int playableCount, List<Integer> takenIDs) {

        List<Integer> randomNumbers = new ArrayList<Integer>();  // This will hold the returned city IDs
        int counter = 0;
        boolean collisionFlag = false;

        Log.d(TAG, "Assigning " + amount + " random cities, ignoring " + takenIDs.size() + " reserved cities");

        Random numberGenerator = new Random();                  // Initialise a new random number generator

        do {                                                                // Until the amount of required numbers is reached
            int randomNumber = numberGenerator.nextInt(playableCount);      // Generate a number within playable limit
            //Log.d(TAG, "Checking for " + takenIDs.size() + " rands/reserved.  counter : " + counter);
            collisionFlag = false;
            for (int a = 0; a < takenIDs.size(); a++) {                     // Check for each of the provided invalid number
                if (randomNumber == takenIDs.get(a)) {
                    Log.d(TAG, "Rand " + randomNumber + " == " + takenIDs.get(a)
                            + " This number has been taken.  " + counter);
                    collisionFlag = true;
                    break;
                }
            }
            if(!collisionFlag) {
                randomNumbers.add(randomNumber);
                takenIDs.add(randomNumber);
                counter += 1;
                Log.d(TAG, "Random Number confirmed was " + randomNumber + " Total generated now: " + counter);
            }
//                } else if (randomNumber != takenIDs.get(a)){                      // Compare to the list of taken numbers
//                    noCollisionCounter += 1;                                // Doesnt match "a", move on
//                    Log.d(TAG, "Rnd " + randomNumber + " != "
//                            + takenIDs.get(a) + ",     " + a + ",  " + counter);
//                }
//
//
//
//                if (noCollisionCounter == takenIDs.size()) {
//                    randomNumbers.add(randomNumber);                // If the number is valid, add to list
//                    takenIDs.add(randomNumber);                     // Add this number to the reserved list
//                    counter += 1;
//                    noCollisionCounter = 0;
//                }
//            }
        } while (counter < amount + 1); // For the full amount of cities

        Log.d(TAG, "Random Numbers assigned successfully");

        return randomNumbers;
    }

    public void updateStatus(List<Integer> ids, int newStatus){
        String[] cityType = {"neutral", "friendly", "enemy"};
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d(TAG, "Assigning " + ids.size() + " " + cityType[newStatus] + " cities");

        for (int b = 0; b < ids.size(); b++) {
            ContentValues row = new ContentValues();
            row.put(COLUMN_NAMES[4], newStatus);

            db.update(TABLE_NAME, row, "ID=" + ids.get(b), null);
            Log.d(TAG, "Updated city at " + ids.get(b));
        }
    }

    // Retrieve the details of a city
    public City getCity(int id){
        SQLiteDatabase db = this.getReadableDatabase();         // Read from DB

        String selectCityQuery = " SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAMES[0] + " = " + id;
        Cursor cursor = db.rawQuery(selectCityQuery, null);

        if (cursor != null)             // Make sure there is a record
            cursor.moveToFirst();       // Move to it
        else {
            Log.d(TAG, "Nothing in database");      // If not, make note for debugging process
        }

        City city = new City(                               // Create city from class
                Integer.parseInt(cursor.getString(0)),      // Retrieve ID, cast to int from string
                cursor.getString(1),                        // Retrieve Name
                cursor.getString(2),                        // Retrieve Latitude, cast to float from string
                cursor.getString(3),                       // Retrieve Longitude, cast to float from string
                Integer.parseInt(cursor.getString(4)));      // Retrieve Status, cast to int from string

        return city;
    }

    public int updateCity(City givenCity) {
        SQLiteDatabase db = this.getReadableDatabase();         // Read from DB

        int id = givenCity.getID();

        String selectCityQuery = " SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAMES[0] + " = " + id;
        Cursor cursor = db.rawQuery(selectCityQuery, null);

        if (cursor != null)             // Make sure there is a record
            cursor.moveToFirst();       // Move to it
        else {
            Log.d(TAG, "Nothing in database");      // If not, make note for debugging process
        }

        City selectedCity = new City(                               // Create city from class
                Integer.parseInt(cursor.getString(0)),      // Retrieve ID, cast to int from string
                cursor.getString(1),                        // Retrieve Name
                cursor.getString(2),                        // Retrieve Latitude, cast to float from string
                cursor.getString(3),                       // Retrieve Longitude, cast to float from string
                Integer.parseInt(cursor.getString(4)));      // Retrieve Status, cast to int from string

        if (selectedCity.getName() != givenCity.getName()) {
            ContentValues row = new ContentValues();
            row.put(COLUMN_NAMES[1], givenCity.getName());

            db.update(TABLE_NAME, row, "ID=" + id, null);
            Log.d(TAG, "Updated city at " + id);
        }   // Update Name

        if (selectedCity.getLatitude() != givenCity.getLatitude()) {
            ContentValues row = new ContentValues();
            row.put(COLUMN_NAMES[2], givenCity.getLatitude());

            db.update(TABLE_NAME, row, "ID=" + id, null);
            Log.d(TAG, "Updated city at " + id);
        }   // Update Latitude

        if (selectedCity.getLongitude() != givenCity.getLongitude()) {
            ContentValues row = new ContentValues();
            row.put(COLUMN_NAMES[3], givenCity.getLongitude());

            db.update(TABLE_NAME, row, "ID=" + id, null);
            Log.d(TAG, "Updated city at " + id);
        }   // Update Longitude

        if (selectedCity.getStatus() != givenCity.getStatus()) {
            ContentValues row = new ContentValues();
            row.put(COLUMN_NAMES[4], givenCity.getStatus());

            db.update(TABLE_NAME, row, "ID=" + id, null);
            Log.d(TAG, "Updated city at " + id);
        }   // Update Status
        return 1;
    }

    /*

    // Function to update city... which sounds cheesy
    public int updateCity(City city) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues row = new ContentValues();
        row.put(COLUMN_NAMES[0], city.getID());
        row.put(COLUMN_NAMES[1], city.getName());
        row.put(COLUMN_NAMES[2], city.getLongitude());
        row.put(COLUMN_NAMES[3], city.getLatitude());
        row.put(COLUMN_NAMES[4], city.getStatus());

        // updating row
        return db.update(TABLE_NAME, row, COLUMN_NAMES[0] + " = ?",
                new String[] { String.valueOf(city.getID()) });
    }

    // Delete city... sort of like the SQLite/Botnet version of a nuke
    public void deleteCity(City city) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAMES[0] + " = ?",
                new String[] { String.valueOf(city.getID()) });
        db.close();
    }

     */ //Useful, unused Functions
}
