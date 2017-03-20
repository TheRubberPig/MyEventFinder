package xyz.brandonflude.developement.myeventfinderv2;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.view.View;
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

import static android.R.attr.password;

public class Login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    EditText _emailText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;
    String encryptedPassword = null;
    String email = "";
    String encryptedKey = "";
    String savedKey = "";
    String loginResponseString = "";
    String authenticationResponseString = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Straight away see if the user has already signed in
        checkCookie();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _signupLink = (TextView) findViewById(R.id.link_signup);
    }

    //This will log people in automatically if they have logged in correctly before
    public void checkCookie() {
        // See if the user has an existing key saved, fetch the key
        SharedPreferences settings = getSharedPreferences("MyEventFinderAuthKeys", 0);
        savedKey = settings.getString("keys", encryptedKey).toString();

        if (savedKey != "") {
            final ProgressDialog progressDialog = new ProgressDialog(Login.this, R.style.AppTheme);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();

            //Start a new thread to check login details against the server
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            //Checks login information with the server, will return true or false if the user has
                            //an account

                            //Stores text from the server
                            String result = "";
                            try {
                                //.get() returns the string and not the activity (which is what we want)
                                result = new GetData().execute("keyCheck").get();
                                //Catch any errors here
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            //If the result is false or null(server error) fail the authentication
                            if (result.equals("false")) {
                                // Exit the method
                            }
                            //Else log the user in
                            else {
                                // Get data from results
                                authenticationResponseString = result;
                                String[] response = authenticationResponseString.split(",");
                                String userID = response[0];
                                String username = response[1];

                                onLoginSuccess(username, userID);

                                progressDialog.dismiss();
                            }
                            progressDialog.dismiss();
                        }
                    }, 3000);
        }
        else
        {
            // Exit the method
        }
    }

    //Method called when login button is clicked
    public void buttonClick(View view) {login();}

    //Opens the signup page when the text is clicked
    public void textClick(View view)
    {
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
    }

    //Main method for checking login information
    private void login() {
        //Get password and email
        String password = _passwordText.getText().toString();
        email = _emailText.getText().toString();
        //Calls validate method to make sure the email and password conform to standards
        if (!validate(email, password)) {
            //If the validate method comes back false fail the login
            onLoginFailed();
            return;
        }
        //Disable the login button and open a progress dialog
        _loginButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(Login.this, R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        //Encrypt the password with MD5
        convertToMD5(password);

        //Start a new thread to check login details against the server
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        //Checks login information with the server, will return true or false if the user has
                        //an account

                        //Stores text from the server
                        String result = "";
                        try {
                            //.get() returns the string and not the activity (which is what we want)
                            result = new GetData().execute("").get();

                            //Catch any errors here
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        //If the result is false or null(server error) fail the login
                        //TODO: Tell the user why the login failed
                        if(result.equals("false") || result.equals(""))
                        {
                            //If the users entered details is incorrect fail the login attempt
                            onLoginFailed();
                        }
                        //Else log the user in
                        else
                        {
                            //If the users details are correct log them in

                            loginResponseString = result;
                            String[] response = loginResponseString.split(",");
                            String userID = response[0];
                            String username = response[1];
                            encryptedKey = response[2];

                            // Save the user's encryptedKey to device
                            SharedPreferences keys = getSharedPreferences("MyEventFinderAuthKeys", 0);
                            SharedPreferences.Editor editor = keys.edit();
                            editor.putString("keys", encryptedKey);
                            editor.commit();

                            onLoginSuccess(username, userID);

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

    //If the login was successful load the main page
    private void onLoginSuccess(String username, String userID) {
        _loginButton.setEnabled(true);
        loadMainPage(username, userID);
    }

    //Tell the user the login failed
    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    //Validates the email and password
    private boolean validate(String email, String password) {
        boolean valid = true;

        //Checks that the email isn't empty and conforms to regular email patterns
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        //Checks that the password isn't empty and is between a certain length
        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            _passwordText.setError("Please enter a password between 4 and 20 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        return valid;
    }

    //Load the main page
    private void loadMainPage(String username, String userID)
    {
        //Start a new intent
        Intent intent = new Intent(this, MainActivity.class);
        //Pass username and id into main activity
        intent.putExtra("username", username);
        intent.putExtra("userID", userID);
        //Start the main activity
        startActivity(intent);
    }

    //Converts a string into MD5
    private void convertToMD5(String password)
    {
        try
        {
            //Create Message digest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add Password bytes to hash
            md.update(password.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            encryptedPassword = sb.toString();
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    //This sets up a second thread to connect to the server in the background and it returns the result as a string
    class GetData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //Sets up the connection and result
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                URL url = null;
                //Selects url to use depending what is passed into the method.
                switch (params[0]){
                    case "":
                        //Trigger login PHP
                        url = new URL("http://calendar.brandonflude.xyz/app/services/login.php?auth=7awee81inro39mzupu8v&email="+ email +"&password=" +encryptedPassword);
                        break;
                    case "keyCheck":
                        //Check if the user has logged in before
                        url = new URL("http://calendar.brandonflude.xyz/app/services/verifyAuthentication.php?auth=7awee81inro39mzupu8v&key="+ savedKey);
                        break;
                    default:
                        //Return null if nothing is passed in.
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
