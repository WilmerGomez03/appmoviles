package com.example.gmaps_reto1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private Location locAct,locMarCer;
    private Marker me;
    private TextView output;
    private Polygon icesi;
    private Button addMarker;
    private double Xini=0;
    private double Yini=0;
    private Geocoder geocoder;
    private LocationManager manager;
    private List<Address> dirActual;
    private ArrayList<Marker> markers;
    private EditText nombreMarcador;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        output = findViewById(R.id.output);
        output.setText("No hay lugares marcados");
        markers = new ArrayList<>();
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());
        addMarker = findViewById(R.id.AddMark);
        nombreMarcador = findViewById(R.id.nameMarker);
        nombreMarcador.setVisibility(View.INVISIBLE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        addMarker = findViewById(R.id.AddMark);
        addMarker.setOnClickListener(
                (v) -> {
                    nombreMarcador.setVisibility(View.VISIBLE);
                }
        );

        //permisos al iniciar app
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 11);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11);
        } else {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11) {
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        LatLng pos = new LatLng(locAct.getLatitude(),locAct.getLongitude());

        locMarCer= new Location("custom Location");
        locMarCer.setLongitude(latLng.longitude);
        locMarCer.setLatitude(latLng.latitude);
        Marker nuevoMarcador = null;
        LatLng pos2= new LatLng(latLng.latitude, latLng.longitude);
        double distance = Math.round(CalcularDistancia(pos,pos2));
        try {

            String locText = nombreMarcador.getText().toString();
            nombreMarcador.setText("");
            String addres = locText+" ubicado en: "+geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0).getAddressLine(0).split(",")[0];
            if (nuevoMarcador==null){
                Marker newMarker = mMap.addMarker(new MarkerOptions().position(pos2).title(addres).snippet("Distancia: "+distance+" m").icon(BitmapDescriptorFactory.defaultMarker(230.0f)));
                markers.add(newMarker);
            }
            nombreMarcador.setVisibility(View.INVISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MarcadorCercano();


    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:
                MarcadorCercano();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (locAct == null) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

                try {
                    locAct = location;
                    dirActual = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String direccion = dirActual.get(0).getAddressLine(0);
                    me = mMap.addMarker(new MarkerOptions().position(pos).title(direccion));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                locAct = location;
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
                me.setPosition(pos);
                try {
                    dirActual = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String direccion = dirActual.get(0).getAddressLine(0);
                    me.setTitle(direccion);

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void MarcadorCercano(){

        double distancia = 99999.0;
        Location temp = new Location("temp marker");
        Marker cercano = null;
        LatLng pos= new LatLng(locAct.getLatitude(), locAct.getLongitude());
        for (int i = 0; i < markers.size();i++){
            temp.setLatitude(markers.get(i).getPosition().latitude);
            temp.setLongitude(markers.get(i).getPosition().longitude);
            LatLng pos2= new LatLng(temp.getLatitude(), temp.getLongitude());
            double tempDistancia = Math.round(CalcularDistancia(pos,pos2));

            if (tempDistancia<distancia){
                cercano = markers.get(i);
                distancia=tempDistancia;
            }

        }
        if (cercano!=null){
            if(distancia<50){
                String message = "Ubicación actual: " + cercano.getTitle();
                output.setText(message);
            }else {
                String message = "El lugar más cercano es " + cercano.getTitle() + " a " + distancia +" m";
                output.setText(message);
            }
        }

    }
    public double CalcularDistancia(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double meter = valueResult % 1000;

        return meter*100;
    }
}
