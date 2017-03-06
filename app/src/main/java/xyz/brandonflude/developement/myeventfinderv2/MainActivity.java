package xyz.brandonflude.developement.myeventfinderv2;


import android.content.Intent;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.timessquare.CalendarPickerView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
{
    Bundle extras;
    String userID = "";

    String _UserID;
    CalendarPickerView calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showRelevantDates showDates = new showRelevantDates();
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

        try{
            for(int i = 0; i < 28; i++)
            {
                if(!(showDates.execute(_UserID, String.valueOf(i)).get().equals("[]")))
                {
                    Log.e("ee","ee");
                }
            }
        }
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
        catch(ExecutionException ee)
        {
            ee.printStackTrace();
        }

        //calendar.highlightDates(userDates);
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
}

class showRelevantDates extends AsyncTask<String,Void,String>
{
    protected String doInBackground(String ...params)
    {
        List<Date> userDates = new ArrayList<>();
        Date thisTime = new Date();
        HttpURLConnection urlConnection = null;
        String result = "";
        String UserID = params[0];
        String dateString = "2017-02-0";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            thisTime = format.parse(dateString);
            userDates.add(thisTime);
        }
        catch(ParseException pe)
        {
            pe.printStackTrace();
        }

        try
        {
            URL qUrl = new URL("http://calendar.brandonflude.xyz/app/services/getFixtures.php?user-id=8&date=2017-02-0");

            urlConnection = (HttpURLConnection) qUrl.openConnection();

            //Makes sure the server is running and accepting connections
            int code = urlConnection.getResponseCode();

            //If the server is up read the text.
            if (code == 200) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if (in != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = "";

                    //While the reader is not null store in result (For us it should always only be one word)
                    while ((line = bufferedReader.readLine()) != null)
                        result += line;
                }
                //Close the input stream.
                in.close();
            }
        }
        catch (MalformedURLException mue)
        {
            mue.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return result;
    }

    protected void onPostExecute(String result)
    {
        returnMethod(result);
    }

    private String returnMethod(String res)
    {
        return res;
    }

}
