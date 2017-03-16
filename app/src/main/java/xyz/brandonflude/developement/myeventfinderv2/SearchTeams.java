package xyz.brandonflude.developement.myeventfinderv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;
import static android.R.id.text1;
import static android.media.CamcorderProfile.get;
import static xyz.brandonflude.developement.myeventfinderv2.R.id.teamLogo;

public class SearchTeams extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextId;
    private Button buttonGet;
    private TextView textViewResult;
    private ListView showResults;

    private ProgressDialog loading;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_teams);

        editTextId = (EditText) findViewById(R.id.editTextId);
        buttonGet = (Button) findViewById(R.id.buttonGet);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        showResults = (ListView) findViewById(R.id.searchResults);

        buttonGet.setOnClickListener(this);
    }

    private void getData(){
        //Gets the users search term
        String id = editTextId.getText().toString().trim();
        //If null error
        if(id.equals("")){
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_LONG).show();
            return;
        }
        //Show a loading dialog while data from the server is being downloaded
        loading = ProgressDialog.show(this, "Please wait...", "Fetching...", false, false);
        String searchTerm = editTextId.getText().toString().trim();
        String url = "http://calendar.brandonflude.xyz/app/services/getTeams.php?team=" + searchTerm;
        //Get a response from the server
        StringRequest stringRequest = new StringRequest(url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response){
                loading.dismiss();
                try {
                    showJSON(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
           @Override
            public void onErrorResponse(VolleyError error) {
              Toast.makeText(SearchTeams.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
           }
         });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showJSON(String response) throws JSONException {
        //Sets up initial variables
        String id = "";
        String teamName = "";
        String logoURL = "";
        String league = "";
        //Creates an array of json data
        JSONArray jsonArray = new JSONArray(response);
        //Makes a list of JSON Objects
        final List<JSONObject> jsonObjects = new ArrayList<>();
        //Loop through the JSON Array to get each object
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            id = jsonObject.getString("team_id");
            teamName = jsonObject.getString("team_nm");
            logoURL = jsonObject.getString("team_logo_url");
            league = jsonObject.getString("league_nm");
            jsonObjects.add(jsonObject);
        }

        //Sets the ListView to show JSON Results
        //showResults = (ListView) findViewById(R.id.searchResults);
        showResults.setAdapter(new ListAdapter(this,jsonObjects));

        //Makes the list respond to clicks
        showResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                extras = getIntent().getExtras();
                String userID = extras.getString("userID");
                //Gets the position of the item in the list
                JSONObject pos = (JSONObject) showResults.getItemAtPosition(position);

                //Open up new page with details about the selected team.
                Intent i = new Intent(getBaseContext(), TeamInformation.class);
                try {
                    i.putExtra("userID", userID);
                    i.putExtra("teamID", pos.getString("team_id"));
                    i.putExtra("imgURL", pos.getString("team_logo_url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });
    }

    @Override
    public void onClick(View v){
        getData();
    }
}

/**
 * Created by Dave on 24/02/2017.
 */

//Adapter to converts JSON Objects into a List
class ListAdapter extends ArrayAdapter<JSONObject> {
    int vg;
    List<JSONObject> list;
    Context context;

    //Sets up initial Variables
    public ListAdapter(Context context, List<JSONObject> list){
        super(context,R.layout.activity_search_teams_row,list);
        this.context = context;
        this.list = list;
    }

    //Gets the view for where the results will show
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.activity_search_teams_row, parent, false);
        TextView teamName=(TextView)itemView.findViewById(R.id.teamName);
        ImageView logoURL=(ImageView) itemView.findViewById(teamLogo);
        TextView textLeague = (TextView)itemView.findViewById(R.id.leagueName);

        try{
            teamName.setText(list.get(position).getString("team_nm"));
            URL imgURL = new URL(list.get(position).getString("team_logo_url"));
            new DownloadImageTask((ImageView) itemView.findViewById(R.id.teamLogo))
                    .execute(list.get(position).getString("team_logo_url"));
            textLeague.setText(list.get(position).getString("league_nm"));
        }
        catch (JSONException e){
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return itemView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
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