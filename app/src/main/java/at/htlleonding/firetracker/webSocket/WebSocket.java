package at.htlleonding.firetracker.webSocket;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import at.htlleonding.firetracker.Services.Gps_tracking;
import at.htlleonding.firetracker.entities.Equipment;
import at.htlleonding.firetracker.entities.Logbook;
import at.htlleonding.firetracker.mapActivity.MapsActivity;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketOptions;

public class WebSocket {

    public static boolean isConnected=false;

    //Singleton Instanz
    static WebSocket instance;
    private final static String ip="firetrackerffo.no-ip.org:8080/Firetracker/endpoint/";
    // Wird benötigt, um den Broadcast zu senden
    Application c;
    // WebSocketConnection Variable
    final WebSocketConnection mConnection;
    WebSocketHandler webSocketHandler;
    //Statische Liste mit DeviceNamen
    static List<String> allDevices;
    // Statische Liste mit allen Routen
    static List<Logbook> allLogBooks;
    // Statische Liste mit allen Geräten
    static List<Equipment> allEquipments;
    //Konstruktor
    private WebSocket(Application context){

        webSocketHandler= new WebSocketHandler(){



            @Override
            public void onOpen() {
                System.out.println("On Open:----Connection is open?  "+mConnection.isConnected()+"------");
                isConnected=true;
                new Handler(c.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(c.getBaseContext(), "connected", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onTextMessage(String payload) {

                try {
                    // Erstellen eines JsonObjects mit der übergebenen Message
                    JSONObject json = new JSONObject(payload);
                    // Welcher Diskriminator steht im JsonObject (Eindeutige Identifizierung, welche Art von Info der Server schickt)
                    switch(json.optInt("diskriminator")){


                        case 2: //ist er 2, wird ein Broadcast gesendet, dieser aktualisiert Route zu Einsatzort (mehr Infos beim B-Receiver)
                            Intent emergencyIntent = new Intent("newEmergency");
                            emergencyIntent.putExtra("x_coordinate",json.optDouble("x_coordinate"));
                            emergencyIntent.putExtra("y_coordinate",json.optDouble("y_coordinate"));
                            c.sendBroadcast(emergencyIntent);
                            break;


                        case 3://ist er 3, wird eine Liste von Geräten gespeichert
                            allEquipments.clear();
                            JSONArray machines = json.getJSONArray("machines");
                            String equipDes;
                            String equipName;
                            JSONObject object;

                            // Es werden die Daten aus dem JsonArray in ein Objekt gespeichert und in eine Liste hinzugefügt
                            for(int i=0; i<machines.length();i++){
                                object=machines.getJSONObject(i);
                                equipDes=object.optString("useage");
                                equipName=object.optString("name");

                                Equipment equip = new Equipment(equipName,equipDes);
                                allEquipments.add(equip);
                            }
                            Intent intentEquip = new Intent("EquipmentsLoaded");
                            //...und per Broadcast gesendet
                            c.sendBroadcast(intentEquip);
                            break;
                        case 10: // ist er 10, bedeutet das, dass der Server das Tracking ein- (true) oder ausschalten (false) möchte
                            Gps_tracking.setTrackingEnabled(json.optBoolean("tracking",true));
                            break;
                        case 11:
                            JSONArray devices = json.optJSONArray("devices");
                            allDevices.clear();
                            for (int i =0;i<devices.length();i++){
                                allDevices.add(devices.getString(i));
                                System.out.println("devicename:    "+devices.getString(i));
                            }
                            if(!MapsActivity.isInDialog) {
                                Intent devicesLoadedIntent = new Intent("devicesLoaded");
                                c.sendBroadcast(devicesLoadedIntent);
                            }

                        case 6: // ist er 6, wird in eine Liste von Fahrtenbuch-Einträgen alle Routen gespeichert
                            // Leeren der Liste, um keine Routen doppelt in der Liste zu haben
                            allLogBooks.clear();
                            JSONArray routes = json.getJSONArray("routes"); // JsonArray mit allen Routen
                            JSONObject obj;
                            String destination; // Zielort
                            int id; // ID
                            Date date; // Datum
                            int day; // Tag
                            int month; // Monat
                            int year; // Jahr
                            JSONObject dateJson; // JsonObject des Datums
                            JSONArray gpsData; // Json Array aller GPS-Daten
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy"); // Format des Datum

                            // Alle Details der Routen inklusive GPS-Daten im JsonArray durchgehen
                            for (int i = 0;i<routes.length();i++){
                                obj=routes.getJSONObject(i); // Eine Route
                                destination=obj.optString("destination"); // Zielort aus der Route
                                id=obj.getInt("id"); // Id der Route
                                dateJson=obj.optJSONObject("date"); // JsonObject des Datums der Route
                                day=dateJson.optInt("day"); // Tag der Route vom JsonObject des Datums
                                month=dateJson.optInt("month"); // Monat der Route vom JsonObject des Datums
                                year=dateJson.optInt("year"); // Jahr der Route vom JsonObject des Datums
                                // Datum dann aus Tag/Monat/Jahr zusammensetzen
                                date=format.parse(day + "/" + month + "/" + year);
                                gpsData=obj.getJSONArray("gps_datas"); // GPS-Daten in Array speichern (Longitude, Latitude)
                                // Es wird ein neues Logbook (Ein Fahrtenbuch-Eintrag) mit den Daten aus einer Route instanziert
                                // Außerdem wird eine leere Liste von Gps-Daten übergeben, die in der nächsten For-Schleife befüllt wird
                                Logbook logBook = new Logbook(id,destination,date,new LinkedList<LatLng>());
                                // Alle GPS-Daten durchlaufen
                                for (int j=0;j<gpsData.length();j++){
                                    // Neues Objekt von LatLng, die einzelnen GPSDaten werden gespeichert und der Liste hinzugefügt
                                    LatLng pos = new LatLng(gpsData.getJSONObject(j).optDouble("x_coor"),gpsData.getJSONObject(j).optDouble("y_coor"));
                                    logBook.getListOfCoords().add(pos);
                                }
                                // Ein Fahrtenbuch-Eintrag wird der Liste von Fahrtenbuch-Einträgen hinzugefügt
                                if(logBook.getListOfCoords().size()!=0)
                                allLogBooks.add(logBook);

                            }
                            // Sind Fahrtenbucheinträge hinzugefügt, wird ein Intent erstellt
                            Intent intent = new Intent("RoutesLoaded");
                            //...und per Broadcast gesendet
                            c.sendBroadcast(intent);
                            break;
                    }

                } catch (JSONException e) {
                   // e.printStackTrace();
                } catch (ParseException e) {
                   // e.printStackTrace();
                }

            }

            @Override
            public void onClose(int code, String reason) {
                isConnected=false;
            }
        };

        // Instanzieren der Liste aller Fahrtenbuch-Einträge
        allLogBooks=new LinkedList<>();
        // Instanzieren der Liste aller Geräte
        allEquipments=new LinkedList<>();
        // Application wird auf den übergebenen Context im Konstruktor gesetzt
        allDevices=new LinkedList<>();
        c=context;
        // Instanzieren der WebSocket Connection
        mConnection = new WebSocketConnection();
      /* try {
            // Verbinden mid dem Server
           // mConnection.connect("ws://"+ip+":8080/Firetracker/endpoint/KDO",webSocketHandler);
        } catch (WebSocketException e) {
            e.printStackTrace();
        }*/
    }

    //Statische Methode für die Liste aller Fahrtenbuch-Einträge
    public static List<Logbook> getAllLogBooks() {
        return allLogBooks;
    }
    // Statische Methode für die Liste aller Geräte
    public static List<Equipment> getAllEquipments(){
        return allEquipments;
    }

    public static List<String> getAllDevices() {
        return allDevices;
    }

    //Ist ein Singleton, Application ist notwendig, um den Broadcast zu senden
    public static WebSocket getInstance(final Application c){
        if(instance==null)
            instance=new WebSocket(c);
        if(!isConnected||!instance.mConnection.isConnected()){
            try {
                new Handler(c.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(c.getBaseContext(), "no connection to server, trying to reconnect...", Toast.LENGTH_LONG).show();
                    }
                });
               instance.mConnection.connect("ws://"+ip+ MapsActivity.deviceName,instance.webSocketHandler);

            } catch (WebSocketException e) {
                e.printStackTrace();
            }
        }

        if(!isConnected||!instance.mConnection.isConnected())
            instance.mConnection.disconnect();
        System.out.println("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-"+isConnected);
        System.out.println("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-"+ip+MapsActivity.deviceName);

        return instance;
    }

    // Sende Server aktuelle Position (X-Coordinate, Y-Coordinate und Speed)
    public void sendLocation(Location location){
        if(mConnection.isConnected()) {
            mConnection.sendTextMessage("{diskriminator:1,x_coordinate:" + location.getLatitude() + ",y_coordinate:" + location.getLongitude() + ",velocity:" + location.getSpeed() + "}");
            Toast.makeText(c,"x_coordinate:" + location.getLatitude() + ",y_coordinate:" + location.getLongitude(),Toast.LENGTH_LONG).show();
        }
    }
    // Sende Server, dass er neue Route anlegen soll
    public void createNewRoute(){
        if(mConnection.isConnected()) {
            mConnection.sendTextMessage("{diskriminator:5}");
            Gps_tracking.setTrackingEnabled(true);
        }
    }

    public void finishNewRoute(String destination){
        if(mConnection.isConnected()) {
            mConnection.sendTextMessage("{diskriminator:7, destination:\" "+destination+" \"}");
            //Gps_tracking.setTrackingEnabled(true);
        }
    }

    // Sende Server, dass er alle Routen übertragen soll
    public void requestRoutes(){
        if(mConnection.isConnected()) {
            mConnection.sendTextMessage("{diskriminator:6}");
        }
    }
    // Sende Server, dass er alle Geräte übertragen soll
    public void requestMachines(){
        if(mConnection.isConnected()){
            mConnection.sendTextMessage("{diskriminator:3}");
        }
    }

    // Sende Server, dass er alle DeviceNamen übertragen soll
    public void requestDeviceNames(){
        if(mConnection.isConnected()){
            mConnection.sendTextMessage("{diskriminator:11}");
        }
    }

    public void reconnect(){
        mConnection.disconnect();
        try {
            mConnection.connect(ip+MapsActivity.deviceName,webSocketHandler);
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }


}
