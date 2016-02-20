package at.htlleonding.firetracker.details;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import at.htlleonding.firetracker.R;
import at.htlleonding.firetracker.activities.EquipmentActivity;
import at.htlleonding.firetracker.activities.LogbookActivity;
import at.htlleonding.firetracker.entities.Logbook;

public class DetailLogbook extends FragmentActivity {
    private GoogleMap mMap;

    private TextView tvDistance;
    private TextView tvDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailloglayout);
        setUpMapIfNeeded();
        tvDistance=(TextView)findViewById(R.id.tvLogDetailKM);
        tvDestination=(TextView)findViewById(R.id.tvLogDetailDestination);
        ((Button)findViewById(R.id.btDetailLogSatellite)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        ((Button)findViewById(R.id.btDetailLogNormal)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        List<LatLng> list = new LinkedList<LatLng>();
        //Json in String speichern
        String s = getIntent().getStringExtra("logBook");

        try {
            JSONArray json = new JSONArray(s);
            JSONObject obj;
            for (int i = 0;i<json.length();i++){
                obj=json.getJSONObject(i);
                list.add(new LatLng(obj.getDouble("lat"),obj.getDouble("lng")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

//.icon(BitmapDescriptorFactory.fromResource(R.drawable.finishFlag))
        mMap.addMarker(new MarkerOptions().position(list.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker)));
        mMap.addMarker(new MarkerOptions().position(list.get(list.size() - 1)).icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_marker)));
        mMap.addPolyline(new PolylineOptions().addAll(list).color(getBaseContext().getResources().getColor(R.color.wallet_holo_blue_light))); // Route zeichnen (mit allen Koordinaten)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(list.get(0),12));// Kamer ausrichten

        double distance=0;
        Location start = new Location("");
        Location end = new Location("");

        for (int i = 0;i<list.size()-1;i++){
            // Startpunkt angeben
            start.setLatitude(list.get(i).latitude);
            start.setLongitude(list.get(i).longitude);

            // Enpunkt angeben
            end.setLatitude(list.get(i + 1).latitude);
            end.setLongitude(list.get(i+1).longitude);
            distance+=start.distanceTo(end); // Rechnet Entfernung einer Route aus

        }

        tvDistance.setText("gefahrene Kilometer: "+ (float)Math.round(distance)/1000);
        tvDestination.setText("Einsatzort: "+getIntent().getStringExtra("destination"));
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDetail))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


}

