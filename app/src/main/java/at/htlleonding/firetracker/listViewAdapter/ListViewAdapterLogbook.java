package at.htlleonding.firetracker.listViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import at.htlleonding.firetracker.entities.Logbook;
import at.htlleonding.firetracker.R;

public class ListViewAdapterLogbook extends ArrayAdapter<Logbook> {
    private Context context;
    private List<Logbook> logs;

    public ListViewAdapterLogbook(Context context, List<Logbook> logs) {
        super(context, R.layout.listviewlog, logs);
        this.context = context;
        this.logs = logs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listviewlog, parent, false);
        if(1==1){

        }
        else{}


        TextView tvName = (TextView) rowView.findViewById(R.id.textViewLogs);
        Logbook log = logs.get(position);

        tvName.setText(log.getDestination()+", "+SimpleDateFormat.getInstance().format(log.getDate()));
        return rowView;
    }
}
