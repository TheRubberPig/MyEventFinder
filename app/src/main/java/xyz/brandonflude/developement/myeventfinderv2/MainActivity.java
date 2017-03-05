package xyz.brandonflude.developement.myeventfinderv2;


import android.content.Intent;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    Bundle extras;
    String userID = "";

    CalendarPickerView calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView user = (TextView) findViewById(R.id.show_username);
        extras = getIntent().getExtras();
        if(extras.getString("username").equals(""))
        {
            user.setText("No username found");
        }
        else
        {
            user.setText("Welcome " + extras.getString("username"));
        }
        userID = extras.getString("userID");
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

    public void searchTeams(View view)
    {
        Intent i = new Intent(getApplicationContext(), SearchTeams.class);
        startActivity(i);
    }

    public void showRelevantDates()
    {
        List<Date> userDates = new ArrayList<>();

        calendar.highlightDates(userDates);

        /*for (Date currDate : userDates)
        {
            calendar.highlightDates();
        }*/

        String dateString = "Dec 12, 2017 9:20 PM";
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        try {
            Date thisTime = format.parse(dateString);

        }
        catch(ParseException pe)
        {

        }

    }
}
