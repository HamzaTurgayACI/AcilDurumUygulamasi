package com.example.acildurumuygulamasi;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class LocationHelper {
    private final Context context;
    private final MapView mapView;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation(OnSuccessListener<GeoPoint> onSuccessListener) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                showUserLocation(userLocation);
                onSuccessListener.onSuccess(userLocation);
            }
        });
    }

    private void showUserLocation(GeoPoint location) {
        mapView.getController().setCenter(location);
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        Drawable markerIcon = ContextCompat.getDrawable(context, R.drawable.icon);
        marker.setIcon(markerIcon);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }
}
