package xyz.brandonflude.developement.myeventfinderv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import static android.app.Activity.RESULT_OK;

public class  Signup extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    //Accesses fields in xml
    EditText _username;
    EditText _emailText;
    EditText _passwordText;
    EditText _reEnterPasswordText;
    Button _signupButton;
    TextView _loginLink;

    //Have to be public to get passed into url for checking.
    String username = "";
    String email = "";
    String encryptedPassword = "";

    //This probably needs using
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
        _username = (EditText) findViewById(R.id.input_username);
    }

    //Call main signup logic
    public void buttonClick(View view)
    {
        signup();
    }

    //Go back to the login page
    public void textClick(View view)
    {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    //Runs sign up logic
    private void signup() {
        //Gets variables needed for checking
        email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        //If the passwords and email aren't valid fail the login
        if (!validate(email, password, reEnterPassword)) {
            onSignupFailed("Bad Server Response");
            return;
        }

        //Disable signup button and show a progress dialog box
        _signupButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(Signup.this, R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //Get the username and encrypt the password with MD5
        username = _username.getText().toString();
        encryptWithMD5(password);

        //Start a new thread to send the information to the server
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
                        switch(result.toString())
                        {
                            // First 6 cases for bad signup, final one is for true response
                            case "false":
                            case "null":
                            default:
                                onSignupFailed("Bad Server Response");
                                break;
                            case "username in use":
                                onSignupFailed("Username in Use");
                                break;
                            case "email in use":
                                onSignupFailed("Email in Use");
                                break;
                            case "email is not valid":
                                onSignupFailed("Email Address is Invalid");
                                break;
                            case "true":
                                onSignupSuccess();
                                break;
                        }

                        //Dismiss the progress dialog
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    //Load main page on successful signup
    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        loadMainPage();
    }

    //Fail the sign up and say why
    public void onSignupFailed(String error) {
        Toast.makeText(getBaseContext(), "Signup Failed - " + error, Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    //Validates the passwords and email before trying to sign up
    public boolean validate(String email, String password, String reEnterPassword) {
        boolean valid = true;

        //Checks if the email is empty and conforms to usual patterns
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {

            _emailText.setError(null);

        }

        //Checks if the password is empty and a certain length
        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {

            _passwordText.setError("between 4 and 20 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        //Checks if the passwords match
        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 20 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Passwords Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        //Return result
        return valid;
    }

    //Load the main page
    public void loadMainPage()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    //Encrypt Password with MD5
    private void encryptWithMD5(String password){
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
    }

    class GetData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //Sets up the connection and result
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                //Connects to the server using the users details (Password is encrypted before hand)
                URL url = new URL("http://calendar.brandonflude.xyz/app/services/signup.php?auth=7awee81inro39mzupu8v&username=" + username +"&email="+ email +"&password=" +encryptedPassword);
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