package com.example.acildurumuygulamasi;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RouteHelper {
    private final String GRAPH_HOPPER_API_KEY = "786e5d70-13f4-4e39-9d38-08e5ec6ed719";
    private final MapView mapView;
    private final List<Polyline> currentRoutes; // Mevcut rotaları saklar

    public RouteHelper(Context context, MapView mapView) {
        this.mapView = mapView;
        this.currentRoutes = new ArrayList<>();
    }

    public void fetchRoute(GeoPoint start, GeoPoint end) {
        clearCurrentRoutes();
        String url = "https://graphhopper.com/api/1/route?point=" + start.getLatitude() + "," + start.getLongitude()
                + "&point=" + end.getLatitude() + "," + end.getLongitude()
                + "&vehicle=car&locale=en&key=" + GRAPH_HOPPER_API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray paths = jsonObject.getJSONArray("paths");
                        if (paths.length() > 0) {
                            JSONObject path = paths.getJSONObject(0);
                            String points = path.getString("points");
                            List<GeoPoint> geoPoints = PolylineDecoder.decode(points);

                            // Yeni bir polyline oluştur ve haritaya ekle
                            Polyline polyline = createPolyline(geoPoints);
                            mapView.getOverlays().add(polyline);
                            currentRoutes.add(polyline); // Yeni polyline'ı listeye ekle

                            mapView.invalidate();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", "Failed to parse JSON", e);
                    }
                }
            }
        });
    }

    // Haritada görünen mevcut rotaları temizler
    private void clearCurrentRoutes() {
        for (Polyline polyline : currentRoutes) {
            mapView.getOverlays().remove(polyline);
        }
        currentRoutes.clear(); // Listeyi temizle
        mapView.invalidate();
    }

    // Polyline oluşturma
    private Polyline createPolyline(List<GeoPoint> geoPoints) {
        Polyline polyline = new Polyline();
        polyline.setPoints(geoPoints);
        polyline.setWidth(10.0f);
        return polyline;
    }
}
