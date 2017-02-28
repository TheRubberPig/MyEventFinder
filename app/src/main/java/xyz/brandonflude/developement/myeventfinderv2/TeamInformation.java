package xyz.brandonflude.developement.myeventfinderv2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class TeamInformation extends AppCompatActivity {
    Bundle extras;
    String teamID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_information);

        extras = getIntent().getExtras();
        teamID = extras.getString("teamID");
        Toast toast = Toast.makeText(this, teamID, Toast.LENGTH_LONG);
        toast.show();
    }
}
