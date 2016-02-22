package at.htlleonding.firetracker.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;

import at.htlleonding.firetracker.webSocket.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class    Gps_tracking extends IntentService {

    LocationManager locationManager;
    String provider;
    private static boolean trackingEnabled=true;
    private LinkedList<Location> notSentLocations;


    // Konstruktor
    public Gps_tracking() {
        super("Gps_tracking");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        notSentLocations=new LinkedList<>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Wie genau die Standortbestimmung ist (FEIN)
        criteria.setPowerRequirement(Criteria.POWER_HIGH); // Wie viel Strom darf verbraucht werden (HOCH)


        WebSocket socket; // WebSocket Instanz

        // Wird alle 5 Sekunden durchgelaufen
        while (true) {
            socket = WebSocket.getInstance(getApplication());
            // Über welche Schnittstelle wird die Location abgefragt (GPS, Network) - in diesem Fall immer GPS
            provider = locationManager.getBestProvider(criteria, false);
            // Nur notwendig damit LastKnownLocation die aktuelle Location zurückliefert
            locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
            Location location = locationManager.getLastKnownLocation(provider); // Gibt letzte gespeicherte Position zurück

            // Findet man eine Location
            if (location != null) {
                Intent i = new Intent("Gps_Update"); // Neuer Intent mit Parameter des Broadcast-Receivers

                i.putExtra("x_coordinate", location.getLongitude()); // X-Coordinate der aktuellen Position
                i.putExtra("y_coordinate", location.getLatitude()); // Y-Coordinate der aktuellen Position
                if (trackingEnabled) { // Wenn das GPS-Tracking eingeschaltet ist..
                    sendBroadcast(i);// ...wird der Broadcast gesendet
                    notSentLocations.add(location);
                    if(WebSocket.isConnected) {
                        for (Location loc :
                                notSentLocations) {
                            socket.sendLocation(loc); // Dem WebSocket wird die aktuelle Position gesendet
                        }
                        notSentLocations.clear();

                    }

                }
                System.out.println("" + location.getLatitude() + " : " + location.getLongitude());
            } else
                Toast.makeText(getApplicationContext(), "no Location found", Toast.LENGTH_LONG).show();
            try {
                Thread.currentThread().sleep(5000); // Warte 5 Sekunden, bis dann nächste Position gesucht wird
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public static boolean isTrackingEnabled() {
        return trackingEnabled;
    } // Returnded einen Boolean ob Tracking enabled ist

    // Setzen des Boolean
    public static void setTrackingEnabled(boolean trackingEn) {
        trackingEnabled = trackingEn;
    }
}
