package xyz.brandonflude.developement.myeventfinderv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;

public class Signup extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    EditText _emailText;
    EditText _passwordText;
    EditText _reEnterPasswordText;
    Button _signupButton;
    TextView _loginLink;
    String email = "";
    String password = "";
    String encryptedPassword = "";
    String reEnterPassword = "";
    String encryptedReEnter = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _reEnterPasswordText =  (EditText)findViewById(R.id.input_reEnterPassword);
        _signupButton = (Button) findViewById(R.id.btn_signup);
        _loginLink = (TextView) findViewById(R.id.link_signup);
    }

    public void buttonClick(View view)
    {
        signup();
    }

    public void textClick(View view)
    {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }
        _signupButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(Signup.this, R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        reEnterPassword = _reEnterPasswordText.getText().toString();
        try
        {
            //Create MessageDigest for MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //MessageDigest md = MessageDigest.getInstance("MD5");
            //Get the individual bytes of the password
            md5.update(password.getBytes());
            //md.update(reEnterPassword.getBytes());
            //Get the hash's bytes
            byte[] bytes = md5.digest();
            //byte[] bytes2 = md.digest();
            //Convert to hex format
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i<bytes.length; i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                //sb.append(Integer.toString((bytes2[i] & 0xff) + 0x100,16).substring(1));
            }
            //Get the complete password in hex format
            encryptedPassword = sb.toString();
           // encryptedReEnter = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // TODO: Implement your own signup logic here.
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        //Checks login information with the server, will return true or false if the user has
                        //an account

                        //Stores text from the server
                        String result = "";
                        try {
                            //.get() returns the string and not the activity (which is what we want)
                            result = new GetData().execute().get();

                            //Catch any errors here
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        //If the result is false or null(server error) fail the login
                        //TODO: Tell the user why the login failed
                        if(result.equals("false") || result.equals(null))
                        {

                            //If the users entered details is incorrect fail the login attempt
                            onSignupFailed();
                        }
                        //Else log the user in
                        else
                        {
                            //If the users details are correct log them in
                            onSignupSuccess();
                            progressDialog.dismiss();
                        }
                        progressDialog.dismiss();
                    }
                }, 3000);
    }
    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        loadMainPage();
    }
    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {

            _emailText.setError(null);

        }
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {

            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    public void loadMainPage()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    class GetData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Sets up the connection and result
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                //Connects to the server using the users details (Password is encrypted before hand)
                //TODO: Let the user create a username
                URL url = new URL("http://calendar.brandonflude.xyz/app/services/signup.php?auth=7awee81inro39mzupu8v&username=test&email="+ email +"&password=" +encryptedPassword);
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