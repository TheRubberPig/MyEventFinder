package xyz.brandonflude.developement.myeventfinderv2;


import android.content.Intent;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Toast;

import com.squareup.timessquare.CalendarPickerView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    CalendarPickerView calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        //Find the calendar view
        calendar = (CalendarPickerView) findViewById(R.id.calendar_grid);
        //Get min date I.E:Today
        Date today = new Date();
        //Get max date, currently 1 year forward and initalize the calendar
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today);
    }

    public void dateClick(View view)
    {
        Date selectedDate = calendar.getSelectedDate();
        String trimmedDate = selectedDate.toString();
        trimmedDate = trimmedDate.substring(0, Math.min(trimmedDate.length(), 10));
        Intent i = new Intent(getApplicationContext(), DateInformation.class);
        i.putExtra("date", trimmedDate);
        startActivity(i);
    }

    public void checkData()
    {
        
    }
}
