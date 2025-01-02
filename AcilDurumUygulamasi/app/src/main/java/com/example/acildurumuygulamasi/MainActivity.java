package com.example.acildurumuygulamasi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private MapView mapView;
    private Button routeButton;
    private String selectedDisasterType = "deprem";

    private LocationHelper locationHelper;
    private RouteHelper routeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OpenStreetMap ayarlarını yükle
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));

        // Harita ve butonları tanımla
        mapView = findViewById(R.id.map);
        routeButton = findViewById(R.id.routeButton);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.5);

        // Konum izni kontrol edilir
        if (checkLocationPermission()) {
            initializeLocationHelper();
        } else {
            requestLocationPermission();
        }

        // Helper sınıflarını başlat
        locationHelper = new LocationHelper(this, mapView);
        routeHelper = new RouteHelper(this, mapView);

        setupDisasterSpinner();

        routeButton.setOnClickListener(v -> calculateRoute());

        // Acil durum bildir butonu
        Button emergencyButton = findViewById(R.id.btnEmergency);
        emergencyButton.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:112"));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                startActivity(callIntent);
            }
        });
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void initializeLocationHelper() {
        locationHelper = new LocationHelper(this, mapView);
        locationHelper.getCurrentLocation(location -> {
            Toast.makeText(this, "Konum alındı: " + location, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDisasterSpinner() {
        Spinner disasterSpinner = findViewById(R.id.disasterSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.disaster_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        disasterSpinner.setAdapter(adapter);

        disasterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDisasterType = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Seçilen afet: " + selectedDisasterType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void calculateRoute() {
        locationHelper.getCurrentLocation(userLocation -> {
            if (userLocation != null) {
                GeoPoint closestTarget = getClosestTargetPoint(selectedDisasterType, userLocation);
                if (closestTarget != null) {
                    routeHelper.fetchRoute(userLocation, closestTarget);
                } else {
                    Toast.makeText(this, "Hedef noktası bulunamadı!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Kullanıcı konumu alınamadı!", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private GeoPoint getClosestTargetPoint(String disasterType, GeoPoint userLocation) {
        List<GeoPoint> targetCoordinates;

        // Afet türüne göre hedef noktaları belirle
        switch (disasterType.toLowerCase()) {
            case "deprem":
                targetCoordinates = Arrays.asList(
                        new GeoPoint(38.3530, 38.3833), // Orduzu Pınarbaşı
                        new GeoPoint(38.3466, 38.2911)  // Sümer Parkı
                );
                break;
            case "yangın":
                targetCoordinates = Arrays.asList(
                        new GeoPoint(38.3491, 38.3044), // Doğa Cadde Otoparkı
                        new GeoPoint(38.3288, 38.2658)  // 100. Yıl Parkı
                );
                break;
            case "sel":
                targetCoordinates = Arrays.asList(
                        new GeoPoint(38.3386, 38.3397), // Beydağı Tabiat Parkı
                        new GeoPoint(38.3016, 38.2483)  // Gedik
                );
                break;
            default:
                Toast.makeText(this, "Geçersiz afet türü!", Toast.LENGTH_SHORT).show();
                return null;
        }


        return findClosestPoint(userLocation, targetCoordinates);
    }



    private GeoPoint findClosestPoint(GeoPoint userLocation, List<GeoPoint> targetCoordinates) {

        GeoPoint closestPoint = null;
        double shortestDistance = Double.MAX_VALUE;

        for (GeoPoint target : targetCoordinates) {
            double distance = userLocation.distanceToAsDouble(target);

            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestPoint = target;
            }
        }
        return closestPoint;
    }



    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocationHelper();
            } else {
                Toast.makeText(this, "Konum izni gerekli", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
