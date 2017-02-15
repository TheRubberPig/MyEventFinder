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

public class Login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    EditText _emailText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;
    String encryptedPassword = null;
    String email = "";
    String userID = "";
    String username = "";
    String encryptedKey = "";
    String loginResponseString = "";

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

    public void checkCookie()
    {
        // See if the user has an existing key saved, fetch the key
        SharedPreferences settings = getSharedPreferences("MyEventFinderAuthKeys", 0);
        String savedKey = settings.getString("keys", encryptedKey).toString();

        // TODO: Check this key against the database.
    }

    public void buttonClick(View view)
    {
        login();
    }

    public void textClick(View view)
    {
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
    }
    public void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }
        _loginButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(Login.this, R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
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
        // TODO: Implement your own authentication logic here.
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
                            userID = response[0];
                            username = response[1];
                            encryptedKey = response[2];

                            // Save the user's encryptedKey to device
                            SharedPreferences keys = getSharedPreferences("MyEventFinderAuthKeys", 0);
                            SharedPreferences.Editor editor = keys.edit();
                            editor.putString("keys", encryptedKey);
                            editor.commit();

                            onLoginSuccess();

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

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        loadMainPage();
    }
    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }
    public boolean validate() {
        boolean valid = true;
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }
        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            _passwordText.setError("between 4 and 20 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        return valid;
    }

    public void loadMainPage()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    //This sets up a second thread to connect to the server in the background and it returns the result as a string
    class GetData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //Sets up the connection and result
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                //Connects to the server using the users details (Password is encrypted before hand)
                URL url = new URL("http://calendar.brandonflude.xyz/app/services/login.php?auth=7awee81inro39mzupu8v&email="+ email +"&password=" +encryptedPassword);
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
