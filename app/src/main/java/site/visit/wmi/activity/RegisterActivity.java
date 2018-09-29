/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package site.visit.wmi.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import site.visit.wmi.R;
import site.visit.wmi.app.AppController;
import site.visit.wmi.helper.SQLiteHandler;
import site.visit.wmi.helper.SessionManager;
import site.visit.wmi.location.ConnectivityReceiver;

import static site.visit.wmi.app.AppConfig.URL_REGISTER;

public class RegisterActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputUsername;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private Spinner pilihan_level;

    private String ip_pref;
    private String port_pref;
    private String ipport;
    private SharedPreferences setting;

    private boolean isConnected;
    private String conType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputUsername = (EditText) findViewById(R.id.username);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        pilihan_level = (Spinner) findViewById(R.id.level_spinner);
        ArrayAdapter<CharSequence> adapterkateg = ArrayAdapter.createFromResource(this, R.array.level_array, android.R.layout.simple_spinner_item);
        adapterkateg.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pilihan_level.setAdapter(adapterkateg);
        pilihan_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                String catcat = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        final String imei = telephonyManager.getDeviceId();

        checkConnection();
        isConnected = ConnectivityReceiver.isConnected();
        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server","");
        port_pref = setting.getString("port_server","");
        ipport =  "http://"+ip_pref+":"+port_pref+"/";

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            //Intent intent = new Intent(RegisterActivity.this,
                    //MainActivity.class);
            //startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String username = inputUsername.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String imei_device = imei;
                String password = inputPassword.getText().toString().trim();
                String level = pilihan_level.getSelectedItem().toString().trim();
                if (!name.isEmpty() && !username.isEmpty() && !email.isEmpty() && !imei_device.isEmpty() && !password.isEmpty()){

                    if (level.contains("Level")) {
                        alert("i-MAV","Pilih level dengan benar");


                    } else {

                        if (conType == "No Internet Connection") {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            View viewInflated = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.message_dialog,
                                    (ViewGroup) findViewById(android.R.id.content), false);
                            final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                            final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                            titletxt.setText("Internet Error!");
                            messagetxt.setText("Periksa kembali koneksi internet Anda");
                            builder.setView(viewInflated);


                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            Dialog dd = builder.show();

                        } else {
                            registerUser(name, username, email, imei_device, password, level);
                        }

                    }
                } else {
                alert("i-MAV","Isi semua data dengan lengkap");
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
                RegisterActivity.this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
        });

    }

    public void onBackPressed() {

        Intent i = new Intent(getApplicationContext(),
                LoginActivity.class);
        startActivity(i);
        finish();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);

    }

    private String conType2;

    private void checkConnection() {
        isConnected = ConnectivityReceiver.isConnected();
        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());
        conType2 = ConnectivityReceiver.getNetworkClass(getApplicationContext());
        showSnack(isConnected,conType,conType2);
    }

    private void showSnack(boolean isConnected, String conType, String conType2) {
        String message;
        int color;
        if (isConnected)  {

            if (conType=="Using Wifi") {
                message = conType+"\nConnected to Internet";
                color = getResources().getColor(R.color.orange300);
            } else {

                message = "Using "+conType2+" Network\nConnected to Internet";
                color = getResources().getColor(R.color.orange300);
            }


        } else {
            message = conType+"\nCheck your settings again";
            color = Color.RED;


        }

        Snackbar snackbar3 = Snackbar.make(findViewById(R.id.btnRegister), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar3.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.bg_login));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar3.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        AppController.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected, String conType, String conType2) {
        showSnack(isConnected,conType,conType2);
    }


    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String username, final String email,final String imei_device,
                              final String password, final String level) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Mendaftarkan ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                ipport+URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String username = user.getString("username");
                        String email = user.getString("email");
                        String imei_device = user.getString("imei_device");
                        String level = user.getString("level");
                        String created_at = user.getString("created_at");


                        // Inserting row in users table
                        db.addUser(name, username, email,level, imei_device, uid, created_at);


                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                        View viewInflated = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.message_dialog,
                                (ViewGroup) findViewById(android.R.id.content), false);
                        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                        titletxt.setText("i-MAV");
                        messagetxt.setText("Akun berhasil terdaftar, silahkan login");
                        builder.setView(viewInflated);


                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                                RegisterActivity.this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                            }
                        });

                        Dialog dd = builder.show();


                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        alert("i-MAV",errorMsg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();


                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("username", username);
                params.put("email", email);
                params.put("level", level);
                params.put("imei_device", imei_device);
                params.put("password", password);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void alert(String title, String msg){

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        View viewInflated = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.message_dialog,
                (ViewGroup) findViewById(android.R.id.content), false);
        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
        titletxt.setText(title);
        messagetxt.setText(msg);
        builder.setView(viewInflated);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog dd = builder.show();

    }
}
