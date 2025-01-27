package com.example.firebase;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import  android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mapa;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    DatabaseReference coordinatesRef;
    Marker marker;

    LatLng parquecentral;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationUpdates();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        coordinatesRef = mDatabase.child("Coordenadas");



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa= googleMap;
        // declarar punto
        parquecentral = new LatLng(-1.0243309441161157, -79.46626348281661);
        //movimiento de camara
        CameraUpdate camUpd1 =
                CameraUpdateFactory
                    .newLatLngZoom(parquecentral, 15);
        mapa.moveCamera(camUpd1);

        mapa.addMarker(new MarkerOptions().position(parquecentral)
        .title("PARQUE CENTRAL"));

    }
    @SuppressLint("MissingPermission")
    private void setupLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); //Cada 5 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    grabarNuevaPosicionGPS(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == 			PackageManager.PERMISSION_GRANTED) {
            setupLocationUpdates();
        } else {
            Toast.makeText(this, "Acceso NO permitido", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    private void grabarNuevaPosicionGPS(Location location) {

        TextView txtLatitud = findViewById(R.id.textView3);
        TextView txtLongitud = findViewById(R.id.textView2);
        txtLatitud.setText (String.format("%.5f", location.getLatitude()).toString());
        txtLongitud.setText (String.format("%.5f", location.getLongitude()).toString());

        coordinatesRef.child("latitud").setValue (location.getLatitude());
        coordinatesRef.child("longitud").setValue (location.getLongitude());
        actualizarMarcadorMapa(location.getLatitude(), location.getLongitude() );
    }
    private void actualizarMarcadorMapa(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        if (marker == null)
            marker = mapa.addMarker(new MarkerOptions().position(latLng).title("Tu Pos"));
        else
            marker.setPosition(latLng);

        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }



}