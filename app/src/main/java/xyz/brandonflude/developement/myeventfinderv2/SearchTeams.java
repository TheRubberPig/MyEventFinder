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
import static xyz.brandonflude.developement.myeventfinderv2.R.id.teamLogo;

public class SearchTeams extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextId;
    private Button buttonGet;
    private TextView textViewResult;
    private ListView showResults;

    private ProgressDialog loading;

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
        String id = editTextId.getText().toString().trim();
        if(id.equals("")){
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_LONG).show();
            return;
        }
        loading = ProgressDialog.show(this, "Please wait...", "Fetching...", false, false);
        String searchTerm = editTextId.getText().toString().trim();
        String url = "http://calendar.brandonflude.xyz/app/services/getTeams.php?team=" + searchTerm;
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
        String id = "";
        String teamName = "";
        String logoURL = "";
        String league = "";
        JSONArray jsonArray = new JSONArray(response);
        List<JSONObject> jsonObjects = new ArrayList<>();
        //JSONArray result = jsonObject.getJSONArray("result");
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            id = jsonObject.getString("team_id");
            teamName = jsonObject.getString("team_nm");
            logoURL = jsonObject.getString("team_logo_url");
            league = jsonObject.getString("league_nm");
            jsonObjects.add(jsonObject);
        }
        showResults = (ListView) findViewById(R.id.searchResults);
        showResults.setAdapter(new ListAdapter(this,jsonObjects));

        showResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //TODO: Get position of the team we want to load


                //TODO: Make a new activity to show upcoming events/Pass ID into new activity
                Intent i = new Intent(getBaseContext(), TeamInformation.class);
                i.putExtra("teamID", id);
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

class ListAdapter extends ArrayAdapter<JSONObject> {
    int vg;
    List<JSONObject> list;
    Context context;

    public ListAdapter(Context context, List<JSONObject> list){
        super(context,R.layout.activity_search_teams_row,list);
        this.context = context;
        this.vg = vg;
        this.list = list;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.activity_search_teams_row, parent, false);
        //TextView teamID=(TextView)itemView.findViewById(R.id.teamID);
        TextView teamName=(TextView)itemView.findViewById(R.id.teamName);
        ImageView logoURL=(ImageView) itemView.findViewById(teamLogo);
        TextView textLeague = (TextView)itemView.findViewById(R.id.leagueName);

        try{
            //teamID.setText(list.get(position).getString("team_id"));
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