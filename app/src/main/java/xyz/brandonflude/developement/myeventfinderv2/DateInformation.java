package xyz.brandonflude.developement.myeventfinderv2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import static xyz.brandonflude.developement.myeventfinderv2.R.id.awayTeamLogo;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.endTime;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.homeTeamLogo;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.teamLogo;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.teamName;

public class DateInformation extends AppCompatActivity {
    //Sets up variables that are needed across methods
    Bundle extras;
    String date;
    String userID;

    private ListView showResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set View
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_information);

        //Get variables from previous intent
        extras = getIntent().getExtras();
        date = extras.getString("date");
        userID = extras.getString("userID");

        //Sets up list
        showResults = (ListView) findViewById(R.id.dateResults);
        final TextView noEvents = (TextView)findViewById(R.id.noEventsText);

        ////Fetches data from the server
        String url = "http://calendar.brandonflude.xyz/app/services/getFixtures.php?user-id=" + userID + "&date=" + date;
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //Shows the data that the server returns in a user friendly fashion
                    if(response.equals("\nfalse"))
                    {
                        //Show text if the date has no events.
                        noEvents.setText("No events to show on this date");
                    }
                    else
                    {
                        showJSON(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DateInformation.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void showJSON(String response) throws JSONException {
        //Creates an array of json data
        JSONArray jsonArray = new JSONArray(response);
        //Makes a list of JSON Objects
        final List<JSONObject> jsonObjects = new ArrayList<>();
        //Loop through the JSON Array to get each object
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            jsonObjects.add(jsonObject);
        }

        //Sets the ListView to show JSON Results
        //Also calls the list adapter class below this method
        showResults.setAdapter(new ListAdapter(this, jsonObjects));
    }

    class ListAdapter extends ArrayAdapter<JSONObject> {
        int vg;
        List<JSONObject> list;
        Context context;

        //Sets up initial Variables
        public ListAdapter(Context context, List<JSONObject> list){
            super(context,R.layout.activity_date_information_row,list);
            this.context = context;
            this.list = list;
        }

        //Gets the view for where the results will show
        public View getView(int position, View convertView, ViewGroup parent){
            //Sets up the card view within the list view
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.activity_date_information_row, parent, false);

            //All the views that need to be filled in with data from the server
            TextView teamName=(TextView)itemView.findViewById(R.id.homeTeamName);
            TextView awayTeamName = (TextView)itemView.findViewById(R.id.awayTeamName);
            TextView textLeague = (TextView)itemView.findViewById(R.id.league);
            TextView gameLocation = (TextView)itemView.findViewById(R.id.gameLocation);
            TextView startTime = (TextView)itemView.findViewById(R.id.startTime);
            TextView endTime = (TextView)itemView.findViewById(R.id.endTime);
            TextView vsText = (TextView)itemView.findViewById(R.id.vsText);

            try{
                //Gets the current league
                String league = list.get(position).getString("league_nm");

                //Sets the formatting depending on the league
                if(league.equals("Premier League"))
                {
                    //Set home and away team text views
                    teamName.setText(list.get(position).getString("home_team_name"));
                    awayTeamName.setText(list.get(position).getString("away_team_name"));
                    //Set middle text
                    vsText.setText("VS");
                    //*******These download images from a URL********
                    new DownloadImageTask((ImageView) itemView.findViewById(R.id.homeTeamLogo))
                            .execute(list.get(position).getString("home_team_logo_url"));
                    new DownloadImageTask((ImageView) itemView.findViewById(R.id.awayTeamLogo))
                            .execute(list.get(position).getString("away_team_logo_url"));
                    //************************************************
                }
                else
                {
                    //Swap home and away team around to match official formatting
                    teamName.setText(list.get(position).getString("away_team_name"));
                    awayTeamName.setText(list.get(position).getString("home_team_name"));
                    //Set Middle text
                    vsText.setText("@");
                    //*******These download images from a URL********
                    new DownloadImageTask((ImageView) itemView.findViewById(R.id.homeTeamLogo))
                            .execute(list.get(position).getString("away_team_logo_url"));
                    new DownloadImageTask((ImageView) itemView.findViewById(R.id.awayTeamLogo))
                            .execute(list.get(position).getString("home_team_logo_url"));
                    //************************************************
                }
                //Set league view and location
                textLeague.setText(list.get(position).getString("league_nm"));
                gameLocation.setText(list.get(position).getString("venue_nm"));

                //Splits the time string into a more user friendly format and then sets it
                String startTimeString = (list.get(position).getString("fixture_start_dt")).substring(10);
                startTime.setText(startTimeString);
                String endTimeString = (list.get(position).getString("fixture_end_dt")).substring(10);
                endTime.setText(endTimeString);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            return itemView;
        }

        //Gets the size of the list
        @Override
        public int getCount() {
            return list.size();
        }

        //Gets the current position in the list
        @Override
        public JSONObject getItem(int position) {
            return list.get(position);
        }

        //Gets the ID of the item in the list
        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}


