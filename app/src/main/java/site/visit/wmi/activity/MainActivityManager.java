package site.visit.wmi.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import site.visit.wmi.R;
import site.visit.wmi.app.AppController;
import site.visit.wmi.helper.SQLiteHandler;
import site.visit.wmi.helper.SessionManager;
import site.visit.wmi.location.ConnectivityReceiver;
import site.visit.wmi.location.GPSTracker;
import site.visit.wmi.visit.JSONParser;
import site.visit.wmi.visit.SiteList;

import static site.visit.wmi.R.id.outstanding_but;
import static site.visit.wmi.app.AppConfig.URL_ALL;
import static site.visit.wmi.app.AppConfig.URL_DASHBOARD;
import static site.visit.wmi.app.AppConfig.URL_R1;
import static site.visit.wmi.app.AppConfig.URL_R2;
import static site.visit.wmi.app.AppConfig.URL_R3;


public class MainActivityManager extends AppCompatActivity implements MainFragment.Callbacks, ConnectivityReceiver.ConnectivityReceiverListener {

    private String URL_FINAL;
    private String str_myvisit;
    private String str_myoutstanding;
    private String erot;
    private TextView welcome_txt;
    private TextView txtName;
    private TextView txtLevel;
    private TextView txtEmail;
    private TextView txtLat;
    private TextView txtLong;
    private TextView txtstatus;
    private LinearLayout bg;

    private SQLiteHandler db;
    private SessionManager session;
    GPSTracker gps;

    private static final String TAG_MYVISIT = "myvisit";
    private static final String TAG_OUTSTANDING = "outstanding";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VISITCOUNT = "visitcount";

    private double latitude, longitude;
    private LinearLayout lin_place;
    private ImageView map;

    JSONArray dataDash = null;

    private String ip_pref;
    private String port_pref;
    private String ipport;
    private SharedPreferences setting;
    private boolean isConnected;
    private String conType;
    private final int sdk = android.os.Build.VERSION.SDK_INT;
    Boolean er = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_manager);

        checkConnection();
        isConnected = ConnectivityReceiver.isConnected();
        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());


        if (findViewById(R.id.container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, MainFragment.newInstance("Placeholder"))
                    .commit();
        }


    //View view = findViewById(R.d.rel_lay);
        //View root = view.getRootView();
        //root.setBackgroundColor(getResources().getColor(R.color.btn_login));

        welcome_txt = (TextView) findViewById(R.id.welcometxt);
        txtName = (TextView) findViewById(R.id.name);
        txtLevel = (TextView) findViewById(R.id.level);
        txtEmail = (TextView) findViewById(R.id.email);
        txtLat = (TextView) findViewById(R.id.latitude);
        txtLong = (TextView) findViewById(R.id.longitude);
        txtstatus = (TextView) findViewById(R.id.status_txt);
        map = (ImageView) findViewById(R.id.imageView);

        lin_place = (LinearLayout) findViewById(R.id.lin_place);

        final WebView browser1 = (WebView) findViewById(R.id.wv_r1);
        Button br1 = (Button) findViewById(R.id.r1_web);
        Button br2 = (Button) findViewById(R.id.r2_web);
        Button br3 = (Button) findViewById(R.id.r3_web);
        final CardView cr1 = (CardView) findViewById(R.id.cr1);

        bg = (LinearLayout) findViewById(R.id.lin_mainpic);

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        str_myvisit = setting.getString("my_visit", "");
        str_myoutstanding = setting.getString("my_outstanding", "");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";

        cr1.setVisibility(View.VISIBLE);
        browser1.loadUrl(ipport+URL_ALL);
        browser1.getSettings().setJavaScriptEnabled(true);


        br1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                cr1.setVisibility(View.VISIBLE);
                browser1.loadUrl(ipport+URL_R1);
                browser1.getSettings().setJavaScriptEnabled(true);

            }

        });
        br2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                cr1.setVisibility(View.VISIBLE);
                browser1.loadUrl(ipport+URL_R2);
                browser1.getSettings().setJavaScriptEnabled(true);
            }

        });
        br3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                cr1.setVisibility(View.VISIBLE);
                browser1.loadUrl(ipport+URL_R3);
                browser1.getSettings().setJavaScriptEnabled(true);
            }

        });
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 10) {
            String greeting = "Selamat Pagi!";
            welcome_txt.setText(greeting);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                txtName.setTextColor(Color.parseColor("#FFFFFF"));
                txtEmail.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setBackgroundDrawable(getResources().getDrawable(R.drawable.morningsky));
            } else {
                txtName.setTextColor(Color.parseColor("#FFFFFF"));
                txtEmail.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setBackground(getResources().getDrawable(R.drawable.morningsky));

            }

        } else if (timeOfDay >= 10 && timeOfDay < 14) {
            String greeting = "Selamat Siang!";
            welcome_txt.setText(greeting);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                bg.setBackgroundDrawable(getResources().getDrawable(R.drawable.siangsky));
            } else {
                bg.setBackground(getResources().getDrawable(R.drawable.siangsky));
            }
        } else if (timeOfDay >= 14 && timeOfDay < 19) {
            String greeting = "Selamat Sore!";
            welcome_txt.setText(greeting);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                bg.setBackgroundDrawable(getResources().getDrawable(R.drawable.soresky));
            } else {
                bg.setBackground(getResources().getDrawable(R.drawable.soresky));
            }
        } else if (timeOfDay >= 19 && timeOfDay < 24) {
            String greeting = "Selamat Malam!";
            welcome_txt.setText(greeting);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                txtName.setTextColor(Color.parseColor("#FFFFFF"));
                txtEmail.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setBackgroundDrawable(getResources().getDrawable(R.drawable.nightsky));
            } else {
                txtName.setTextColor(Color.parseColor("#FFFFFF"));
                txtEmail.setTextColor(Color.parseColor("#FFFFFF"));
                bg.setBackground(getResources().getDrawable(R.drawable.nightsky));

            }
        }


        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        HashMap<String, String> user = db.getUserDetails();
        final String namedb = user.get("name");
        final String leveldb = user.get("level");
        String emaildb = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(namedb);
        txtLevel.setText(leveldb);
        txtEmail.setText(emaildb);
        String pic_name = txtName.getText().toString().trim();

        try {
            String tt = URLEncoder.encode(pic_name, "UTF-8");
            URL_FINAL = ipport + URL_DASHBOARD + tt;
            //Log.e("a", URL_FINAL);
        } catch (Exception e) {
            return;
        }

        final LocationManager managergps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!managergps.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        txtLat.setText("");

        lin_place.setVisibility(View.GONE);

        gps = new GPSTracker(getApplicationContext());
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            //List<Address> addresses =geocoder.getFromLocation(latitude, longitude, 1);
            String addres;
            try {

                Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
                List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                if (addresses.isEmpty()) {

                    txtLong.setText("");
                } else {
                    if (addresses.size() > 0) {
                        lin_place.setVisibility(View.VISIBLE);
                        txtLat.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getSubAdminArea());
                        txtLong.setText(addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                        //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();

                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // getFromLocation() may sometimes fail
            }
        } else {
            //gps.showSettingsAlert();
        }


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
                color = getResources().getColor(R.color.blue_dongker);
            } else {

                message = "Using "+conType2+" Network\nConnected to Internet";
                color = getResources().getColor(R.color.blue_dongker);
            }


        } else {
            message = conType+"\nCheck your settings again";
            color = Color.RED;


        }

        Snackbar snackbar3 = Snackbar.make(findViewById(R.id.level), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar3.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.cardview_light_background));
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


    private void buildAlertMessageNoGps() {


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityManager.this);
        View viewInflated = LayoutInflater.from(MainActivityManager.this).inflate(R.layout.message_dialog,
                (ViewGroup) findViewById(android.R.id.content), false);
        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
        titletxt.setText("GPS");
        messagetxt.setText("Silahkan hidupkan GPS di smartphone Anda.");
        builder.setView(viewInflated);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void alert(String title, String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityManager.this);
        View viewInflated = LayoutInflater.from(MainActivityManager.this).inflate(R.layout.message_dialog,
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

    private void logoutUser() {

        session.setLogin(false);

        db.deleteUsers();
        db.deleteSettings();

        //Launching the login activity
        Intent intent = new Intent(MainActivityManager.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    private void managerCheckin() {
        HashMap<String, String> user = db.getUserDetails();

        String pic_checkin = user.get("name");
        Intent intent = new Intent(MainActivityManager.this, SiteList.class);
        startActivity(intent);
        intent.putExtra("pic", pic_checkin);

    }


    private void settings() {

        Intent intent = new Intent(MainActivityManager.this, SettingActivity.class);


        finish();
        startActivity(intent);

    }


    public class dashData extends AsyncTask<Void, Void, String> {

        JSONArray dataJsonArr = null;
        String myvisitstr;
        String outstandingstr;
        String value;

        private Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        final protected String doInBackground(Void... params) {

            // get the array of users
            try {

                JSONParser jParser = new JSONParser();

                // get json string from url
                JSONObject json = jParser.getJSONFromUrl(URL_FINAL);
                Log.e("TAG", URL_FINAL);
                //Log.d("Login attempt", json.toString());

                dataJsonArr = json.getJSONArray(TAG_VISITCOUNT);

                JSONObject c = dataJsonArr.getJSONObject(0);

                myvisitstr = c.getString(TAG_MYVISIT);
                outstandingstr = c.getString(TAG_OUTSTANDING);


                value = myvisitstr + "," + outstanding_but;
                //Log.e(TAG,"My Visit: " + myvisitstr +", Oustanding: " + outstandingstr);

                er = false;

            } catch (Exception e) {
                exception = e;
                e.printStackTrace();
                er = true;
                return null;
            }

            return value;
        }


        @Override
        final protected void onPostExecute(String value) {
            super.onPostExecute(value);
            erot=er.toString();
            Log.e("Erooooooooot", erot);


            if ((er)||(erot=="true")) {


                setting = getSharedPreferences("WSV_SETTINGS", 0);
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("my_visit", "99");
                editor.putString("my_outstanding", "99");
                editor.commit();


            } else if ((!er)||(erot=="false")) {


                setting = getSharedPreferences("WSV_SETTINGS", 0);
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("my_visit", myvisitstr);
                editor.putString("my_outstanding", outstandingstr);
                editor.commit();

            }


        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("i-MAV STATISTIC");
            actionBar.setHomeAsUpIndicator(R.drawable.header_stat);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {

            logoutUser();

        } else  if (id == R.id.action_checkin) {

            managerCheckin();

        } else if (id == R.id.action_setting) {

            settings();


        } else if (id == R.id.action_exit) {


            finish();

        } else if (id == R.id.action_refresh) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            final ProgressDialog pDialog = new ProgressDialog(MainActivityManager.this);
            pDialog.setMessage("Refreshing Data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            new CountDownTimer(1500, 1000) {

                public void onTick(long millisUntilFinished) {
                    pDialog.show();
                }

                public void onFinish() {
                    pDialog.dismiss();


                    if (conType == "No Internet Connection") {

                        alert("Internet Error!", "Periksa kembali koneksi internet Anda");

                    } else {

                        new dashData().execute();
                        setting = getSharedPreferences("WSV_SETTINGS", 0);
                        str_myvisit = setting.getString("my_visit", "");
                        str_myoutstanding = setting.getString("my_outstanding", "");

                    }

                }
            }.start();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void passDataToActivity(String data) {
        // Do nothing yet...
    }

    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityManager.this);
        View viewInflated = LayoutInflater.from(MainActivityManager.this).inflate(R.layout.message_dialog,
                (ViewGroup) findViewById(android.R.id.content), false);
        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
        titletxt.setText("EXIT");
        messagetxt.setText("Apakah benar ingin keluar?");
        builder.setView(viewInflated);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dd = builder.show();

    }


}
