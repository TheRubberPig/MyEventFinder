package xyz.brandonflude.developement.myeventfinderv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.BreakIterator;
import java.util.concurrent.ExecutionException;

import static android.R.attr.data;
import static android.R.id.list;
import static android.R.id.switch_widget;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.fixturePlayed;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.fixtureTime;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.fixtureLeague;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.fixtureVenue;

public class TeamInformation extends AppCompatActivity {
    Bundle extras;
    String teamID = "";
    String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_information);

        extras = getIntent().getExtras();
        teamID = extras.getString("teamID");
        userID = extras.getString("userID");
        String imgURL = extras.getString("imgURL");
        new DownloadImage((ImageView) findViewById(R.id.logo)).execute(imgURL);
        LinearLayout listings = (LinearLayout) findViewById(R.id.FixtureListings);
        TextView descView = (TextView) findViewById(R.id.description);
        try {
            String desc = new GetData().execute("desc").get();
            descView.setText(desc);
            String fixtureInfo = new GetData().execute("").get();
            if(fixtureInfo.equals("false")){
                TextView noFixtures = (TextView) findViewById(R.id.noFixtures);
                listings.setVisibility(View.GONE);
                noFixtures.setText("No upcoming fixtures");
            }
            else
            {
                TextView fixturePlayed = (TextView) findViewById(R.id.fixturePlayed);
                TextView fixtureTime = (TextView) findViewById(R.id.fixtureTime);
                TextView fixtureVenue = (TextView) findViewById(R.id.fixtureVenue);
                TextView fixtureLeague = (TextView) findViewById(R.id.fixtureLeague);
                String[] allFixtures = fixtureInfo.split("\\|");
                String[] parts = allFixtures[0].split(",");

                // Format the data fetched from the server to be more user friendly
                String dateInfo = parts[2];
                String[] splitInfo = dateInfo.split(" ");
                String date = splitInfo[0];
                String time = splitInfo[1];
                String[] splitDate = date.split("-");
                String[] splitTime = time.split(":");
                String month = splitDate[1];
                String day = splitDate[2];
                String hour = splitTime[0];
                String mins = splitTime[1];

                // Set strings that need to be displayed
                String fixtureTeams;
                String fixtureStart = day + "/" + month + " at " + hour + ":" + mins;
                String venueName = parts[4];
                String leagueName = parts[5];

                if(leagueName.equals("Premier League"))
                {
                    fixtureTeams = parts[0] + " vs. " + parts[1];
                }
                else
                {
                    fixtureTeams = parts[1] + " @ " + parts[0];
                }


                // Fill the containers with parsed data
                fixturePlayed.setText(fixtureTeams);
                fixtureTime.setText(fixtureStart);
                fixtureVenue.setText(venueName);
                fixtureLeague.setText(leagueName);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void addTeam(View view){
        try{
            String result = new GetData().execute("add").get();
            Context context = getApplicationContext();
            switch(result){
                case "true":
                    Toast toast = Toast.makeText(context, "Team added!", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case "false":
                    Toast toastF = Toast.makeText(context, "An unexpected error occurred", Toast.LENGTH_LONG);
                    toastF.show();
                    break;

                case "exists":
                    Toast toastE = Toast.makeText(context, "Team already added!", Toast.LENGTH_LONG);
                    toastE.show();
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void removeTeam(View view){
        try{
            String result = new GetData().execute("remove").get();
            Context context = getApplicationContext();
            switch(result){
                case "true":
                    Toast toast = Toast.makeText(context, "Team removed!", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case "false":
                    Toast toastF = Toast.makeText(context, "An unexpected error occurred", Toast.LENGTH_LONG);
                    toastF.show();
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    class GetData extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            //Sets up the connection and result
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                //Connects to the server using the users details (Password is encrypted before hand)
                URL url;
                switch(params[0]){
                    case "desc":
                        url = new URL("http://calendar.brandonflude.xyz/app/services/descriptions.php?team-id=" + teamID);
                        break;
                    case "add":
                        url = new URL("http://calendar.brandonflude.xyz/app/services/addTeam.php?user-id=" + userID + "&team-id=" + teamID);
                        break;
                    case "remove":
                        url = new URL("http://calendar.brandonflude.xyz/app/services/removeTeam.php?user-id=" + userID + "&team-id=" + teamID);
                        break;
                    case "":
                        url = new URL("http://calendar.brandonflude.xyz/app/services/summary.php?team-id=" + teamID);
                        break;
                    default:
                        url = null;
                        break;
                }
                //Opens the connection
                urlConnection = (HttpURLConnection) url.openConnection();

                //Makes sure the server is running and accepting connections
                int code = urlConnection.getResponseCode();

                //If the server is up read the text.
                if(code==200){
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

                //Return the result
                return result;

                //Catch errors if something unexpected happens
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Close the connection
            finally {
                urlConnection.disconnect();
            }

            //Returns null if unsuccessful
            return result;

        }
    }
}

class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImage(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}