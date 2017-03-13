package xyz.brandonflude.developement.myeventfinderv2;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DateInformation extends AppCompatActivity {
    //Meh
    Bundle extras;
    String date;
    String userID;

    private String mDataset;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_information);

        extras = getIntent().getExtras();
        date = extras.getString("date");
        userID = extras.getString("userID");
        TextView temp = (TextView) findViewById(R.id.date_picked);
        temp.setText(date);
        //TODO: Query for fixtures on a specific date and then show them in a list.
        String result = "";
        try {
            result = new getInformation().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    class getInformation extends AsyncTask<String,Void,String>
    {
        protected String doInBackground(String ...params)
        {
            Date thisTime = new Date();
            HttpURLConnection urlConnection = null;
            String result = "";
            String dateString = date;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try
            {
                thisTime = format.parse(dateString);
            }
            catch(ParseException pe)
            {
                pe.printStackTrace();
            }

            try
            {
                URL qUrl = new URL("http://calendar.brandonflude.xyz/app/services/getFixtures.php?user-id=" + userID + "&date=2017-03-");

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
}


