package xyz.brandonflude.developement.myeventfinderv2;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DateInformation extends AppCompatActivity {
    Bundle extras;
    String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_information);

        extras = getIntent().getExtras();
        date = extras.getString("date");
        TextView temp = (TextView) findViewById(R.id.date_picked);
        temp.setText(date);
    }
}
