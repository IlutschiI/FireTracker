package at.htlleonding.firetracker.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import at.htlleonding.firetracker.R;
import at.htlleonding.firetracker.details.DetailEquipment;
import at.htlleonding.firetracker.entities.Equipment;
import at.htlleonding.firetracker.listViewAdapter.ListViewAdapterEquipment;
import at.htlleonding.firetracker.webSocket.WebSocket;

public class EquipmentActivity extends Activity {

    ListView lv;
    EditText etFilterEquip;
    Intent i;

    ArrayList<Equipment> list=new ArrayList<>();

    // BroadcastReceiver
    BroadcastReceiver onLoadedFinish = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            list.clear();
            list.addAll(WebSocket.getAllEquipments()); //Es werden alle Geräte der Liste hinzugefügt
            refreshListView(list); // Aktualisieren der ListView
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Registrieren des Broadcast-Receivers auf die Aktion "EquipmentsLoaded"
        registerReceiver(onLoadedFinish, new IntentFilter("EquipmentsLoaded"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equipmentlay);

        lv=(ListView)findViewById(R.id.listView);

        // Wenn auf ein Element in der ListView gedrückt wird
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lv.getSelectedItem();
                Equipment actEquipment= (Equipment)lv.getItemAtPosition(position);
                i = new Intent(getBaseContext(),DetailEquipment.class);
                Bundle b = new Bundle();
                // Wird ein Intent mit Extra an die Detail Klasse des Geräts geschickt
                i.putExtra("name", actEquipment.getEquipName());
                i.putExtra("desc", actEquipment.getEquipDesc());
                // Starten der DetailView
                startActivity(i);
            }
        });

        refreshListView(list);
        etFilterEquip = (EditText)findViewById(R.id.etFilterEquip);

        // Listener auf die Filtereingabe
        etFilterEquip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Equipment> equips = new ArrayList<>();

                // Sucht Elemente, die selben String wie der Filter enthalten
                for (Equipment item : list) {
                    if (String.valueOf(item.getEquipName().toUpperCase()).contains(s.toString().toUpperCase())) {
                        equips.add(item);
                    }
                }
                refreshListView(equips);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });}

    public void refreshListView(List <Equipment> list){
        final ListViewAdapterEquipment adapter = new ListViewAdapterEquipment(this, list);
        lv.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onLoadedFinish); // ist dazu da, um Mehrfach-Registrierung zu verhindern
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebSocket.getInstance(getApplication()).requestMachines(); // Maschinen werden geladen
    }
}
