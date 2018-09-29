package site.visit.wmi.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import site.visit.wmi.R;
import site.visit.wmi.app.AppController;
import site.visit.wmi.helper.SQLiteHandler;
import site.visit.wmi.helper.SessionManager;
import site.visit.wmi.location.ConnectivityReceiver;
import site.visit.wmi.location.GPSTracker;
import site.visit.wmi.visit.SiteListOffline;
import site.visit.wmi.visit.myVisit;
import site.visit.wmi.visit.outstandingVisit;

import static site.visit.wmi.app.AppConfig.URL_APP;
import static site.visit.wmi.app.AppConfig.URL_APP_VERSION;
import static site.visit.wmi.app.AppConfig.URL_DASHBOARD;
import static site.visit.wmi.app.AppConfig.URL_MY_VISIT;
import static site.visit.wmi.app.AppConfig.URL_OUTSTANDING;
import static site.visit.wmi.app.AppConfig.URL_UPDATE_SITE;
import static site.visit.wmi.app.AppConfig.URL_USER_DETAIL;
import static site.visit.wmi.app.AppConfig.URL_USER_IMG;
import static site.visit.wmi.app.AppConfig.URL_VISIT_START;

public class MainActivity extends AppCompatActivity implements MainFragment.Callbacks, ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG_MYVISIT = "myvisit";
    private static final String TAG_OUTSTANDING = "outstanding";
    private static final String TAG_ONGOING = "ongoing";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VISITCOUNT = "visitcount";
    private static String TAG = MainActivity.class.getSimpleName();
    private final int sdk = android.os.Build.VERSION.SDK_INT;
    GPSTracker gps;
    JSONArray dataDash = null;
    String myvisitstr;
    String outstandingstr;
    String ongoing_str;
    private ImageView imgNavHeaderBg, imgProfile;
    private String URL_FINAL;
    private String URL_SITE;
    private String URL_MV;
    private String URL_OV;
    private String URL_3;
    private String URL_UD;
    private String statusInternet;


    private String version;
    private String versioncode;
    private TextView welcome_txt;
    private TextView txtName;
    private TextView txtLevel;
    private TextView txtUsername;
    private TextView txtEmail;
    private TextView txtLat;
    private TextView txtLong;
    private TextView txtstatus;
    private LinearLayout bg;
    private Timer timer1;
    private Timer timer2;
    private Button btnLogout;
    private Button btnVisitAll;
    private Button btnSitelist;
    private Button checkIn;
    private Button btnmyvisit;
    private Button btnoutstanding;
    private Notification myNotication;
    private NotificationManager manager;
    private SQLiteHandler db;
    private SessionManager session;
    private double latitude, longitude;
    private LinearLayout lin_place;
    private ImageView map;
    private ProgressDialog mdialog;

    private boolean isConnected;
    private String conType;
    private Runnable runnableCode;
    private String conType2;

    private Handler mHandler;
    private String packageName;

    private SharedPreferences setting;
    SharedPreferences.Editor editor;
    private String ip_pref;
    private String port_pref;
    private String ipport;
    private String str_username;
    private String str_name;
    private String str_email;
    private String str_img;
    private String str_latitude;
    private String str_longitude;
    private String str_myvisit;
    private String str_myoutstanding;
    private String str_myongoing;
    private String str_ttopen;
    private String str_autoupdate;

    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageName =  this.getPackageName();

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


        //View view = findViewById(R.id.rel_lay);
        //View root = view.getRootView();
        //root.setBackgroundColor(getResources().getColor(R.color.btn_login));

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        welcome_txt = (TextView) findViewById(R.id.welcometxt);
        txtName = (TextView) findViewById(R.id.name);
        txtLevel = (TextView) findViewById(R.id.level);
        txtEmail = (TextView) findViewById(R.id.email);
        txtLat = (TextView) findViewById(R.id.latitude);
        txtLong = (TextView) findViewById(R.id.longitude);
        txtstatus = (TextView) findViewById(R.id.status_txt);
        map = (ImageView) findViewById(R.id.imageView);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnVisitAll = (Button) findViewById(R.id.btnVisitAll);
        btnSitelist = (Button) findViewById(R.id.btnSitelist);
        imgProfile = (ImageView) findViewById(R.id.img_profile);
        btnmyvisit = (Button) findViewById(R.id.myvisit_but);

        btnoutstanding = (Button) findViewById(R.id.outstanding_but);

        lin_place = (LinearLayout) findViewById(R.id.lin_place);

        checkIn = (Button) findViewById(R.id.btnAdd);

        bg = (LinearLayout) findViewById(R.id.lin_mainpic);

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        str_username = setting.getString("username", "");
        str_name = setting.getString("name", "");
        str_email = setting.getString("email", "");
        str_img = setting.getString("img", "");
        str_myvisit = setting.getString("my_visit","");
        str_myoutstanding = setting.getString("my_outstanding","");
        str_myongoing = setting.getString("my_ongoing", "");
        str_ttopen = setting.getString("my_tt","");
        str_autoupdate = setting.getString("auto_update","");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";
        if (str_myongoing == "") {
            editor = setting.edit();
            editor.putString("my_ongoing", "0");
            editor.commit();
            str_myongoing = setting.getString("my_ongoing", "");
        } else if (str_autoupdate == ""){

            editor = setting.edit();
            editor.putString("auto_update","true");
            editor.commit();
            str_autoupdate = setting.getString("auto_update","");
        }

        mHandler = new Handler();


        btnmyvisit.setText(str_myvisit);
        btnoutstanding.setText(str_myoutstanding);


        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 10) {
            String greeting = "Selamat Pagi!";
            welcome_txt.setText(greeting);
            txtName.setTextColor(Color.parseColor("#F5F5F5"));
            txtEmail.setTextColor(Color.parseColor("#F5F5F5"));
            txtName.setShadowLayer(3, 5, 3,   getResources().getColor(R.color.bg_login));
            txtEmail.setShadowLayer(3, 5, 3,   getResources().getColor(R.color.bg_login));
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                bg.setBackgroundDrawable(getResources().getDrawable(R.drawable.morningsky));
            } else {
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
            txtName.setTextColor(Color.parseColor("#F5F5F5"));
            txtEmail.setTextColor(Color.parseColor("#F5F5F5"));
            txtName.setShadowLayer(3, 5, 3,   getResources().getColor(R.color.bg_login));
            txtEmail.setShadowLayer(3, 5, 3,   getResources().getColor(R.color.bg_login));
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                bg.setBackgroundDrawable(getResources().getDrawable(R.drawable.nightsky));
            } else {
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
        final String usernamedb = user.get("username");
        final String leveldb = user.get("level");
        String emaildb = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(str_name);
        txtEmail.setText(str_email);
        txtLevel.setText(leveldb);
        txtLevel.setVisibility(View.GONE);


        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            Integer kode = pInfo.versionCode;
            versioncode = kode.toString().trim();
            URL_3 = ipport + URL_USER_IMG + str_img;
            URL_FINAL = ipport + URL_DASHBOARD + str_username;
            URL_SITE = ipport + URL_UPDATE_SITE;
            URL_MV = ipport + URL_MY_VISIT + str_username;
            URL_OV = ipport + URL_OUTSTANDING + str_username;
            URL_UD = ipport + URL_USER_DETAIL + str_username;
            //Log.e("a", URL_FINAL);
        } catch (Exception e) {
            return;
        }

        //if (update_status.contains("true")) {

        //final AlertDialog.Builder warningAlertDialog = new AlertDialog.Builder(MainActivity.this);

        //warningAlertDialog.setTitle("WMI");
        //warningAlertDialog.setMessage("Update Tersedia.\nApakah ingin unduh sekarang?");
        //warningAlertDialog.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
        //public void onClick(DialogInterface dialog, int which) {

        //updateApp();

        //}
        //});
        //warningAlertDialog.setNegativeButton("Nanti",new DialogInterface.OnClickListener() {
        //public void onClick(DialogInterface dialog, int which) {

        //dialog.cancel();
        //}
        //});

        //warningAlertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        //warningAlertDialog.show();

        //}

        btnmyvisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int int2 = Integer.valueOf(str_myvisit.toString());

                if (int2 == 0) {

                    alert("i-MAV", "Bulan ini belum ada kunjungan.");

                } else {

                    seeMyVisit();

                }

            }
        });

        btnoutstanding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int int2 = Integer.valueOf(str_myoutstanding.toString());

                if (int2 == 0) {

                    alert("i-MAV", "Tidak ada yang Outstanding.");

                } else {

                    seeMyOutstanding();

                }

            }
        });

        checkIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mdialog = new ProgressDialog(MainActivity.this);
                mdialog.setMessage("Loading..");
                mdialog.setIndeterminate(false);
                mdialog.setCancelable(false);
                new CountDownTimer(1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        mdialog.show();
                    }

                    public void onFinish() {
                        mdialog.dismiss();
                        if (checkIn.getText().toString().trim().contains("offline")) {

                            AddVisitOffline();

                        } else {
                            AddVisit();
                        }

                    }
                }.start();
            }
        });



        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.anim_slide_in_down, R.anim.anim_slide_out_down);

                if(mHandler.hasMessages(1)) {
                    mHandler.removeMessages(1);
                    stopRepeatingTask();
                }
            }
        });

        imgProfile.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        //overlay is black with transparency of 0x77 (119)
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }

                return false;
            }
        });

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
                    lin_place.setVisibility(View.GONE);
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

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String currentDateandTime = sdf.format(new Date());

        timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        int int2 = Integer.valueOf(str_myvisit.toString());

                        if (int2 == 99) {
                            CI_button_off();
                        }

                        setting = getSharedPreferences("WSV_SETTINGS", 0);
                        ip_pref = setting.getString("ip_server", "");
                        port_pref = setting.getString("port_server", "");
                        str_myvisit = setting.getString("my_visit", "");
                        str_myoutstanding = setting.getString("my_outstanding", "");
                        str_myongoing = setting.getString("my_ongoing", "");
                        ipport = "http://" + ip_pref + ":" + port_pref + "/";
                        btnmyvisit.setText(str_myvisit);
                        btnoutstanding.setText(str_myoutstanding);

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();

                        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());

                        if (conType == "No Internet Connection") {

                            CI_button_off();

                        } else {

                            //new dashData().execute();
                            dashDatav2();
                            updatemyVisit();
                            updateoutstandingVisit();
                            post_appversion(usernamedb,version,currentDateandTime);
                            userDetailRequest();
                            new loadDataProfile().execute();
                        }
                    }
                });
            }
        }, 0, 5000);


        notifHandler();
        registerReceiver(closeMyActivity, new IntentFilter("xyz"));
        loadPP();
    }

    private final BroadcastReceiver closeMyActivity = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private void notifHandler() {

        final Handler handler = new Handler();

        runnableCode = new Runnable() {
            @Override
            public void run() {

                final int int_og = Integer.valueOf(str_myongoing.toString());
                final int int_os = Integer.valueOf(str_myoutstanding.toString());
                int time = 0;

                if (int_og > 0) {
                    String Sound = "android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.solemn;
                    showNotification(int_og + " Visit On Going", "Silahkan dilanjutkan", Sound);
                    time = 50000;

                } else {
                    if (int_os > 0) {
                        String Sound = "android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.twirl;
                        showNotification(int_os + " Visit Oustanding", "Segera kirimkan dokumentasi!", Sound);
                        time = 10 * 60 * 1000;
                    }
                }

                // Repeat this the same runnable code block again another 2 seconds
                handler.postDelayed(runnableCode, time);


            }

        };

        handler.post(runnableCode);

    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
                updateApp(MainActivity.this, version);
            updateAppNotif(MainActivity.this, version);
                mHandler.postDelayed(mStatusChecker, 60*60*1000);
        }
    };

    Runnable mStatusCheckerNotif = new Runnable() {
        @Override
        public void run() {
            updateAppNotif(MainActivity.this, version);
            mHandler.postDelayed(mStatusChecker, 60*60*1000);
        }
    };

    void startRepeatingTask() {

        mStatusChecker.run();

    }

  void stopRepeatingTask(){
      mHandler.removeCallbacks(mStatusChecker);
  }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(closeMyActivity);


        AppController.getInstance().setConnectivityListener(this);
    }

    private void checkConnection() {
        isConnected = ConnectivityReceiver.isConnected();
        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());
        conType2 = ConnectivityReceiver.getNetworkClass(getApplicationContext());
        showSnack(isConnected, conType, conType2);
    }

    private void showSnack(boolean isConnected, String conType, String conType2) {
        String message;
        int color;
        if (isConnected) {

            if (conType == "Using Wifi") {
                message = conType + "\nConnected to Internet";
                color = getResources().getColor(R.color.blue_dongker);
            } else {

                message = "Using " + conType2 + " Network\nConnected to Internet";
                color = getResources().getColor(R.color.blue_dongker);
            }


        } else {
            message = conType + "\nCheck your settings again";
            color = Color.RED;


        }

        Snackbar snackbar3 = Snackbar.make(findViewById(R.id.btnLogout), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar3.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.cardview_light_background));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar3.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPP();
        if (str_autoupdate.contains("true")) {

            startRepeatingTask();
            mHandler.sendEmptyMessage(1);

        }
        AppController.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected, String conType, String conType2) {
        showSnack(isConnected, conType, conType2);
    }

    private void CI_button_off() {

        checkIn.setText("CHECK IN (offline)");
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            bg.setBackgroundDrawable(getResources().getDrawable(R.drawable.nightsky));
            checkIn.setBackgroundColor(getResources().getColor(R.color.input_register_bg));
        } else {
            checkIn.setBackgroundColor(getResources().getColor(R.color.input_register_bg));
        }

    }

    private void CI_button() {

        checkIn.setText("CHECK IN");
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            checkIn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            checkIn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        }

    }

    private void buildAlertMessageNoGps() {


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.message_dialog,
                (ViewGroup) findViewById(android.R.id.content), false);
        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
        titletxt.setText("i-MAV");
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

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.message_dialog,
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
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);

        startActivity(intent);
        finish();

        if(mHandler.hasMessages(1)) {
            mHandler.removeMessages(1);
            stopRepeatingTask();
        }


    }

    private void AddVisit() {
        HashMap<String, String> user = db.getUserDetails();

        String pic_checkin = user.get("name");
        String mode_checkin = checkIn.getText().toString().trim();
        Intent intent = new Intent(MainActivity.this, SiteListOffline.class);
        intent.putExtra("mode",mode_checkin);
        intent.putExtra("pic", pic_checkin);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_down, R.anim.anim_slide_out_up);
        if(mHandler.hasMessages(1)) {
            mHandler.removeMessages(1);
            stopRepeatingTask();
        }
    }

    private void AddVisitOffline() {
        HashMap<String, String> user = db.getUserDetails();

        String pic_checkin = user.get("name");
        String mode_checkin = checkIn.getText().toString().trim();
        Intent intent = new Intent(MainActivity.this, SiteListOffline.class);
        intent.putExtra("mode",mode_checkin);
        intent.putExtra("pic", pic_checkin);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_down, R.anim.anim_slide_out_up);
        if(mHandler.hasMessages(1)) {
            mHandler.removeMessages(1);
            stopRepeatingTask();
        }

    }

    private void seeMyVisit() {
        HashMap<String, String> user = db.getUserDetails();

        String pic_checkin = txtName.getText().toString().trim();
        String URL = URL_MY_VISIT;
        Intent intent = new Intent(MainActivity.this, myVisit.class);
        intent.putExtra("pic", pic_checkin);
        intent.putExtra("URL", URL);
        intent.putExtra("title", "MY VISIT");
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_down, R.anim.anim_slide_out_up);
        if(mHandler.hasMessages(1)) {
            mHandler.removeMessages(1);
            stopRepeatingTask();
        }
    }

    private void settings() {

        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        timer1.cancel();
        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        if(mHandler.hasMessages(1)) {
            mHandler.removeMessages(1);
            stopRepeatingTask();
        }

    }

    private void seeMyOutstanding() {
        HashMap<String, String> user = db.getUserDetails();

        String pic_checkin = txtName.getText().toString().trim();
        String URL = URL_OUTSTANDING;
        Intent intent = new Intent(MainActivity.this, outstandingVisit.class);
        intent.putExtra("pic", pic_checkin);
        intent.putExtra("URL", URL);
        intent.putExtra("title", "OUTSTANDING DATA");
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_down, R.anim.anim_slide_out_down);
        if(mHandler.hasMessages(1)) {
            mHandler.removeMessages(1);
            stopRepeatingTask();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Dashboard");
            actionBar.setHomeAsUpIndicator(R.drawable.header_logo);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        } else if (id == R.id.action_setting) {

            settings();


        } else if (id == R.id.action_exit) {

            timer1.cancel();

            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);

        } else if (id == R.id.action_refresh) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            final ProgressDialog pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Refreshing Data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            Picasso.with(getApplicationContext())
                    .load(URL_3)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(target);
            new CountDownTimer(1500, 1000) {

                public void onTick(long millisUntilFinished) {
                    pDialog.show();
                }

                public void onFinish() {
                    pDialog.dismiss();


                    if (conType == "No Internet Connection") {

                        alert("i-MAV", "Periksa kembali koneksi internet Anda");

                    } else {

                        //new dashData().execute();
                        dashDatav2();
                        loadPP();
                        //updateSitelist();
                        updatemyVisit();
                        updateoutstandingVisit();


                        setting = getSharedPreferences("WSV_SETTINGS", 0);
                        str_myvisit = setting.getString("my_visit", "");
                        str_myoutstanding = setting.getString("my_outstanding", "");
                        btnmyvisit.setText(str_myvisit);
                        btnoutstanding.setText(str_myoutstanding);
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.message_dialog,
                (ViewGroup) findViewById(android.R.id.content), false);
        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
        titletxt.setText("i-MAV");
        messagetxt.setText("Ingin keluar i-MAV ?");
        builder.setView(viewInflated);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
        });


        builder.setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        final AlertDialog alert = builder.create();
        alert.show();

    }

    private void updateSitelist() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_SITE, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.e(TAG, response.toString());

                try {

                    JSONArray sitelist = response.getJSONArray("site");

                    for (int i = 0; i < sitelist.length(); i++) {
                        JSONObject c = sitelist.getJSONObject(i);

                        String site_id = c.getString("site_id");

                    }

                    try {
                        File picDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
                        picDir.mkdir();
                        FileWriter file = new FileWriter(picDir + File.separator  +  "sitelist");
                        file.write(response.toString());
                        file.flush();
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    alert("i-MAV", "Koneksi ke server bermasalah.\nSilahkan hubungi NOC WMI");
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                alert("i-MAV", "Koneksi ke server bermasalah.\nSilahkan hubungi NOC WMI");
                //VolleyLog.d(TAG, "Error: " + error.getMessage());

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void updatemyVisit() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_MV, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.e(TAG, response.toString());

                try {


                    JSONArray sitelist = response.getJSONArray("visit");


                    try {
                        File picDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
                        picDir.mkdir();
                        FileWriter file = new FileWriter(picDir + File.separator  +  "myVisit");
                        file.write(response.toString());
                        file.flush();
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();
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

    private void updateoutstandingVisit() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_OV, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.e(TAG, response.toString());

                try {

                    JSONArray sitelist = response.getJSONArray("visit");


                    try {
                        File picDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
                        picDir.mkdir();
                        FileWriter file = new FileWriter(picDir + File.separator  +   "outstandingVisit");
                        file.write(response.toString());
                        file.flush();
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();
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

    private void dashDatav2() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_FINAL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.e(TAG, response.toString());

                try {

                    JSONArray sitelist = response.getJSONArray(TAG_VISITCOUNT);
                    for (int i = 0; i < sitelist.length(); i++) {
                        JSONObject c = sitelist.getJSONObject(i);

                        myvisitstr = c.getString(TAG_MYVISIT);
                        outstandingstr = c.getString(TAG_OUTSTANDING);
                        ongoing_str = c.getString(TAG_ONGOING);

                    }

                    CI_button();
                    setting = getSharedPreferences("WSV_SETTINGS", 0);
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putString("my_visit", myvisitstr);
                    editor.putString("my_outstanding", outstandingstr);
                    editor.putString("my_ongoing", ongoing_str);
                    editor.commit();


                } catch (JSONException e) {
                    e.printStackTrace();
                    CI_button_off();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                CI_button_off();

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }


    public void showNotification(String title, String message, String sound) {

        // define sound URI, the sound to be played when there's a notification
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        HashMap<String, String> user = db.getUserDetails();

        String pic_checkin = txtName.getText().toString().trim();
        String URL = URL_OUTSTANDING;
        Intent intent = new Intent(getApplicationContext(), outstandingVisit.class);
        intent.putExtra("pic", pic_checkin);
        intent.putExtra("URL", URL);
        intent.putExtra("title", "OUTSTANDING DATA");
        PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)

                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_stat_logo)
                .setContentIntent(pIntent)
                .setSound(Uri.parse(sound))
                .addAction(0, "View", pIntent)
                .setVibrate(new long[]{1, 1, 1})
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below
        // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mNotification);
    }

    private void post_appversion(final String username, final String version, final String dt) {
        // Tag used to cancel the request
        String tag_string_req = "finishing_visit";



        StringRequest strReq = new StringRequest(Request.Method.POST,
                ipport + URL_APP_VERSION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "Post App Version: " + response.toString());


                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {





                    } else {

                        // Error pada add site visit
                        // message


                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d(TAG, "Post App Version: " + error.getMessage());


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("version", version);
                params.put("dt", dt);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    String updatestr;
    String updatelinkstr;
    String versionstr;
    String codestr;

    public void updateApp(final Context context, final String appversion) {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                ipport+URL_APP, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.e(TAG, response.toString());

                try {

                    JSONArray sitelist = response.getJSONArray("statusapp");

                    JSONObject c = sitelist.getJSONObject(0);

                    versionstr = c.getString("version");
                    codestr = c.getString("code");
                    updatestr = c.getString("update");
                    updatelinkstr = c.getString("update_link");

                    //Log.e(versionstr+" # "+appversion,codestr+" ## "+versioncode);

                    Integer codeserver = Integer.parseInt(codestr.toString());
                    Integer codedapp = Integer.parseInt(versioncode.toString().trim());

                    if (codeserver-codedapp==0){



                    } else  if (codeserver > codedapp){


                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View viewInflated = LayoutInflater.from(context).inflate(R.layout.message_dialog,
                                (ViewGroup) findViewById(android.R.id.content), false);
                        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                        titletxt.setText("i-MAV");
                        messagetxt.setText("Tersedia versi yang baru.\nApakah ingin download sekarang? ");
                        builder.setView(viewInflated);


                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                // Log.e(TAG,"LINK UPDDATE" +update_link);

                                String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
                                String fileName = "update.apk";
                                String fil = destination += fileName;
                                final Uri uri = Uri.parse("file://" + destination);

                                //Delete update file if exists
                                File file = new File(fil);
                                if (file.exists())
                                    //file.delete() - test this, I think sometimes it doesnt work

                                    file.delete();


                                //get url of app on server
                                //String url = "http://121.52.87.128:7777/wmisitevisit/download/apk/update.apk";

                                //set downloadmanager
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(updatelinkstr));
                                request.setDescription("Downloading Update");
                                request.setTitle("i-MAV versi "+versionstr);

                                //set destination
                                request.setDestinationUri(uri);

                                // get download service and enqueue file
                                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                final long downloadId = manager.enqueue(request);

                                //set BroadcastReceiver to install app when .apk is downloaded
                                BroadcastReceiver onComplete = new BroadcastReceiver() {
                                    public void onReceive(Context ctxt, Intent intent) {
                                        Intent install = new Intent(Intent.ACTION_VIEW);
                                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        install.setDataAndType(uri,
                                                manager.getMimeTypeForDownloadedFile(downloadId));
                                        startActivity(install);

                                        unregisterReceiver(this);
                                        finish();
                                    }
                                };
                                //register receiver for when .apk download is compete
                                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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


                } catch (JSONException e) {

                    e.printStackTrace();
                    //alert("i-MAV", "Koneksi Putus\nSilahkan hubungi NOC WMI");

                }


            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                //alert("i-MAV", "Koneksi Putus\nSilahkan hubungi NOC WMI");


            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    public void updateAppNotif(final Context context, final String appversion) {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                ipport+URL_APP, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.e(TAG, response.toString());

                try {

                    JSONArray sitelist = response.getJSONArray("statusapp");

                    JSONObject c = sitelist.getJSONObject(0);

                    versionstr = c.getString("version");
                    codestr = c.getString("code");
                    updatestr = c.getString("update");
                    updatelinkstr = c.getString("update_link");

                    //Log.e(versionstr+" # "+appversion,codestr+" ## "+versioncode);

                    Integer codeserver = Integer.parseInt(codestr.toString());
                    Integer codedapp = Integer.parseInt(versioncode.toString().trim());

                    if (codeserver-codedapp==0){



                    } else  if (codeserver > codedapp){

                        Intent serviceIntent = new Intent(getApplicationContext(),BackgroundUpdateApp.class);
                        serviceIntent.putExtra("link", updatelinkstr); //data yang dikirim
                        serviceIntent.putExtra("version", version); //data yang dikirim
                        getApplicationContext().startService(serviceIntent); //mulai jalankan
                        PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this,0,serviceIntent,0);

                        Notification mNotification = new Notification.Builder(MainActivity.this)

                                .setContentTitle("i-MAV Update")
                                .setContentText("Terdapat pembaharuan aplikasi")
                                .setSmallIcon(R.drawable.ic_stat_logo)
                                .setContentIntent(pIntent)
                                .addAction(0, "Download", pIntent)
                                .setAutoCancel(true)
                                .setVibrate(new long[]{1, 1, 1})
                                .setPriority(Notification.PRIORITY_MAX)
                                .build();

                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                        // If you want to hide the notification after it was selected, do the code below
                        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(0, mNotification);

                    }


                } catch (JSONException e) {

                    e.printStackTrace();
                    //alert("i-MAV", "Koneksi Putus\nSilahkan hubungi NOC WMI");

                }


            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                //alert("i-MAV", "Koneksi Putus\nSilahkan hubungi NOC WMI");


            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void loadPP() {

        File imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "img");
        imgDir.mkdirs();

        File picUser = new File(imgDir +"/"+ str_img);
        if (picUser.exists()) {
            Picasso.with(getApplicationContext())
                    .load(picUser)
                    .error(R.drawable.avatar_default_round)
                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .transform(new jp.wasabeef.picasso.transformations.CropCircleTransformation())
                    .into(imgProfile);
        } else {
            Picasso.with(getApplicationContext())
                    .load(URL_3)
                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(target);

            if (picUser.exists()) {

                Picasso.with(getApplicationContext())
                        .load(picUser)
                        .error(R.drawable.avatar_default_round)
                        .memoryPolicy(MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .transform(new jp.wasabeef.picasso.transformations.CropCircleTransformation())
                        .into(imgProfile);

            }

        }

    }

    private Target target = new Target() {

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {

                @Override
                public void run() {


                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        File imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "img");
                        imgDir.mkdirs();
                        File file = new File(imgDir +"/"+ str_img);
                        if (file.exists()){

                            return;

                        } else {
                            try {

                                file.createNewFile();
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                                ostream.close();


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }).start();

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            if (placeHolderDrawable != null) {}
        }

    };

    private void userDetailRequest() {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_UD, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, response.toString());

                try {

                    String state = Environment.getExternalStorageState();

                    File picDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
                    picDir.mkdir();

                    FileWriter file = new FileWriter(picDir + File.separator  + "userdetail");
                    file.write(response.toString());
                    file.flush();
                    file.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                //Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    public String getDataUser() {

        try {
            File picDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
            File f = new File(picDir + File.separator + "userdetail");
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {

            //Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
            return null;
        }
    }

    class loadDataProfile extends AsyncTask<Void, Void, String> {

        String value;
        String name_json;
        String username_json;
        String email_json;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {


            try {
                JSONObject c = new JSONObject(getDataUser());

                name_json= c.getString("name");
                username_json = c.getString("username");
                email_json = c.getString("email");


                editor = setting.edit();
                editor.putString("name", name_json);
                editor.putString("username", username_json);
                editor.putString("email", email_json);
                editor.putString("img", username_json+".png");
                editor.commit();


            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

            return value;
        }


        protected void onPostExecute(String value) {
            super.onPostExecute(value);
            txtName.setText(name_json);
            txtEmail.setText(email_json);
        }


    }


}
