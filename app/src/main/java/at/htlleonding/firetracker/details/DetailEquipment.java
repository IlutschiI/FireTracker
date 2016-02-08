package at.htlleonding.firetracker.details;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import at.htlleonding.firetracker.R;

public class DetailEquipment extends Activity {

    TextView tvName;
    TextView tvUseage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailequiplayout);

        tvName=(TextView) findViewById(R.id.tvMachine);
        tvUseage=(TextView) findViewById(R.id.tvUseage);

        tvName.setText(getIntent().getStringExtra("name"));
        tvUseage.setText(getIntent().getStringExtra("desc"));

    }
}
