package xyz.brandonflude.developement.myeventfinderv2;

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
import java.util.concurrent.ExecutionException;

import static android.R.attr.data;
import static android.R.id.list;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.awayTeam;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.homeTeam;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.league;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.location;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.stadium;

public class TeamInformation extends AppCompatActivity {
    Bundle extras;
    String teamID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_information);

        extras = getIntent().getExtras();
        teamID = extras.getString("teamID");
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
                TextView homeTeam = (TextView) findViewById(R.id.homeTeam);
                TextView awayTeam = (TextView) findViewById(R.id.awayTeam);
                TextView startTime = (TextView) findViewById(R.id.fixtureStart);
                TextView stadium = (TextView) findViewById(R.id.stadium);
                TextView location = (TextView) findViewById(R.id.location);
                TextView league = (TextView) findViewById(R.id.league);
                String[] parts = fixtureInfo.split(",");
                homeTeam.setText(parts[0]);
                awayTeam.setText(parts[1]);
                startTime.setText(parts[2]);
                stadium.setText(parts[4]);
                location.setText(parts[5]);
                league.setText(parts[6]);
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
                if(params[0].equals("desc"))
                {
                    url = new URL("http://calendar.brandonflude.xyz/app/services/descriptions.php?team-id=" + teamID);
                }
                else if(params[0].equals(""))
                {
                     url = new URL("http://calendar.brandonflude.xyz/app/services/summary.php?team-id=" + teamID);
                }
                else
                {
                    //Should never get here
                    url = null;
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