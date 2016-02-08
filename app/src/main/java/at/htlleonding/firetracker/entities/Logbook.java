package at.htlleonding.firetracker.entities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.LocaleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import at.htlleonding.firetracker.R;
import at.htlleonding.firetracker.details.DetailLogbook;
import at.htlleonding.firetracker.listViewAdapter.ListViewAdapterLogbook;

public class Logbook implements Serializable {


    List<LatLng> listOfCoords; // Liste von Koordinaten
    String destination; // Zielort
    Date date; // Datum
    Integer missionNr; // ID


    public Logbook(Integer missionNr, String destination, Date date, List<LatLng> listOfCoords) {
        this.missionNr=missionNr;
        this.destination = destination;
        this.date = date;
        this.listOfCoords = listOfCoords;
    }

    public Logbook() {
    }

    public Integer getNr() {
        return missionNr;
    }
    public void setNr(Integer missionNr){
        this.missionNr=missionNr;
    }

    public List<LatLng> getListOfCoords() {
        return listOfCoords;
    }

    public void setListOfCoords(List<LatLng> listOfCoords) {
        this.listOfCoords = listOfCoords;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
