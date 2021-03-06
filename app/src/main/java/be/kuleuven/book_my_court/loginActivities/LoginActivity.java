package be.kuleuven.book_my_court.loginActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import be.kuleuven.book_my_court.FragmentActivity;
import be.kuleuven.book_my_court.R;

public class LoginActivity extends AppCompatActivity {

    private static String userName;
    private MaterialButton login_button;
    private MaterialButton forget_button;
    private MaterialButton register_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //These two lines hide the top bar of the page
        //must before the super onCreate method
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //This line hides the action bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_button = (MaterialButton) findViewById(R.id.login_button);
        forget_button = (MaterialButton) findViewById(R.id.forget_button);
        register_button = (MaterialButton) findViewById(R.id.register_button);

        try {
            checkLoginTxt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            readLoginTxt();
        } catch (IOException e) {
            e.printStackTrace();
        }
}

    public void onBtnForget_Clicked(View view){
        Intent intent = new Intent(this, ForgetActivity.class);
        finish();
        startActivity(intent);
    }

    public void onBtnRegister_Clicked(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        finish();
        startActivity(intent);
    }

    public static String getUserName() {
        return userName;
    }

    public void onBtnLogin_Clicked(View view) {
        EditText username = (EditText) findViewById(R.id.login_username);
        EditText password = (EditText) findViewById(R.id.login_password);
        userName = username.getText().toString().trim();
        String passWord = password.getText().toString();
        String url = "https://studev.groept.be/api/a21pt101/findPassword/";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        Intent intent = new Intent(this, FragmentActivity.class);

        //lambda expression
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url + userName,
                response ->  {
                    try {
                        JSONArray responseArray = new JSONArray(response);
                        JSONObject curObject = responseArray.getJSONObject(0);
                        String responseString = curObject.getString("password");

                        if (hashPassword(passWord).equals(responseString)) {
                            updateLoginTxt(userName);
                            finish();
                            startActivity(intent);
                    }
                        else {
                            Toast.makeText(LoginActivity.this, "The username or password is incorrect. " +
                                "Please verify and try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    Log.e("Database", e.getMessage(), e);
                }
        },
                error -> Toast.makeText(LoginActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(stringRequest);
    }

    public static String hashPassword(String password){
        String hashedString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16)
                        .substring(1));
            }
            hashedString = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedString;
    }

    private void checkLoginTxt() throws IOException {
        String s = getFilesDir() + "/" + "login";
        try{
            new BufferedReader(new FileReader(s));
        }
        catch (Exception e){
            generateLoginTxt();
        }
    }

    private void generateLoginTxt() throws IOException {
        JSONObject object = new JSONObject();
        try {
            object.put("username", "");
            writeLoginTxt(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void readLoginTxt() throws IOException {
        String s = getFilesDir() + "/" + "login";
        BufferedReader reader = new BufferedReader(new FileReader(s));
        String json = "";
        json = getJSONString(reader);
        Intent intent = new Intent(this, FragmentActivity.class);
        try {
            JSONObject object = new JSONObject(json);
            String username = object.getString("username");
            if(!username.equals("")){
                userName = username;
                finish();
                startActivity(intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateLoginTxt(String username) throws IOException {
        String s = getFilesDir() + "/" + "login";
        BufferedReader reader = new BufferedReader(new FileReader(s));
        String json = "";
        json = getJSONString(reader);

        try {
            JSONObject object = new JSONObject(json);
            object.put("username", username);
            writeLoginTxt(object);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getJSONString(BufferedReader reader) throws IOException {
        String json = "";
        try {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }
            json = sb.toString();
        } finally {
            reader.close();
        }

        return json;
    }

    private void writeLoginTxt(JSONObject object) throws IOException {
        String s = getFilesDir() + "/" + "login";
        BufferedWriter output = new BufferedWriter(new FileWriter(s));
        output.write(object.toString());
        output.close();
    }
}