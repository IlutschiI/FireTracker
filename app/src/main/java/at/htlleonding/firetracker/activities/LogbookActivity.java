package at.htlleonding.firetracker.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import at.htlleonding.firetracker.R;
import at.htlleonding.firetracker.details.DetailLogbook;
import at.htlleonding.firetracker.entities.Logbook;
import at.htlleonding.firetracker.listViewAdapter.ListViewAdapterLogbook;
import at.htlleonding.firetracker.webSocket.WebSocket;

public class LogbookActivity extends Activity {

    ListView lv; // ListView aller Einträge
    EditText etFilterLogs; // Filtern nach einem Eintrag
    Intent i; // Intent i
    ArrayList<Logbook> list;

    // BroadcastReceiver
    BroadcastReceiver onLoadedFinish = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            list.clear();
            list.addAll(WebSocket.getAllLogBooks()); //Es werden alle Fahrtenbuch-Einträge der Liste hinzugefügt
            refreshListView(list); // Aktualisieren der ListView
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Registrieren des Broadcast-Receivers auf die Aktion "RoutesLoaded"
        registerReceiver(onLoadedFinish, new IntentFilter("RoutesLoaded"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logbooklay);
        list = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listView);
        etFilterLogs = (EditText) findViewById(R.id.etFilterLogs);

        // Stellt Request an WebSocket-Server, dass er alle Routen senden soll
        WebSocket.getInstance(getApplication()).requestRoutes();

        // Wenn ein Element in der ListView gedrückt wird
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Logbook l = (Logbook)lv.getItemAtPosition(position); // Element speichern, auf das geklickt wurde
                i = new Intent(getBaseContext(), DetailLogbook.class); // Intent an DetailLogbook, um alle Informationen über den Einsatz zu sehen
                JSONArray json = new JSONArray();
                JSONObject obj;
                // Es wird die Liste von Koordinaten meines gedrückten Einsatzes durchlaufen
                for (LatLng lat:l.getListOfCoords()) {
                    obj=new JSONObject(); // Ein Objekt besteht aus einer Latitude und einer Longitude
                    try {
                        // Dem Objekt werden ein Koordinaten-Paar hinzugefügt
                        obj.put("lat",lat.latitude);
                        obj.put("lng",lat.longitude);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    json.put(obj); // Das Objekt wird dem Array hinzugefügt
                }
                i.putExtra("logBook", json.toString()); // Dem Intent wird als Extra der vorher zusammengestellte Json als String übergeben
                i.putExtra("destination",l.getDestination());
                startActivity(i);
            }
        });

        refreshListView(list);
        // Filter
        etFilterLogs.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Logbook> logs = new ArrayList<>();
                // Sucht alle Elemente in der Liste, die den Buchstaben im Filter in dem Zielort enthalten
                for (Logbook item : list) {
                    if (String.valueOf(item.getDestination().toUpperCase()).contains(s.toString().toUpperCase())) {
                        logs.add(item); // Alle Treffer werden in eine Liste gespeichert...
                    }
                }
                refreshListView(logs);//... und dann angezeigt
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Mithilfe des Adapters wird die Liste angezeigt
    public void refreshListView(List<Logbook> list) {
        final ListViewAdapterLogbook adapter = new ListViewAdapterLogbook(this, list);
        lv.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onLoadedFinish); // Mehrfach-Registrierung verhindern
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebSocket.getInstance(getApplication()).requestRoutes(); // Routen werden geladen
    }
}
