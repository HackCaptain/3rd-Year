package com.example.botnets;

// Helper class for retrieving and modifying city details.
// Used to help get the city locations used in Botnets.

// Getter and setters for everything because they are useful
// e.g LatLng co - ordinates, in case of a really bad earthquake or something
// or names in case of political shifts or something.

public class City {
    int _id;
    String _name;
    float _latitude;
    float _longitude;
    int _status;

    public City() {
    }

    public City(int id, String name, String _latitude, String _longitude, int status) {
        this._id = id;
        this._name = name;
        this._latitude = Float.parseFloat(_latitude);
        this._longitude = Float.parseFloat(_longitude);
        this._status = status;
    }

    public City(String name, float _latitude, float _longitude){
        this._name = name;
        this._latitude = _latitude;
        this._longitude = _longitude;
    }

    // Getters


    public int getID(){
        return this._id;
    }

    public String getName(){
        return this._name;
    }

    public float getLatitude(){
        return this._latitude;
    }

    public float getLongitude(){
        return this._longitude;
    }

    public int getStatus(){
        return this._status;
    }

    // Setters

    public void setID(int id){
        this._id = id;
    }

    public void setName(String Name){
        this._name = Name;
    }

    public void setLatitude(String Latitude){
        this._latitude = Float.parseFloat(Latitude);
    }

    public void setLongitude(String Longitude){
        this._longitude = Float.parseFloat(Longitude);
    }

    public void setStatus(int status){
        this._status = status;
    }

}
