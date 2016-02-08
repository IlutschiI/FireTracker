package at.htlleonding.firetracker.mapActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.em;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import at.htlleonding.firetracker.R;
import at.htlleonding.firetracker.Services.GMapDirections;
import at.htlleonding.firetracker.Services.Gps_tracking;
import at.htlleonding.firetracker.activities.EquipmentActivity;
import at.htlleonding.firetracker.activities.LogbookActivity;
import at.htlleonding.firetracker.webSocket.WebSocket;

public class MapsActivity extends FragmentActivity {

    //region Variablen definition
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker currentPos; // Ein Marker ist ein Symbol, das (in dem Fall) meinen eigenen Standort anzeigt
    TextView tvKM; // Fehlende Kilometer bis zum Einsatzort
    TextView tvLoc; // Adresse des Einsatzortes
    Button btG; // Button zu den Geräten
    Button btFB; // Button zu dem Fahrtenbuch
    Intent i;
    ActionBar actionBar;
    Polyline emergencyRoute; // Polyline zeichnet Route
    private static boolean followPoint=true;
    private static boolean isEmergency=false;
    LatLng emergencyLatLng;
    public static String deviceName="";
    public static boolean isInDialog=false;
    //endregion

    //region Broadcast GPS_Update
    BroadcastReceiver gps_Update_Receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(WebSocket.isConnected)
                actionBar.setSubtitle("Connected");
            else
                actionBar.setSubtitle("notConnected");

            System.out.println("BroadcastReceiver --------------------------------------------------");

            // Aktuelle Position im Receiver setzen
            currentPos.setPosition(new LatLng(
                    intent.getDoubleExtra("y_coordinate", 1), intent.getDoubleExtra("x_coordinate", 1)
            ));
            // Kamera folgt dem Punkt zu aktueller Position
            if(followPoint)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos.getPosition()));

            // Gibt es einen Einsatz
            if(isEmergency){
                GMapDirections directions = new GMapDirections();
                LatLng start = currentPos.getPosition(); // aktuelle Position
                // Parst die Route in ein Objekt
                Document d = directions.getDocument(start, emergencyLatLng, "DRIVING");
                if(emergencyRoute!=null) {
                    emergencyRoute.remove(); // Route wird gelöscht
                    // um dann auf neue Position zu aktualisieren
                    emergencyRoute = mMap.addPolyline(new PolylineOptions().addAll(directions.getDirection(d)).color(R.color.wallet_holo_blue_light).width(12));
                    tvKM.setText("Verbleibende Kilometer: " + (double) directions.getDistanceValue(d) / 1000); // Restlichen Kilometer
                    tvLoc.setText("Einsatzort: " + directions.getEndAddress(d)); // Zielort


                        if(directions.getDistanceValue(d)<=50) {
                            emergencyRoute.remove();
                            isEmergency = false;

                            tvKM.setText("Verbleibende Kilometer: ");
                            tvLoc.setText("Einsatzort: ");
                        }

                }
            }
        }
    };
    //endregion

    // Broadcast Receiver
    //region Broadcasr newÉmergency
    BroadcastReceiver newEmergency=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            GMapDirections directions = new GMapDirections();
            LatLng start = currentPos.getPosition(); // Hier wird die Startposition gespeichert
            emergencyLatLng = new LatLng(intent.getDoubleExtra("x_coordinate", 1), intent.getDoubleExtra("y_coordinate", 1)); // Koordinaten zum Zielpunkt

            Gps_tracking.setTrackingEnabled(true); // GPS-Tracking wird eingeschaltet (über stopRoute im OptionsMenu ausschaltbar)
            invalidateOptionsMenu();
            isEmergency=true; // Es handelt sich um einen Einsatz

            Document d = directions.getDocument(start, emergencyLatLng, "DRIVING");
            if(emergencyRoute!=null)
            emergencyRoute.remove();
            emergencyRoute=mMap.addPolyline(new PolylineOptions().addAll(directions.getDirection(d)).color(R.color.wallet_holo_blue_light).width(12));
            tvKM.setText("Verbleibende Kilometer: " + (double) directions.getDistanceValue(d) / 1000);
            tvLoc.setText("Einsatzort: "+directions.getEndAddress(d));


        }
    };
//endregion

    //region Broadcast devicesLoaded
    BroadcastReceiver devicesLoaded= new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final Spinner spinner;

            final Dialog dialog = new Dialog(MapsActivity.this);
            dialog.setContentView(R.layout.dialog);
            dialog.setTitle("Fahrzeug auswählen:");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            spinner = (Spinner) dialog.findViewById(R.id.pickDeviceSpinner);
            spinner.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, WebSocket.getAllDevices()));

            ((Button)dialog.findViewById(R.id.dialogSave)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deviceName = (String) spinner.getSelectedItem();
                    Toast.makeText(getApplicationContext(), deviceName, Toast.LENGTH_SHORT).show();
                    WebSocket.getInstance(getApplication()).reconnect();
                    actionBar.setSubtitle("Connected");
                    dialog.dismiss();
                    isInDialog = false;
                }
            });
            if(!isInDialog)
            dialog.show();
            isInDialog=true;

        }
    };
//endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu,menu);
        if(Gps_tracking.isTrackingEnabled()){
            menu.findItem(R.id.stoproute).setVisible(true);
            menu.findItem(R.id.route).setVisible(false);
        }
        else
        {

            menu.findItem(R.id.stoproute).setVisible(false);
            menu.findItem(R.id.route).setVisible(true);
        }
        if(!followPoint){
            menu.findItem(R.id.followPoint).setVisible(true);
            menu.findItem(R.id.notFollowPoint).setVisible(false);
        }
        else
        {
            menu.findItem(R.id.followPoint).setVisible(false);
            menu.findItem(R.id.notFollowPoint).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.route:
                //ToDo: start Route
                Gps_tracking.setTrackingEnabled(true);
                WebSocket.getInstance(this.getApplication()).createNewRoute();
                break;
            case R.id.stoproute:
                if(emergencyRoute!=null)
                emergencyRoute.remove();
                isEmergency=false;

                Gps_tracking.setTrackingEnabled(false);
                tvKM.setText("Verbleibende Kilometer: ");
                tvLoc.setText("Einsatzort: ");

                final Dialog d = new Dialog(MapsActivity.this);
                d.setContentView(R.layout.finish_route_dialog);
                ((Button)d.findViewById(R.id.btDialogFinishRoute)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String destination=((TextView)d.findViewById(R.id.etDialogFinishRoute)).getText().toString();
                        WebSocket.getInstance(getApplication()).finishNewRoute(destination);
                        d.dismiss();
                    }
                });
                d.setCancelable(false);
                d.setCanceledOnTouchOutside(false);
                d.show();


                break;
            case R.id.followPoint:
                followPoint=true;

                break;
            case R.id.notFollowPoint:
                followPoint=false;
                break;
            case R.id.setName:
                WebSocket.getInstance(getApplication()).requestDeviceNames();


        }

        invalidateOptionsMenu();
        return true;
    }

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        // Registrieren bei beiden Broadcasts
            registerReceiver(gps_Update_Receiver, new IntentFilter("Gps_Update"));
        registerReceiver(newEmergency, new IntentFilter("newEmergency"));
        registerReceiver(devicesLoaded, new IntentFilter("devicesLoaded"));
            super.onCreate(savedInstanceState);
            setContentView(R.layout.map);
            setUpMapIfNeeded();

            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

            currentPos=mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("currentPos"));
            Intent service = new Intent(this,Gps_tracking.class);
            startService(service); // Tracking starten

            btG=(Button)findViewById(R.id.btG);
            btFB=(Button)findViewById(R.id.btFB);
        tvKM=(TextView)findViewById(R.id.tvKM);
        tvLoc=(TextView)findViewById(R.id.tvLoc);
        ((Button)findViewById(R.id.btSatellite)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        ((Button)findViewById(R.id.btNormal)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

            btG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    i = new Intent(getBaseContext(), EquipmentActivity.class);
                    startActivity(i);
                }
            });

            btFB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    i = new Intent(getBaseContext(), LogbookActivity.class);
                    startActivity(i);
                }
            });


        actionBar= getActionBar();
        actionBar.setSubtitle("Not Connected");

        }

        @Override
        protected void onResume() {
            super.onResume();
            setUpMapIfNeeded();
        }

        /**
         * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
         * installed) and the map has not already been instantiated.. This will ensure that we only ever
         * call {@link #setUpMap()} once when {@link #mMap} is not null.
         * <p/>
         * If it isn't installed {@link SupportMapFragment} (and
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
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gps_Update_Receiver);
        unregisterReceiver(newEmergency);
        unregisterReceiver(devicesLoaded);
    }
}
