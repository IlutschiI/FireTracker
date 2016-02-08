package at.htlleonding.firetracker.listViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import at.htlleonding.firetracker.entities.Equipment;
import at.htlleonding.firetracker.R;

public class ListViewAdapterEquipment extends ArrayAdapter<Equipment> {
    private Context context;
    private List<Equipment> equipments;

    public ListViewAdapterEquipment(Context context, List<Equipment> equipments) {
        super(context, R.layout.listvieweuqip, equipments);
        this.context = context;
        this.equipments = equipments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listvieweuqip, parent, false);

        TextView tvName = (TextView) rowView.findViewById(R.id.tvEquiName);
        Equipment equipment = equipments.get(position);

        tvName.setText(equipment.getEquipName());
        return rowView;
    }
}
