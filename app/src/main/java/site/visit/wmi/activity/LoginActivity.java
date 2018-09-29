package site.visit.wmi.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import site.visit.wmi.R;
import site.visit.wmi.app.AppController;
import site.visit.wmi.helper.SQLiteHandler;
import site.visit.wmi.helper.SessionManager;
import site.visit.wmi.location.ConnectivityReceiver;
import site.visit.wmi.location.GPSTracker;

import static site.visit.wmi.app.AppConfig.URL_APP;
import static site.visit.wmi.app.AppConfig.URL_LOGIN;

public class LoginActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private String URL_2;
    private static final String TAG = RegisterActivity.class.getSimpleName();
    SharedPreferences.Editor editor;
    private Button btnLogin;
    private Button btnLinkToRegister;
    private String statusInternet;
    private EditText inputUsername;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private GPSTracker gps;
    private ImageView menubtn;
    private String updatestr;
    private String updatelinkstr;
    private String registerstr;
    private Intent keMain;
    private ProgressDialog mdialog;
    private String ip_Text;
    private String port_Text;

    private String str_username;
    private String str_name;
    private String str_email;
    private String str_img;
    private String ip_pref;
    private String port_pref;
    private String ipport;
    private Timer timer3;
    private Timer timer1;

    private SharedPreferences setting;
    private ImageView logo_img;
    private boolean isConnected;
    private boolean isReach;
    private String conType;

    private String str_myvisit;
    private String str_myoutstanding;
    private String str_myongoing;

    private String erot;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputUsername = (EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        logo_img = (ImageView) findViewById(R.id.sv_img);
        menubtn = (ImageView) findViewById(R.id.menu_button);

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        str_username = setting.getString("username", "");
        str_name = setting.getString("name", "");
        str_email = setting.getString("email", "");
        str_img = setting.getString("img", "");
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        str_myvisit = setting.getString("my_visit","");
        str_myoutstanding = setting.getString("my_outstanding","");
        str_myongoing = setting.getString("my_ongoing","");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";
        if (ip_pref == "") {
            editor = setting.edit();
            editor.putString("ip_server", "112.215.192.131");
            editor.putString("port_server", "8888");
            editor.putString("my_visit", "0");
            editor.putString("my_outstanding", "0");
            editor.putString("my_ongoing", "0");
            editor.commit();
        } else if (ip_pref.contains("121.52.87.128")) {
            editor = setting.edit();
            editor.putString("ip_server", "112.215.192.131");
            editor.putString("port_server", "8888");
            editor.commit();
        }

        if (findViewById(R.id.container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, MainFragment.newInstance("Placeholder"))
                    .commit();
        }

        checkConnection();
        isConnected = ConnectivityReceiver.isConnected();
        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String imei = telephonyManager.getDeviceId();

        gps = new GPSTracker(LoginActivity.this);

        timer1 = new Timer();
        timer1.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());


                    }
                });

            }
        }, 0, 10);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());


        if ((str_img=="")&&(session.isLoggedIn())){

            logoutUser();

        } else  if (session.isLoggedIn()) {

            keMain = new Intent(LoginActivity.this, SplashscreenActivity.class);
            timer1.cancel();
            startActivity(keMain);
            finish();

        } else {

            if (conType=="No Internet Connection") {

            } else {
                timer();
            }

        }


        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String username = inputUsername.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String imei_device = imei;


                // Check for empty data in the form
                if (!username.isEmpty() && !password.isEmpty()) {


                    if (conType=="No Internet Connection") {

                        alert("i-MAV","Periksa kembali koneksi internet Anda");

                    } else {

                        checkLogin(username, password, imei_device);

                    }

                } else {
                    // Prompt user to enter credentials

                    alert("i-MAV", "Username / Password belum terisi");

                    return;
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {


                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                LoginActivity.this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);


            }
        });

        logo_img.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                View viewInflated = LayoutInflater.from(LoginActivity.this).inflate(R.layout.ip_layout_dialog,
                        (ViewGroup) findViewById(android.R.id.content), false);
                final EditText input_ip = (EditText) viewInflated.findViewById(R.id.input_ip);
                final EditText input_port = (EditText) viewInflated.findViewById(R.id.input_port);
                input_ip.setText(ip_pref);
                input_port.setText(port_pref);
                builder.setView(viewInflated);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip_Text2 = input_ip.getText().toString();
                        String port_Text2 = input_port.getText().toString();
                        setting = getSharedPreferences("WSV_SETTINGS", 0);
                        SharedPreferences.Editor editor = setting.edit();
                        editor.putString("ip_server", ip_Text2);
                        editor.putString("port_server", port_Text2);
                        editor.commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                Dialog d = builder.show();


            }
        });

        menubtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                View viewInflated = LayoutInflater.from(LoginActivity.this).inflate(R.layout.ip_layout_dialog,
                        (ViewGroup) findViewById(android.R.id.content), false);
                final EditText input_ip = (EditText) viewInflated.findViewById(R.id.input_ip);
                final EditText input_port = (EditText) viewInflated.findViewById(R.id.input_port);
                input_ip.setText(ip_pref);
                input_port.setText(port_pref);
                builder.setView(viewInflated);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip_Text2 = input_ip.getText().toString();
                        String port_Text2 = input_port.getText().toString();
                        SharedPreferences.Editor editor = setting.edit();
                        editor.putString("ip_server", ip_Text2);
                        editor.putString("port_server", port_Text2);
                        editor.commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                Dialog d = builder.show();


            }
        });


    }



    private void alert(String title, String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        View viewInflated = LayoutInflater.from(LoginActivity.this).inflate(R.layout.message_dialog,
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


    private void timer() {

        timer3 = new Timer();
        timer3.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ip_pref = setting.getString("ip_server", "");
                        port_pref = setting.getString("port_server", "");
                        ipport = "http://" + ip_pref + ":" + port_pref + "/";


                        //new Register().execute();
                        registerStatus();

                    }
                });

            }
        }, 0, 3000);
    }

    /**
     * function to verify login details in mysql db
     */
    private void checkLogin(final String username, final String password, final String imei_device) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                ipport + URL_LOGIN, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();
                //Log.d(TAG, ipport + URL_LOGIN);

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String username = user.getString("username");
                        String email = user.getString("email");
                        String level= user.getString("level");
                        String imei_device = user.getString("imei_device");
                        String created_at = user.getString("created_at");

                        editor = setting.edit();
                        editor.putString("name", name);
                        editor.putString("username", username);
                        editor.putString("email", email);
                        editor.putString("img", username+".png");
                        editor.commit();


                        // Inserting row in users table
                        db.addUser(name, username, email, level ,imei_device, uid, created_at);


                        Intent intent = new Intent(LoginActivity.this, SplashscreenActivity.class);
                        startActivity(intent);
                        finish();
                    } else {


                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");

                        alert("i-MAV", errorMsg);


                        return;
                    }
                } catch (JSONException e) {
                    alert("i-MAV", "Koneksi Internet atau Server bermasalah segera hubungi NOC WMI");
                    //e.printStackTrace();
                    //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                alert("i-MAV", "Koneksi Internet atau Server bermasalah segera hubungi NOC WMI");
             //Log.e(TAG, "Login Error: " + error.getMessage());
            //Toast.makeText(getApplicationContext(),
                        //error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                params.put("imei_device", imei_device);
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

    @Override
    protected void onStart() {
        super.onStart();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#9E000000")));

        }


    }


    private void registerStatus() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                ipport + URL_APP, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.e(TAG, response.toString());

                try {

                    JSONArray sitelist = response.getJSONArray("statusapp");
                    for (int i = 0; i < sitelist.length(); i++) {
                        JSONObject c = sitelist.getJSONObject(i);

                        updatestr = c.getString("update");
                        updatelinkstr = c.getString("update_link");
                        registerstr = c.getString("register");

                    }

                    if (registerstr.contains("false")) {

                        btnLinkToRegister.setVisibility(View.GONE);
                    } else {

                        btnLinkToRegister.setVisibility(View.VISIBLE);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
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

        Snackbar snackbar3 = Snackbar.make(findViewById(R.id.btnLogin), message, Snackbar.LENGTH_LONG);
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

    private void logoutUser() {

        session.setLogin(false);

        db.deleteUsers();
        db.deleteSettings();


    }


}
