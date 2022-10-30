package com.anurag.myapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;

public class CropRecommendation extends AppCompatActivity {
    FusedLocationProviderClient mFusedLocationClient;
    double latitude,longitude,temperature;
    int month;
    ArrayList<String> crops;
    String soil;
    TextView textView;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_recommendation);
        textView = findViewById(R.id.textView1);
        listView = findViewById(R.id.listView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

    }

    private void getInputs() {

        // Method to obtain all the Inputs without user intervention
        String url = "https://api.worldweatheronline.com/premium/v1/weather.ashx?q="+latitude+","+longitude+"&key=d7cd298c36d842cd985130454202009&format=json&mca=yes&fx=no&cc=no";
        String url1 = "https://rest.soilgrids.org/query?lon="+longitude+"&lat="+latitude;
        RequestQueue q = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url1, null, response -> {
            try {
                double clay = response.getJSONObject("properties").getJSONObject("CLYPPT").getJSONObject("M").getDouble("sl1");
                double sand = response.getJSONObject("properties").getJSONObject("SNDPPT").getJSONObject("M").getDouble("sl1");
                double silt = response.getJSONObject("properties").getJSONObject("SLTPPT").getJSONObject("M").getDouble("sl1");
                if (sand>=0 && sand<=45 && silt>=0 && silt<=40 && clay>=40 && clay<=100) {
                    soil = "CLAY";
                }
                else if (sand>=0 && sand<=20 && silt>=40 && silt<=60 && clay>=40 && clay<=60) {
                    soil = "SILTY CLAY";
                }
                else if (sand>=45 && sand<=65 && silt>=0 && silt<=20 && clay>=35 && clay<=55) {
                    soil = "SANDY CLAY";
                }
                else if (sand>=0 && sand<=20 && silt>=40 && silt<=73 && clay>=27 && clay<=40) {
                    soil = "SILTY CLAY LOAM";
                }
                else if (sand>=45 && sand<=80 && silt>=0 && silt<=28 && clay>=20 && clay<=35) {
                    soil = "SANDY CLAY LOAM";
                }
                else if (sand>=20 && sand<=45 && silt>=15 && silt<53 && clay>=27 && clay<=40) {
                    soil = "CLAY LOAM";
                }
                else if (sand>=0 && sand<=20 && silt>=80 && silt<=100 && clay>=0 && clay<=12) {
                    soil = "SILT";
                }
                else if ((sand>=20 && sand<=50 && silt>=50 && silt<=80 && clay>=0 && clay<=27) || (sand>=0 && sand<=8 && silt>=80 && silt<=88 && clay>=12 && clay<=20)) {
                    soil = "SILT LOAM";
                }
                else if (sand>=23 && sand<=52 && silt>=28 && silt<=50 && clay>=7 && clay<=27) {
                    soil = "LOAM";
                }
                else if ((sand>=52 && sand<=70 && silt>=10 && silt<=48 && clay>=0 && clay<=20) || (sand>=43 && sand<=52 && silt>=43 && silt<=50 && clay>=0 && clay<=7) || (sand>=70 && sand<=85 && silt>=0 && silt<=15 && clay>=15 && clay<=20)) {
                    soil = "SANDY LOAM";
                }
                else if (sand>=70 && sand<=85 && silt>=0 && silt<=30 && clay>=0 && clay<=15) {
                    soil = "LOAMY SAND";
                }
                else {
                    soil = "SAND";
                }
                textView.setText(soil);
                JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.GET, url, null, responses -> {
                    try {
                        final Calendar c = Calendar.getInstance();
                        month = c.get(Calendar.MONTH);
                        temperature = responses.getJSONObject("data").getJSONArray("ClimateAverages").getJSONObject(0).getJSONArray("month").getJSONObject(month).getDouble("avgTemp");
                        getCrops();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {

                });
                q.add(jsonObjectRequest1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {

        });
        q.add(jsonObjectRequest);
    }
    @SuppressWarnings("StringEquality")
    private void getCrops() {

        // Method to obtain the list of Recommended Crops
        crops = new ArrayList<>();
        if ((temperature>=18 && temperature<=37) && (soil=="SANDY LOAM" || soil=="LOAMY SAND" || soil=="SANDY CLAY LOAM") && (month==3 || month==4 || month==5 || month==6)) {
            crops.add("Cotton");
        }
        if ((temperature>=20 && temperature<=37) && (soil=="CLAY LOAM" || soil=="CLAY" || soil=="LOAM" || soil=="SILTY CLAY" || soil=="SILTY CLAY LOAM") && (month==3 || month==4 || month==5 || month==6 || month==7)) {
            crops.add("Paddy");
        }
        if ((temperature>=16 && temperature<=30) && (soil=="SANDY" || soil=="SILTY CLAY LOAM" || soil=="LOAM" || soil=="SANDY LOAM" || soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM" || soil=="SANDY CLAY") && (month==0 || month==1 || month==5 || month==6 || month==8 || month==9 || month==10)) {
            crops.add("Maize");
        }
        if ((temperature>=18 && temperature<=25) && (soil=="LOAM" || soil=="SANDY LOAM" || soil=="SANDY CLAY LOAM" || soil=="CLAY" || soil=="CLAY LOAM") && (month==8 || month==9 || month==10 || month==11 || month==0)) {
            crops.add("Wheat");
        }
        if ((temperature>=20 && temperature<=30) && (soil=="CLAY" || soil=="LOAM" || soil=="CLAY LOAM" || soil=="SANDY LOAM") && (month==3 || month==4 || month==5 || month==6 || month==7)) {
            crops.add("Bajra");
        }
        if ((temperature>=23 && temperature<=33) && (soil=="CLAY LOAM" || soil=="LOAM" || soil=="SANDY LOAM") && (month==0 || month==1 || month==2 || month==3 || month==4 || month==5 || month==6 || month==7 || month==8 || month==9 || month==10 || month==11)) {
            crops.add("Jowar");
        }
        if ((temperature>=15 && temperature<=32) && (soil=="SANDY LOAM" || soil=="SILTY CLAY LOAM" || soil=="LOAM" || soil=="CALY LOAM") && (month==1 || month==2 || month==5 || month==6)) {
            crops.add("Soybean");
        }
        if ((temperature>=20 && temperature<=34) && (soil=="LOAM" || soil=="SANDY CLAY LOAM" || soil=="SANDY LOAM" || soil=="CLAY LOAM" || soil=="LOAMY SAND") && (month==3 || month==4 || month==5 || month==6 || month==9 || month==10 || month==11)) {
            crops.add("Ragi");
        }
        if ((temperature>=15 && temperature<=28) && (soil=="SANDY LOAM" || soil=="LOAM" || soil=="SANDY CLAY LOAM") && (month==0 || month==1 || month==9 || month==10 || month==11)) {
            crops.add("Sunflower");
        }
        if ((temperature>=20 && temperature<=30) && (soil=="SANDY LOAM" || soil=="LOAM" || soil=="SANDY CLAY LOAM") && (month==0 || month==5 || month==6 || month==7 || month==10 || month==11)) {
            crops.add("Groundnut");
        }
        if ((temperature>=14 && temperature<=30) && (soil=="SANDY LOAM" || soil=="LOAMY SAND" || soil=="LOAM" || soil=="SILT LOAM" || soil=="SANDY CLAY LOAM" || soil=="SANDY CLAY" || soil=="SILTY CLAY" || soil=="SILTY CLAY LOAM" || soil=="SILT" || soil=="CLAY LOAM") && (month==2 || month==3 || month==5 || month==6 || month==9 || month==10)) {
            crops.add("Tomato");
        }
        if ((temperature>=20 && temperature<=30) && (soil=="LOAM" || soil=="SANDY LOAM" || soil=="SILT LOAM") && (month==1 || month==2 || month==4 || month==5 || month==6 || month==8 || month==9 || month==10)) {
            crops.add("Chilli");
        }
        if ((temperature>=12 && temperature<=24) && (soil=="LOAM" || soil=="SANDY LOAM" || soil=="SILT LOAM" || soil=="CLAY LOAM") && (month==0 || month==5 || month==6 || month==7 || month==9 || month==10 || month==11)) {
            crops.add("Brinjal");
        }
        if ((temperature>=20 && temperature<=35) && (soil=="SANDY LOAM" || soil=="CLAY LOAM" || soil=="LOAM" || soil=="SANDY CLAY LOAM") && (month==3 || month==4 || month==5 || month==6)) {
            crops.add("Turmeric");
        }
        if ((temperature>=22 && temperature<=35) && (soil=="CLAY LOAM" || soil=="SANDY LOAM" || soil=="LOAM") && (month==0 || month==1 || month==2 || month==5 || month==6 || month==7)) {
            crops.add("Lady's Finger");
        }
        if ((temperature>=22 && temperature<=35) && (soil=="SANDY LOAM" || soil=="LOAM") && (month==1 || month==2 || month==5 || month==6)) {
            crops.add("Green Gram");
        }
        if ((temperature>=22 && temperature<=35) && (soil=="SANDY LOAM" || soil=="LOAM") && (month==1 || month==2 || month==5 || month==3 || month==6)) {
            crops.add("Black Gram");
        }
        if ((temperature>=18 && temperature<=35) && (soil=="SANDY LOAM" || soil=="LOAM" || soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM") && (month==0 || month==1 || month==3 || month==4 || month==5 || month==6)) {
            crops.add("Bottle Gourd");
        }
        if ((temperature>=5 && temperature<=19) && (soil=="SANDY LOAM" || soil=="LOAMY SAND" || soil=="LOAM" || soil=="SILT LOAM" || soil=="SANDY CLAY LOAM" || soil=="SANDY CLAY" || soil=="SILTY CLAY" || soil=="SILTY CLAY LOAM" || soil=="SILT" || soil=="CLAY LOAM") && (month==2 || month==3 || month==4 || month==9 || month==10)) {
            crops.add("Pea");
        }
        if ((temperature>=5 && temperature<=27) && (soil=="SANDY" || soil=="LOAMY SAND" || soil=="SANDY LOAM" || soil=="LOAM" || soil=="CLAY LOAM" || soil=="SANDY CLAY LOAM") && (month==0 || month==9 || month==10 || month==11)) {
            crops.add("Barley");
        }
        if ((temperature>=5 && temperature<=25) && (soil=="CLAY LOAM" || soil=="SILT LOAM" || soil=="SILTY CLAY" || soil=="CLAY" || soil=="SANDY CLAY" || soil=="SILTY CLAY LOAM" || soil=="SANDY CLAY LOAM" || soil=="LOAM" || soil=="SANDY LOAM") && (month==9 || month==10 || month==11)) {
            crops.add("Oats");
        }
        if ((temperature>=18 && temperature<=30) && (soil=="LOAM" || soil=="SILT LOAM" || soil=="SILTY CLAY LOAM" || soil=="SANDY LOAM" || soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM") && (month==5 || month==6 || month==9 || month==10)) {
            crops.add("Coriander");
        }
        if ((temperature>=20 && temperature<=30) && (soil=="SANDY LOAM" || soil=="CLAY LOAM" || soil=="LOAM" || soil=="SANDY CLAY LOAM") && (month==9 || month==10)) {
            crops.add("Bengal Gram");
        }
        if ((temperature>=13 && temperature<=25) && (soil=="CLAY LOAM" || soil=="SILT LOAM" || soil=="SILTY CLAY" || soil=="CLAY" || soil=="SANDY CLAY" || soil=="SILTY CLAY LOAM" || soil=="SANDY CLAY LOAM" || soil=="LOAM" || soil=="SANDY LOAM") && (month==0 || month==4 || month==5 || month==6 || month==7 || month==8 || month==9 || month==10 || month==11)) {
            crops.add("Onion");
        }
        if ((temperature>=10 && temperature<=30) && (soil=="SANDY CLAY" || soil=="SANDY CLAY LOAM" || soil=="CALY LOAM" || soil=="LOAM" || soil=="SANDY LOAM") && (month==5 || month==6 || month==9 || month==10)) {
            crops.add("Garlic");
        }
        if ((temperature>=15 && temperature<=26) && (soil=="SANDY" || soil=="SANDY LOAM" || soil=="LOAMY SAND" || soil=="LOAM" || soil=="SILT LOAM") && (month==7 || month==8 || month==9 || month==10 || month==11)) {
            crops.add("Carrot");
        }
        if ((temperature>=13 && temperature<=26) && (soil=="LOAM" || soil=="CLAY LOAM" || soil=="SANDY CLAY LOAM" || soil=="SANDY LOAM") && (month==4 || month==5 || month==6 || month==7 || month==8 || month==9 || month==10)) {
            crops.add("Cauliflower");
        }
        if ((temperature>=10 && temperature<=30) && (soil=="LOAM" || soil=="SANDY LOAM" || soil=="SILT LOAM" || soil=="SANDY CLAY LOAM") && (month==0 || month==5 || month==6 || month==7 || month==9 || month==10)) {
            crops.add("Potato");
        }
        if ((temperature>=10 && temperature<=30) && (soil=="LOAM" || soil=="CLAY LOAM" || soil=="SANDY CLAY LOAM") && (month==8 || month==9 || month==10 || month==11)) {
            crops.add("Rapeseed");
        }
        if ((temperature>=18 && temperature<=33) && (soil=="SANDY LOAM" || soil=="LOAM" || soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM" || soil=="LOAMY SAND" || soil=="SILT LOAM" || soil=="SILTY CLAY LOAM") && (month==2 || month==1 || month==0 || month==10 || month==11)) {
            crops.add("Watermelon");
        }
        if ((temperature>=18 && temperature<=30) && (soil=="SANDY LOAM" || soil=="LOAMY SAND" || soil=="SANDY CLAY LOAM" || soil=="LOAM" || soil=="SILT") && (month==0 || month==1 || month==2 || month==3 || month==10 || month==11)) {
            crops.add("Muskmelon");
        }
        if ((temperature>=18 && temperature<=28) && (soil=="LOAM" || soil=="LOAMY SAND" || soil=="SANDY LOAM" || soil=="SANDY CLAY LOAM") && (month==0 || month==1 || month==2 || month==8 || month==9 || month==10 || month==11)) {
            crops.add("Pumpkin");
        }
        if ((temperature>=15 && temperature<=25) && (soil=="CLAY LOAM" || soil=="SANDY CLAY LOAM" || soil=="SILT LOAM" || soil=="LOAM" || soil=="SANDY LOAM" || soil=="LOAMY SAND" || soil=="SANDY") && (month==0 || month==1 || month==2 || month==3 || month==5)) {
            crops.add("Cucumber");
        }
        if ((temperature>=18 && temperature<=28) && (soil=="SANDY LOAM" || soil=="LOAM" || soil=="CLAY LOAM" || soil=="SANDY CLAY LOAM" || soil=="SANDY CLAY" || soil=="CLAY" || soil=="SILT LOAM" || soil=="SILTY CLAY LOAM") && (month==0 || month==1 || month==2 || month==5 || month==6)) {
            crops.add("Bitter Gourd");
        }
        if ((temperature>=20 && temperature<=35) && (soil=="SANDY LOAM" || soil=="LOAM" || soil=="CLAY LOAM" || soil=="SILT" || soil=="SILTY CLAY LOAM" || soil=="SILT LOAM" || soil=="SANDY CLAY LOAM") && (month==1 || month==2 || month==3 || month==0 || month==5 || month==6)) {
            crops.add("Ridge Gourd");
        }
        if ((temperature>=15 && temperature<=22) && (soil=="SANDY LOAM" || soil=="LOAM" || soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM" || soil=="SANDY CLAY" || soil=="CLAY") && (month==8 || month==9 || month==10)) {
            crops.add("Cabbage");
        }
        if ((temperature>=24 && temperature<=38) && (soil=="LOAM" || soil=="SANDY LOAM" || soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM" || soil=="SILT LOAM") && (month==1 || month==2 || month==3 || month==4)) {
            crops.add("Jute");
        }
        if ((temperature>=21 && temperature<=38) && (soil=="SANDY LOAM" || soil=="LOAM" || soil=="LOAMY SAND" || soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM" || soil=="CLAY" || soil=="SANDY CLAY" || soil=="SILT LOAM" || soil=="SILTY CLAY LOAM") && (month==1 || month==2 || month==3 || month==4)) {
            crops.add("Ginger");
        }
        if ((temperature>=20 && temperature<=30) && (soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM" || soil=="LOAM" || soil=="SANDY LOAM") && (month==8 || month==9 || month==10 || month==11)) {
            crops.add("Capsicum");
        }
        if ((temperature>=10 && temperature<=25) && (soil=="SILT" || soil=="SILTY CLAY" || soil=="SANDY" || soil=="SANDY LOAM" || soil=="LOAM" || soil=="LOAMY SAND" || soil=="SANDY CLAY LOAM" || soil=="CLAY LOAM" || soil=="CLAY" || soil=="SANDY CLAY" || soil=="SILT LOAM" || soil=="SILTY CLAY LOAM") && (month==8 || month==9 || month==10)) {
            crops.add("Mustard");
        }
        ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, crops);
        listView.setAdapter(listAdapter);
    }

    @SuppressLint("MissingPermission")
    private void getLocation(){

        // Obtain Lat Long Coordinates
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        task -> {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                getInputs();
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        // Request Device Location
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
    };

    private boolean checkPermissions() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                50
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 50) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }
}
