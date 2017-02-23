package xyz.brandonflude.developement.myeventfinderv2;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import static android.R.id.text1;

public class SearchTeams extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextId;
    private Button buttonGet;
    private TextView textViewResult;

    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_teams);

        editTextId = (EditText) findViewById(R.id.editTextId);
        buttonGet = (Button) findViewById(R.id.buttonGet);
        textViewResult = (TextView) findViewById(R.id.textViewResult);

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
        //JSONArray result = jsonObject.getJSONArray("result");
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            id = jsonObject.getString("team_id");
            teamName = jsonObject.getString("team_nm");
            logoURL = jsonObject.getString("team_logo_url");
            league = jsonObject.getString("league_nm");
        }
        textViewResult.setText("ID:\t"+id+"\nTeam Name:\t" + teamName+"\nLeague:\t"+ league);
    }

    @Override
    public void onClick(View v){
        getData();
    }
}
