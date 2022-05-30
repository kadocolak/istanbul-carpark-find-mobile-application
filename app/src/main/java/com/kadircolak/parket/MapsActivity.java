package com.kadircolak.parket;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap haritasiteMap;
    public double currentLat,currentLng;
    private LocationManager locationManager;
    LatLng currentLocation,parkLocation;
    Calendar dateNow = Calendar.getInstance();
    private int nowHour = dateNow.get(Calendar.HOUR_OF_DAY);
    private double currentCapacity = 0;
    private double startHour = 0;
    private double finishHour = 0;
    private double nowDistance = 0;
    private String tomtomApi = "BGIsg8FveblTPXxLwDAZg10XxlODvQDq";
    private RequestQueue request;

    public void opening()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        request = Volley.newRequestQueue(MapsActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        request = Volley.newRequestQueue(MapsActivity.this);
    }

    public void onMapReady(GoogleMap googleMap) {
        haritasiteMap = googleMap;
        haritasiteMap.setTrafficEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        haritasiteMap.setMyLocationEnabled(true);
        haritasiteMap.getUiSettings().setZoomControlsEnabled(true);
        haritasiteMap.getUiSettings().setMyLocationButtonEnabled(true);
        haritasiteMap.getUiSettings().isCompassEnabled();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)  return;
        else {
            Location locationGPS = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            if(locationGPS!=null)
            {
                currentLat=locationGPS.getLatitude();
                currentLng=locationGPS.getLongitude();
                currentLocation = new LatLng(currentLat, currentLng);
                haritasiteMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
                findNearStations();
            }
            else Toast.makeText(getApplicationContext(), "Konum paylaşımı yapmadınız!", Toast.LENGTH_LONG).show();
        }
    }

    private void findNearStations()
    {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.ibb.gov.tr/").addConverterFactory(GsonConverterFactory.create()).build();
        JsonWebApi jsonWebApi = retrofit.create(JsonWebApi.class);
        Call<List<Post>> call = jsonWebApi.getData();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), response.code(), Toast.LENGTH_LONG).show();
                    return;
                }
                List<Post> posts = response.body();
                for (Post post : posts) {
                    parkLocation = new LatLng(post.getLat(), post.getLng());
                    boolean check = arePointsNear(currentLocation, parkLocation, 5000);
                    if(check) {
                        String url = "https://api.tomtom.com/routing/1/calculateRoute/"+currentLat+","+currentLng+":"+post.getLat()+","+post.getLng()+"/json?key="+tomtomApi;
                        JsonObjectRequest getData = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray routeArray = response.getJSONArray("routes");
                                    JSONObject routes = routeArray.getJSONObject(0);
                                    if(post.getIsOpen()==1)
                                    {
                                        currentCapacity = 100*(post.getCapacity()-post.getEmptyCapacity())/post.getCapacity();
                                        if(currentCapacity<0) currentCapacity=100;
                                        try
                                        {
                                            startHour = Double.parseDouble(post.getWorkHours().split("-")[0].split(":")[0]);
                                            finishHour = Double.parseDouble(post.getWorkHours().split("-")[1].split(":")[0]);
                                        }
                                        catch(Exception e)
                                        { startHour=0; finishHour=23; }
                                        if (nowHour > startHour && finishHour > nowHour) {
                                            if (currentCapacity >= 0 && currentCapacity<=75) {
                                                LatLng marker = new LatLng(post.getLat(), post.getLng());
                                                nowDistance = Math.round(routes.getJSONObject("summary").getInt("travelTimeInSeconds"));
                                                haritasiteMap.addMarker(new MarkerOptions().position(marker).title(post.getParkName()).snippet("Yaklaşık "+(Math.round(nowDistance)/60)+" dakika Güncel kapasite: %"+Math.round(currentCapacity)+"").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        request.add(getData);
                    }
                };
            }
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
            }
        });
        Retrofit retrofit1 = new Retrofit.Builder().baseUrl("https://kadircolak.com/").addConverterFactory(GsonConverterFactory.create()).build();
        PrivateStations privateStations = retrofit1.create(PrivateStations.class);
        Call<List<Post3>> call2 = privateStations.getLocation();
        call2.enqueue(new Callback<List<Post3>>() {
            @Override
            public void onResponse(Call<List<Post3>> call2, Response<List<Post3>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), response.code(), Toast.LENGTH_LONG).show();
                    return;
                }
                List<Post3> posts = response.body();
                for (Post3 post : posts) {
                    parkLocation = new LatLng(post.getparkLat(), post.getparkLng());
                    boolean check = arePointsNear(currentLocation, parkLocation, 5000);
                    if(check)
                    {
                        String url = "https://api.tomtom.com/routing/1/calculateRoute/"+currentLat+","+currentLng+":"+post.getparkLat()+","+post.getparkLng()+"/json?key="+tomtomApi;
                        JsonObjectRequest getData = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray routeArray = response.getJSONArray("routes");
                                    JSONObject routes = routeArray.getJSONObject(0);
                                    LatLng marker = new LatLng(post.getparkLat(), post.getparkLng());
                                    nowDistance = Math.round(routes.getJSONObject("summary").getInt("travelTimeInSeconds"));
                                    haritasiteMap.addMarker(new MarkerOptions().position(marker).title(post.getparkName()).snippet("Yaklaşık "+(Math.round(nowDistance)/60)+" dakika").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                            }
                        });
                        request.add(getData);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Post3>> call2, Throwable t) {
            }
        });
    }

    private boolean arePointsNear(LatLng current, LatLng park, int radius)
    {
        int km = radius/1000;
        int ky = 40000 / 360;
        double kx = Math.cos(Math.PI * park.latitude / 180) * ky;
        double dx = Math.abs(park.longitude - current.longitude) * kx;
        double dy = Math.abs(park.latitude - current.latitude) * ky;
        return Math.sqrt(dx * dx + dy * dy) <= km;
    }

    @Override
    public void onStart(){
        super.onStart();
        opening();
        findNearStations();
    }

    @Override
    public void onResume(){
        super.onResume();
        opening();
        findNearStations();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        opening();
        findNearStations();
    }
}