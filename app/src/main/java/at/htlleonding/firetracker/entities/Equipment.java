package at.htlleonding.firetracker.entities;

import android.app.Activity;
import android.content.Intent;
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
import at.htlleonding.firetracker.listViewAdapter.ListViewAdapterEquipment;

public class Equipment {


    String equipName;
    String equipDesc;

    public Equipment(String equipName, String equipDesc) {
        this.equipName = equipName;
        this.equipDesc = equipDesc;
    }

    public Equipment() {
    }


    public String getEquipName() {
        return equipName;
    }

    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }

    public String getEquipDesc() {
        return equipDesc;
    }

    public void setEquipDesc(String equipDesc) {
        this.equipDesc = equipDesc;
    }
}

