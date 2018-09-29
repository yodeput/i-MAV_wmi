package site.visit.wmi.visit;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import site.visit.wmi.R;
import site.visit.wmi.activity.MainActivity;
import site.visit.wmi.app.AppController;
import site.visit.wmi.helper.SQLiteHandler;
import site.visit.wmi.helper.SessionManager;
import site.visit.wmi.location.ConnectivityReceiver;
import site.visit.wmi.location.GPSTracker;

import static site.visit.wmi.app.AppConfig.URL_VISIT_FINISH;
import static site.visit.wmi.app.AppConfig.URL_VISIT_START;

/**
 * Created by NOC WMI on 20/09/2016.
 */
public class VisitAdd extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = VisitAdd.class.getSimpleName();
    private static final String TAG_SITEID = "site_id";
    private static final String TAG_HOSTCODE = "host_code";
    private static final String TAG_SITENAME = "site_name";
    private static final String TAG_CLUSTER = "cluster";
    private static final String TAG_MONITORING = "monitoring";
    private static final String TAG_CUSTOMER = "customer";
    private Button Checkinbtn;
    private Button Checkoutbtn;
    private Button simpanParameter;
    private Button simpanRunning;
    private Button simpanFuel;
    private Button simpanRemark;
    private Button isiData;
    private Button kirimsms;
    private Button sharepicture;
    private EditText input_busv;
    private EditText input_loadi;
    private EditText input_rpd;
    private EditText input_rh;
    private EditText input_fuelrest;
    private EditText input_refuel;
    private EditText input_remark;
    private TextView picname_txt;
    private TextView site_idtxt;
    private TextView host_codetxt;
    private TextView site_nametxt;
    private TextView clustertxt;
    private TextView monitoringtxt;
    private TextView customertxt;
    private TextView activitytxt;
    private TextView starttimetxt;
    private TextView finishtimetxt;
    private TextView busvtxt;
    private TextView loaditxt;
    private TextView rpdtxt;
    private TextView rhtxt;
    private TextView fuelresttxt;
    private TextView refueltxt;
    private TextView remarktxt;
    private TextView lattxt;
    private TextView longtxt;
    private TableRow row_parameter, row_running, row_fuel, row_remark;
    private LinearLayout lin_kateg_atas;
    private LinearLayout lin_kateg_bawah;
    private LinearLayout lin_start;
    private LinearLayout lin_finish;
    private LinearLayout lin_paramater;
    private LinearLayout lin_running;
    private LinearLayout lin_fuel;
    private LinearLayout lin_position;
    private LinearLayout lin_remark;

    private ImageView imgNavHeaderBg, imgProfile;
    private RelativeLayout rel_pp;
    private TableLayout remark_tabel;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private Timer duration;
    private Chronometer timer;
    private SimpleDateFormat sdf;
    private Spinner pilihan_kateg;
    private NotificationManager manager;
    private Notification myNotication;
    private GPSTracker gps;
    private String conType2;

    private String str_username;
    private String str_name;
    private String str_email;
    private String str_img;
    private String ip_pref;
    private String port_pref;
    private String ipport;
    private SharedPreferences setting;
    private String packageName;

    private boolean isConnected;
    private String conType;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_add);

        packageName =  this.getPackageName();
        View view = findViewById(R.id.scrollView);
        View root = view.getRootView();
        root.setBackgroundColor(getResources().getColor(R.color.btn_login));

        checkConnection();
        isConnected = ConnectivityReceiver.isConnected();
        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        str_username = setting.getString("username", "");
        str_name = setting.getString("name", "");
        str_email = setting.getString("email", "");
        str_img = setting.getString("img", "");
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";


        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        final String nama_pic = str_name;
        String email_pic = user.get("email");

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        timer = (Chronometer) findViewById(R.id.chronometer);
        pilihan_kateg = (Spinner) findViewById(R.id.kateg_spinner);


        input_busv = (EditText) findViewById(R.id.in_busv);
        input_loadi = (EditText) findViewById(R.id.in_loadi);
        input_rpd = (EditText) findViewById(R.id.in_rpd);
        input_rh = (EditText) findViewById(R.id.in_rh);
        input_fuelrest = (EditText) findViewById(R.id.in_fuelrest);
        input_refuel = (EditText) findViewById(R.id.in_refuel);
        input_remark = (EditText) findViewById(R.id.in_remark);

        picname_txt = (TextView) findViewById(R.id.textPIC_name);
        activitytxt = (TextView) findViewById(R.id.activitytxt);
        starttimetxt = (TextView) findViewById(R.id.starttimetxt);
        finishtimetxt = (TextView) findViewById(R.id.finishtimetxt);

        busvtxt = (TextView) findViewById(R.id.busvtxt);
        loaditxt = (TextView) findViewById(R.id.loaditxt);
        fuelresttxt = (TextView) findViewById(R.id.fuelresttxt);
        refueltxt = (TextView) findViewById(R.id.refueltxt);
        rpdtxt = (TextView) findViewById(R.id.rpdtxt);
        rhtxt = (TextView) findViewById(R.id.rhtxt);
        remarktxt = (TextView) findViewById(R.id.remarktxt);
        lattxt = (TextView) findViewById(R.id.lattxt);
        longtxt = (TextView) findViewById(R.id.longtxt);

        Checkinbtn = (Button) findViewById(R.id.AddSiteVisit);
        Checkoutbtn = (Button) findViewById(R.id.SiteVisitOut);
        simpanParameter = (Button) findViewById(R.id.simpanParameter);
        simpanRunning = (Button) findViewById(R.id.simpan_rh);
        simpanFuel = (Button) findViewById(R.id.simpan_fuel);
        isiData = (Button) findViewById(R.id.addData);
        simpanRemark = (Button) findViewById(R.id.simpan_remark);
        kirimsms = (Button) findViewById(R.id.kirimSMS);
        sharepicture = (Button) findViewById(R.id.sharePict);

        row_parameter = (TableRow) findViewById(R.id.tegangan_row);
        row_running = (TableRow) findViewById(R.id.running_row);
        row_fuel = (TableRow) findViewById(R.id.fuel_row);
        row_remark = (TableRow) findViewById(R.id.remark_row);
        remark_tabel = (TableLayout) findViewById(R.id.remark_tabel);

        lin_kateg_atas = (LinearLayout) findViewById(R.id.lin_kategatas);
        lin_kateg_bawah = (LinearLayout) findViewById(R.id.lin_kategbawah);
        lin_start = (LinearLayout) findViewById(R.id.lin_startatas);
        lin_finish = (LinearLayout) findViewById(R.id.lin_finishatas);
        lin_paramater = (LinearLayout) findViewById(R.id.lin_parameter);
        lin_running = (LinearLayout) findViewById(R.id.lin_running);
        lin_fuel = (LinearLayout) findViewById(R.id.lin_fuel);
        lin_remark = (LinearLayout) findViewById(R.id.lin_remark);
        lin_position = (LinearLayout) findViewById(R.id.lin_position);

        imgProfile = (ImageView) findViewById(R.id.img_profile);
        rel_pp = (RelativeLayout) findViewById(R.id.rel_pp);
        rel_pp.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> adapterkateg = ArrayAdapter.createFromResource(this, R.array.kategory_array, android.R.layout.simple_spinner_item);
        adapterkateg.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pilihan_kateg.setAdapter(adapterkateg);
        pilihan_kateg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //Log.v("item", (String) parent.getItemAtPosition(position));
                String catcat = (String) parent.getItemAtPosition(position);
                activitytxt.setText(catcat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Intent i = getIntent();
        String site_id = i.getStringExtra(TAG_SITEID);
        final String host_code = i.getStringExtra(TAG_HOSTCODE);
        final String site_name = i.getStringExtra(TAG_SITENAME);
        String cluster = i.getStringExtra(TAG_CLUSTER);
        String monitoring = i.getStringExtra(TAG_MONITORING);
        String customer = i.getStringExtra(TAG_CUSTOMER);
        final String mode_checkin = i.getStringExtra("mode");

        //GPS Loc Start

        gps = new GPSTracker(getApplicationContext());
        final double latitude = gps.getLatitude();
        final double longitude = gps.getLongitude();
        lattxt.setText(Double.toString(latitude));
        longtxt.setText(Double.toString(longitude));
        loadPP();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        final double latitude = gps.getLatitude();
                        final double longitude = gps.getLongitude();
                        lattxt.setText(Double.toString(latitude));
                        longtxt.setText(Double.toString(longitude));

                    }
                });

            }
        }, 0, 100);

        //GPS Loc End

        site_idtxt = (TextView) findViewById(R.id.site_id_txt);
        host_codetxt = (TextView) findViewById(R.id.host_code_txt);
        site_nametxt = (TextView) findViewById(R.id.site_name_txt);
        clustertxt = (TextView) findViewById(R.id.cluster_txt);
        monitoringtxt = (TextView) findViewById(R.id.monitoring_txt);
        customertxt = (TextView) findViewById(R.id.customer_txt);

        site_idtxt.setText(site_id);
        host_codetxt.setText(host_code);
        site_nametxt.setText(site_name);
        clustertxt.setText(cluster);
        monitoringtxt.setText(monitoring);
        customertxt.setText(customer);
        picname_txt.setText(str_name);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String currentDateandTime = sdf.format(new Date());


        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        duration = new Timer();
        Checkinbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final String site_id = site_idtxt.getText().toString().trim();
                final String site_name = site_nametxt.getText().toString().trim();
                final String category = activitytxt.getText().toString().trim();
                final String pic = nama_pic;
                starttimetxt.setText(currentDateandTime);
                final String start_time = starttimetxt.getText().toString().trim();
                final String latitude = lattxt.getText().toString().trim();
                final String longitude = longtxt.getText().toString().trim();

                final Boolean aucan = Boolean.TRUE;
                if (!category.isEmpty() && !pic.isEmpty()) {

                    if (category.contains("Pilih")) {

                        alert("i-MAV", "Pilih aktivitas yang akan dilakukan");


                    } else {

                        if (mode_checkin.contains("offline")) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                            View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                                    (ViewGroup) findViewById(android.R.id.content), false);
                            final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                            final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                            titletxt.setText("i-MAV");
                            messagetxt.setText("Koneksi ke server bermasalah.\nPastikan pulsa mencukupi untuk mengirim SMS");
                            builder.setView(viewInflated);

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pDialog = new ProgressDialog(VisitAdd.this);
                                    pDialog.setMessage("Check In..");
                                    pDialog.setIndeterminate(false);
                                    pDialog.setCancelable(false);
                                    new CountDownTimer(1000, 1000) {

                                        public void onTick(long millisUntilFinished) {

                                            pDialog.show();
                                        }

                                        public void onFinish() {
                                            startVisitOffline(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);

                                            pDialog.dismiss();
                                        }

                                    }.start();
                                }
                            });
                            builder.setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.cancel();

                                }
                            });

                            Dialog dd = builder.show();


                        } else {

                            if (conType == "No Internet Connection") {

                                AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                                View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                                        (ViewGroup) findViewById(android.R.id.content), false);
                                final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                                final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                                titletxt.setText("i-MAV");
                                messagetxt.setText("Tidak ada koneksi Internet.\nPastikan pulsa mencukupi untuk mengirim SMS");
                                builder.setView(viewInflated);

                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pDialog = new ProgressDialog(VisitAdd.this);
                                        pDialog.setMessage("Check In..");
                                        pDialog.setIndeterminate(false);
                                        pDialog.setCancelable(false);
                                        new CountDownTimer(1000, 1000) {

                                            public void onTick(long millisUntilFinished) {

                                                pDialog.show();
                                            }

                                            public void onFinish() {
                                                startVisitOffline(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);

                                                pDialog.dismiss();
                                            }

                                        }.start();
                                    }
                                });
                                builder.setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.cancel();

                                    }
                                });

                                Dialog dd = builder.show();


                            } else if (conType2 == "2G" && conType == "Using Wifi") {


                                StartVisit(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);


                            } else if (conType2 == "2G") {

                                final AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                                final View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog_button,
                                        (ViewGroup) findViewById(android.R.id.content), false);
                                final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                                final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                                final Button sms_checkin = (Button) viewInflated.findViewById(R.id.dialog_cancel);
                                final Button internet_checkin = (Button) viewInflated.findViewById(R.id.dialog_ok);
                                titletxt.setText("i-MAV");
                                messagetxt.setText("Jaringan Internet: 2G\nSilahkan pilih Mode Check Out");
                                sms_checkin.setText("SMS");
                                internet_checkin.setText("Internet");
                                builder.setView(viewInflated);
                                final AlertDialog dialog2 = builder.create();
                                dialog2.show();

                                sms_checkin.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        dialog2.dismiss();

                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(VisitAdd.this);
                                        View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                                                (ViewGroup) findViewById(android.R.id.content), false);
                                        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                                        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                                        titletxt.setText("i-MAV");
                                        messagetxt.setText("Pastikan pulsa mencukupi untuk mengirim SMS");
                                        builder2.setView(viewInflated);

                                        builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pDialog = new ProgressDialog(VisitAdd.this);
                                                pDialog.setMessage("Check In..");
                                                pDialog.setIndeterminate(false);
                                                pDialog.setCancelable(false);
                                                new CountDownTimer(1000, 1000) {

                                                    public void onTick(long millisUntilFinished) {

                                                        pDialog.show();
                                                    }

                                                    public void onFinish() {
                                                        startVisitOffline(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);

                                                        pDialog.dismiss();
                                                    }

                                                }.start();
                                            }
                                        });
                                        builder2.setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                dialog.cancel();

                                            }
                                        });

                                        Dialog dd = builder2.show();


                                    }
                                });


                                internet_checkin.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        dialog2.dismiss();

                                        StartVisit(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);

                                    }
                                });


                            } else {

                                StartVisit(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);
                            }

                        }
                    }

                    }else{


                        alert("i-MAV", "Parameter yang dibutuhkan belum lengkap.");

                    }
                }
            }

            );

            isiData.setOnClickListener(new View.OnClickListener()

            {
                public void onClick (View view){

                TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                animate.setDuration(500);
                animate.setFillAfter(false);
                isiData.setAnimation(animate);
                isiData.setVisibility(View.GONE);


                TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                animate2.setDuration(500);
                animate2.setFillAfter(false);
                lin_paramater.setAnimation(animate2);
                lin_paramater.setVisibility(View.VISIBLE);


            }

            }

            );


            simpanParameter.setOnClickListener(new View.OnClickListener()

            {
                public void onClick ( final View view){
                final String busv = input_busv.getText().toString().trim();
                final String loadi = input_loadi.getText().toString().trim();
                if (!busv.isEmpty() && !loadi.isEmpty()) {

                    pDialog = new ProgressDialog(VisitAdd.this);
                    pDialog.setMessage("Saving..");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    new CountDownTimer(1000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            pDialog.show();
                        }

                        public void onFinish() {
                            pDialog.dismiss();

                            busvtxt.setText(busv);
                            loaditxt.setText(loadi);

                            row_parameter.setVisibility(View.VISIBLE);

                            String category = activitytxt.getText().toString().trim();
                            if (category.contains("PM")) {


                                TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                                animate.setDuration(500);
                                animate.setFillAfter(false);
                                lin_paramater.setAnimation(animate);
                                lin_paramater.setVisibility(View.GONE);


                                TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                                animate2.setDuration(500);
                                animate2.setFillAfter(true);
                                lin_running.setAnimation(animate2);
                                lin_running.setVisibility(View.VISIBLE);


                            } else if (category.contains("REFUEL")) {

                                busvtxt.setText(busv);
                                loaditxt.setText(loadi);

                                TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                                animate.setDuration(500);
                                animate.setFillAfter(false);
                                lin_paramater.setAnimation(animate);
                                lin_paramater.setVisibility(View.GONE);


                                TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                                animate2.setDuration(500);
                                animate2.setFillAfter(true);
                                lin_fuel.setAnimation(animate2);
                                lin_fuel.setVisibility(View.VISIBLE);

                                rpdtxt.setText("0");
                                rhtxt.setText("0");

                            } else {

                                AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                                View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                                        (ViewGroup) findViewById(android.R.id.content), false);
                                final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                                final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                                titletxt.setText("i-MAV");
                                messagetxt.setText("Apakah ada Remark?");
                                builder.setView(viewInflated);

                                builder.setPositiveButton("Ada", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        rpdtxt.setText("0");
                                        rhtxt.setText("0");
                                        fuelresttxt.setText("0");
                                        refueltxt.setText("0");
                                        remarktxt.setText("0");

                                        TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                                        animate.setDuration(500);
                                        animate.setFillAfter(false);
                                        lin_paramater.setAnimation(animate);
                                        lin_paramater.setVisibility(View.GONE);
                                        lin_fuel.setVisibility(View.GONE);


                                        TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                                        animate2.setDuration(500);
                                        animate2.setFillAfter(true);
                                        lin_remark.setAnimation(animate2);
                                        lin_remark.setVisibility(View.VISIBLE);
                                    }
                                });

                                builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        rpdtxt.setText("0");
                                        rhtxt.setText("0");
                                        fuelresttxt.setText("0");
                                        refueltxt.setText("0");
                                        remarktxt.setText("-");

                                        TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                                        animate.setDuration(500);
                                        animate.setFillAfter(false);
                                        lin_paramater.setAnimation(animate);
                                        lin_paramater.setVisibility(View.GONE);
                                        remark_tabel.setVisibility(View.VISIBLE);


                                        TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                                        animate2.setDuration(500);
                                        animate2.setFillAfter(true);
                                        Checkoutbtn.setAnimation(animate2);
                                        Checkoutbtn.setVisibility(View.VISIBLE);
                                        dialog.cancel();

                                    }
                                });

                                Dialog dd = builder.show();


                            }
                        }
                    }.start();

                } else {

                    alert("i-MAV", "Parameter masih kosong.");

                }


            }

            }

            );

            simpanRunning.setOnClickListener(new View.OnClickListener()

            {
                public void onClick ( final View view){
                final String rpd = input_rpd.getText().toString().trim();
                final String rh = input_rh.getText().toString().trim();
                if (!rpd.isEmpty() && !rh.isEmpty()) {

                    pDialog = new ProgressDialog(VisitAdd.this);
                    pDialog.setMessage("Saving..");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    new CountDownTimer(1000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            pDialog.show();
                        }

                        public void onFinish() {
                            pDialog.dismiss();


                            rpdtxt.setText(rpd);
                            rhtxt.setText(rh);
                            row_running.setVisibility(View.VISIBLE);

                            TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                            animate.setDuration(500);
                            animate.setFillAfter(false);
                            lin_running.setAnimation(animate);
                            lin_running.setVisibility(View.GONE);


                            TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                            animate2.setDuration(500);
                            animate2.setFillAfter(true);
                            lin_fuel.setAnimation(animate2);
                            lin_fuel.setVisibility(View.VISIBLE);

                        }
                    }.start();
                } else {
                    alert("i-MAV", "Parameter masih kosong.");

                }

            }

            }

            );
            simpanFuel.setOnClickListener(new View.OnClickListener()

            {
                public void onClick ( final View view){
                final String fuelrest = input_fuelrest.getText().toString().trim();
                final String refuel = input_refuel.getText().toString().trim();
                if (!fuelrest.isEmpty() && !refuel.isEmpty()) {

                    pDialog = new ProgressDialog(VisitAdd.this);
                    pDialog.setMessage("Saving..");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    new CountDownTimer(1000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            pDialog.show();
                        }

                        public void onFinish() {
                            pDialog.dismiss();


                            fuelresttxt.setText(fuelrest);
                            refueltxt.setText(refuel);

                            AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                            View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                                    (ViewGroup) findViewById(android.R.id.content), false);
                            final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                            final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                            titletxt.setText("i-MAV");
                            messagetxt.setText("Apakah ada Remark?");
                            builder.setView(viewInflated);

                            builder.setPositiveButton("Ada", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    row_fuel.setVisibility(View.VISIBLE);

                                    TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                                    animate.setDuration(500);
                                    animate.setFillAfter(false);
                                    lin_fuel.setAnimation(animate);
                                    lin_fuel.setVisibility(View.GONE);


                                    TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                                    animate2.setDuration(500);
                                    animate2.setFillAfter(true);
                                    lin_remark.setAnimation(animate2);
                                    lin_remark.setVisibility(View.VISIBLE);
                                }
                            });

                            builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    remarktxt.setText("-");
                                    row_fuel.setVisibility(View.VISIBLE);

                                    TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                                    animate.setDuration(500);
                                    animate.setFillAfter(false);
                                    lin_fuel.setAnimation(animate);
                                    lin_fuel.setVisibility(View.GONE);


                                    remark_tabel.setVisibility(View.VISIBLE);

                                    TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                                    animate2.setDuration(500);
                                    animate2.setFillAfter(true);
                                    Checkoutbtn.setAnimation(animate2);
                                    Checkoutbtn.setVisibility(View.VISIBLE);
                                    dialog.cancel();

                                }
                            });

                            Dialog dd = builder.show();

                        }
                    }.start();

                } else {

                    alert("i-MAV", "Parameter masih kosong.");


                }


            }

            }

            );

            simpanRemark.setOnClickListener(new View.OnClickListener()

            {
                public void onClick ( final View view){
                final String remark = input_remark.getText().toString().trim();

                if (!remark.isEmpty()) {

                    pDialog = new ProgressDialog(VisitAdd.this);
                    pDialog.setMessage("Saving..");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    new CountDownTimer(1000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            pDialog.show();
                        }

                        public void onFinish() {
                            pDialog.dismiss();


                            remarktxt.setText(remark);
                            remark_tabel.setVisibility(View.VISIBLE);

                            TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
                            animate.setDuration(500);
                            animate.setFillAfter(false);
                            lin_remark.setAnimation(animate);
                            lin_remark.setVisibility(View.GONE);

                            TranslateAnimation animate2 = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                            animate2.setDuration(500);
                            animate2.setFillAfter(true);
                            Checkoutbtn.setAnimation(animate2);
                            Checkoutbtn.setVisibility(View.VISIBLE);
                            Checkoutbtn.setEnabled(true);


                        }
                    }.start();
                } else {

                    alert("i-MAV", "Remark masih kosong.");


                }


            }

            }

            );

            Checkoutbtn.setOnClickListener(new View.OnClickListener()

            {
                public void onClick (View view){

                AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                        (ViewGroup) findViewById(android.R.id.content), false);
                final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                titletxt.setText("i-MAV");
                messagetxt.setText("Apakah benar ingin Check Out?");
                builder.setView(viewInflated);

                builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String site_id = site_idtxt.getText().toString().trim();
                        final String site_name = site_nametxt.getText().toString().trim();
                        final String category = activitytxt.getText().toString().trim();
                        final String pic = nama_pic;

                        final String busv = input_busv.getText().toString().trim();
                        final String loadi = input_loadi.getText().toString().trim();
                        final String rpd = input_rpd.getText().toString().trim();
                        final String rh = input_rh.getText().toString().trim();
                        final String fuelrest = input_fuelrest.getText().toString().trim();
                        final String refuel = input_refuel.getText().toString().trim();

                        final String remark = remarktxt.getText().toString().trim();

                        final String start_time = "Sudah Selesai " + category;
                        finishtimetxt.setText(currentDateandTime);
                        final String finish_time = finishtimetxt.getText().toString().trim();
                        final Boolean aucan = Boolean.FALSE;

                        if (mode_checkin.contains("offline")) {

                            pDialog = new ProgressDialog(VisitAdd.this);
                            pDialog.setMessage("Check In..");
                            pDialog.setIndeterminate(false);
                            pDialog.setCancelable(false);
                            new CountDownTimer(1000, 1000) {

                                public void onTick(long millisUntilFinished) {

                                    pDialog.show();
                                }

                                public void onFinish() {
                                    FinishVisitOffline(site_id, category, pic, finish_time, aucan, site_name, busv, loadi, rpd, rh, fuelrest, refuel, remark);
                                    manager.cancelAll();
                                    pDialog.dismiss();
                                }

                            }.start();

                        } else {


                            if (conType == "No Internet Connection") {

                                finishtimetxt.setText(currentDateandTime);


                                pDialog = new ProgressDialog(VisitAdd.this);
                                pDialog.setMessage("Check In..");
                                pDialog.setIndeterminate(false);
                                pDialog.setCancelable(false);
                                new CountDownTimer(1000, 1000) {

                                    public void onTick(long millisUntilFinished) {

                                        pDialog.show();
                                    }

                                    public void onFinish() {
                                        FinishVisitOffline(site_id, category, pic, finish_time, aucan, site_name, busv, loadi, rpd, rh, fuelrest, refuel, remark);
                                        manager.cancelAll();
                                        pDialog.dismiss();
                                    }

                                }.start();

                            } else if (conType2 == "2G" && conType == "Using Wifi") {


                                FinishVisit(site_id, category, pic, finish_time, aucan, site_name, busv, loadi, rpd, rh, fuelrest, refuel, remark);
                                manager.cancelAll();


                            } else if (conType2 == "2G") {

                                final AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                                final View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog_button,
                                        (ViewGroup) findViewById(android.R.id.content), false);
                                final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                                final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                                final Button sms_checkin = (Button) viewInflated.findViewById(R.id.dialog_cancel);
                                final Button internet_checkin = (Button) viewInflated.findViewById(R.id.dialog_ok);
                                titletxt.setText("i-MAV");
                                messagetxt.setText("Jaringan Internet: 2G\nSilahkan pilih Mode Check Out");
                                sms_checkin.setText("SMS");
                                internet_checkin.setText("Internet");
                                builder.setView(viewInflated);
                                final AlertDialog dialog2 = builder.create();
                                dialog2.show();

                                sms_checkin.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        dialog2.dismiss();

                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(VisitAdd.this);
                                        View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                                                (ViewGroup) findViewById(android.R.id.content), false);
                                        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                                        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                                        titletxt.setText("i-MAV");
                                        messagetxt.setText("Pastikan pulsa mencukupi untuk mengirim SMS");
                                        builder2.setView(viewInflated);

                                        builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pDialog = new ProgressDialog(VisitAdd.this);
                                                pDialog.setMessage("Check In..");
                                                pDialog.setIndeterminate(false);
                                                pDialog.setCancelable(false);
                                                new CountDownTimer(1000, 1000) {

                                                    public void onTick(long millisUntilFinished) {

                                                        pDialog.show();
                                                    }

                                                    public void onFinish() {
                                                        FinishVisitOffline(site_id, category, pic, finish_time, aucan, site_name, busv, loadi, rpd, rh, fuelrest, refuel, remark);
                                                        manager.cancelAll();
                                                        pDialog.dismiss();
                                                    }

                                                }.start();
                                            }
                                        });
                                        builder2.setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                dialog.cancel();

                                            }
                                        });

                                        Dialog dd = builder2.show();


                                    }
                                });


                                internet_checkin.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        dialog2.dismiss();

                                        FinishVisit(site_id, category, pic, finish_time, aucan, site_name, busv, loadi, rpd, rh, fuelrest, refuel, remark);
                                        manager.cancelAll();

                                    }
                                });


                            } else {

                                FinishVisit(site_id, category, pic, finish_time, aucan, site_name, busv, loadi, rpd, rh, fuelrest, refuel, remark);
                                manager.cancelAll();
                            }
                        }
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

            );


            kirimsms.setOnClickListener(new View.OnClickListener()

            {
                public void onClick (View view){

                final String site_id = site_idtxt.getText().toString().trim();
                final String site_name = site_nametxt.getText().toString().trim();
                final String category = activitytxt.getText().toString().trim();
                final String pic = nama_pic;
                final String start_time = starttimetxt.getText().toString().trim();
                final String finish_time = finishtimetxt.getText().toString().trim();
                final String busv = input_busv.getText().toString().trim();
                final String loadi = input_loadi.getText().toString().trim();
                final String rpd = input_rpd.getText().toString().trim();
                final String rh = input_rh.getText().toString().trim();
                final String fuelrest = input_fuelrest.getText().toString().trim();
                final String refuel = input_refuel.getText().toString().trim();
                final String remark = remarktxt.getText().toString().trim();
                final String latitude = lattxt.getText().toString().trim();
                final String longitude = longtxt.getText().toString().trim();


                kirim_sms_out(site_id, category, pic, start_time, finish_time, busv, loadi, rpd, rh, fuelrest, refuel, remark);


            }

            }

            );

            sharepicture.setOnClickListener(new View.OnClickListener()

            {
                public void onClick (View view){

                File picDir = new File(Environment.getExternalStorageDirectory() + "/Pictures/SiteVisit");
                String sid = site_idtxt.getText().toString().trim();
                String act = activitytxt.getText().toString().trim();
                String fileName = act + "_" + sid + ".jpg";
                File picFile = new File(picDir + "/" + fileName);

                shareImage(picFile);

            }

            }

            );

        }


    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }


    private void Notify(Boolean aucan, String site_name, String start_time) {
        String sn = site_name;

        if (start_time == "Selesai Aktivitas") {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            Notification.Builder builder = new Notification.Builder(VisitAdd.this);

            builder.setAutoCancel(true);
            //builder.setTicker("this is ticker text");
            builder.setContentTitle(sn);
            builder.setContentText(start_time);
            builder.setSmallIcon(R.drawable.ic_stat_logo);
            builder.setContentIntent(pendingIntent);
            builder.setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.start));
            //builder.setSubText("This is subtext...");
            builder.setOngoing(false);
            //builder.setNumber(100);
            builder.build();

            myNotication = builder.getNotification();
            manager.notify(11, myNotication);

        } else {

            Intent intent = new Intent(getApplicationContext(), site.visit.wmi.visit.VisitAdd.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            Notification.Builder builder = new Notification.Builder(VisitAdd.this);

            builder.setAutoCancel(true);
            //builder.setTicker("this is ticker text");
            builder.setContentTitle(sn);
            builder.setContentText(start_time);
            builder.setSmallIcon(R.drawable.ic_stat_logo);
            builder.setContentIntent(pendingIntent);
            //builder.setSubText("This is subtext...");
            builder.setOngoing(false);
            builder.setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.finish));
            //builder.setNumber(100);
            builder.build();

            myNotication = builder.getNotification();
            manager.notify(11, myNotication);

        }
    }

    private void StartVisit(final String site_id, final String category, final String pic,
                            final String start_time, final Boolean aucan, final String site_name,
                            final String latitude, final String longitude) {
        // Tag used to cancel the request
        String tag_string_req = "finishing_visit";

        pDialog.setMessage("Mengirimkan Data..");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                ipport + URL_VISIT_START, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {


                        String errorMsg = jObj.getString("message");

                        alert("i-MAV", errorMsg);

                        pilihan_kateg.setVisibility(View.GONE);


                        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -100);
                        animate.setDuration(500);
                        animate.setFillAfter(false);
                        Checkinbtn.setAnimation(animate);
                        Checkinbtn.setVisibility(View.GONE);


                        TranslateAnimation animate2 = new TranslateAnimation(0, 0, 100, 0);
                        animate2.setDuration(500);
                        animate2.setFillAfter(false);
                        isiData.setAnimation(animate2);
                        isiData.setVisibility(View.VISIBLE);


                        lin_kateg_bawah.setVisibility(View.GONE);

                        lin_kateg_atas.setVisibility(View.VISIBLE);
                        lin_start.setVisibility(View.VISIBLE);

                        timer.setVisibility(View.VISIBLE);
                        timer.start();

                        Notify(aucan, site_name, start_time);


                    } else {

                        // Error pada add site visit
                        // message
                        String errorMsg = jObj.getString("message");
                        alert("i-MAV", errorMsg);


                    }
                } catch (JSONException e) {

                    e.printStackTrace();

                    AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                    View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                            (ViewGroup) findViewById(android.R.id.content), false);
                    final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                    final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                    titletxt.setText("i-MAV");
                    messagetxt.setText("Koneksi ke server bermasalah.\nPastikan pulsa mencukupi untuk mengirim SMS");
                    builder.setView(viewInflated);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pDialog = new ProgressDialog(VisitAdd.this);
                            pDialog.setMessage("Check In..");
                            pDialog.setIndeterminate(false);
                            pDialog.setCancelable(false);
                            new CountDownTimer(1000, 1000) {

                                public void onTick(long millisUntilFinished) {

                                    pDialog.show();
                                }

                                public void onFinish() {
                                    startVisitOffline(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);

                                    pDialog.dismiss();
                                }

                            }.start();
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
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "Registration Error: " + error.getMessage());
                hideDialog();

                AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                        (ViewGroup) findViewById(android.R.id.content), false);
                final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                titletxt.setText("i-MAV");
                messagetxt.setText("Koneksi ke server bermasalah.\nPastikan pulsa mencukupi untuk mengirim SMS");
                builder.setView(viewInflated);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pDialog = new ProgressDialog(VisitAdd.this);
                        pDialog.setMessage("Check In..");
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(false);
                        new CountDownTimer(1000, 1000) {

                            public void onTick(long millisUntilFinished) {

                                pDialog.show();
                            }

                            public void onFinish() {
                                startVisitOffline(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);

                                pDialog.dismiss();
                            }

                        }.start();
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
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("site_id", site_id);
                params.put("category", category);
                params.put("pic", pic);
                params.put("start_time", start_time);
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void FinishVisit(final String site_id, final String category, final String pic,
                             final String finish_time, final Boolean aucan, final String site_name,
                             final String busv, final String loadi, final String rpd, final String rh,
                             final String fuelrest, final String refuel, final String remark) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Mengirimkan Data..");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                ipport + URL_VISIT_FINISH, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDateandTime = sdf.format(new Date());
                        finishtimetxt.setText(currentDateandTime);

                        rel_pp.setVisibility(View.VISIBLE);
                        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -100);
                        animate.setDuration(500);
                        animate.setFillAfter(false);
                        Checkoutbtn.setAnimation(animate);
                        Checkoutbtn.setVisibility(View.GONE);


                        lin_finish.setVisibility(View.VISIBLE);
                        timer.setVisibility(View.GONE);
                        timer.stop();
                        String start_time = "Selesai Aktivitas";
                        Notify(aucan, site_name, start_time);


                        String errorMsg = jObj.getString("message");

                        AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                        View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                                (ViewGroup) findViewById(android.R.id.content), false);
                        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                        titletxt.setText("i-MAV");
                        messagetxt.setText(errorMsg);
                        builder.setView(viewInflated);


                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Bitmap bitmap = takeScreenshot();
                                saveBitmap(bitmap);
                                dialog.dismiss();
                                TranslateAnimation animate2 = new TranslateAnimation(0, 0, 100, 0);
                                animate2.setDuration(500);
                                animate2.setFillAfter(false);
                                sharepicture.setAnimation(animate2);
                                sharepicture.setVisibility(View.VISIBLE);
                            }
                        });

                        Dialog dd = builder.show();

                        //Snackbar snackbar3 = Snackbar.make(findViewById(R.id.AddSiteVisit), errorMsg, Snackbar.LENGTH_LONG);
                        //View sbView = snackbar3.getView();
                        //FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)sbView.getLayoutParams();
                        //params.gravity = Gravity.CENTER_VERTICAL;
                        //sbView.setLayoutParams(params);
                        //TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        //snackbar3.show();

                        // Inserting row in users table


                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("message");
                        alert("i-MAV", errorMsg);
                    }
                } catch (JSONException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                    View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                            (ViewGroup) findViewById(android.R.id.content), false);
                    final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                    final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                    titletxt.setText("i-MAV");
                    messagetxt.setText("Koneksi ke server bermasalah.\nPastikan pulsa mencukupi untuk mengirim SMS");
                    builder.setView(viewInflated);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pDialog = new ProgressDialog(VisitAdd.this);
                            pDialog.setMessage("Check In..");
                            pDialog.setIndeterminate(false);
                            pDialog.setCancelable(false);
                            new CountDownTimer(1000, 1000) {

                                public void onTick(long millisUntilFinished) {

                                    pDialog.show();
                                }

                                public void onFinish() {
                                    FinishVisitOffline(site_id, category, pic, finish_time, aucan, site_name, busv, loadi, rpd, rh, fuelrest, refuel, remark);

                                    pDialog.dismiss();
                                }

                            }.start();
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
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();

                AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
                View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                        (ViewGroup) findViewById(android.R.id.content), false);
                final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                titletxt.setText("i-MAV");
                messagetxt.setText("Koneksi ke server bermasalah.\nPastikan pulsa mencukupi untuk mengirim SMS");
                builder.setView(viewInflated);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pDialog = new ProgressDialog(VisitAdd.this);
                        pDialog.setMessage("Check In..");
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(false);
                        new CountDownTimer(1000, 1000) {

                            public void onTick(long millisUntilFinished) {

                                pDialog.show();
                            }

                            public void onFinish() {
                                FinishVisitOffline(site_id, category, pic, finish_time, aucan, site_name, busv, loadi, rpd, rh, fuelrest, refuel, remark);

                                pDialog.dismiss();
                            }

                        }.start();
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
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("site_id", site_id);
                params.put("category", category);
                params.put("pic", pic);
                params.put("busv", busv);
                params.put("loadi", loadi);
                params.put("rpd", rpd);
                params.put("rh", rh);
                params.put("fuelrest", fuelrest);
                params.put("refuel", refuel);
                params.put("finish_time", finish_time);
                params.put("remark", remark);


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
    public void onBackPressed() {


        if (timer.getVisibility() == View.GONE) {
            finish();
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        } else {
            String sn = site_nametxt.getText().toString().trim();
            String catcat = activitytxt.getText().toString().trim();
            alert("i-MAV", "Maintenance belum selesai.\n\n" +
                    "~~ " + sn + " ~~\n" +
                    "~~ " + catcat + " ~~");

            return;

        }

    }

    public Bitmap takeScreenshot() {
        View rootView = findViewById(R.id.scrollView).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    public void saveBitmap(Bitmap bitmap) {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File picDir = new File(Environment.getExternalStorageDirectory() + "/Pictures/SiteVisit");
            if (!picDir.exists()) {
                picDir.mkdir();
            }

            String sid = site_idtxt.getText().toString().trim();
            String act = activitytxt.getText().toString().trim();
            String fileName = act + "_" + sid + ".jpg";
            File picFile = new File(picDir + "/" + fileName);
            try {
                picFile.createNewFile();
                FileOutputStream picOut = new FileOutputStream(picFile);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), (int) (bitmap.getHeight() / 1.2));//Optional
                boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, picOut);
                if (saved) {
                    Toast.makeText(getApplicationContext(), "Berhasil dicapture.", Toast.LENGTH_SHORT).show();
                    notify();


                } else {
                    //Error
                }
                picOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    private void startVisitOffline(final String site_id, final String category, final String pic,
                                   final String start_time, final Boolean aucan, final String site_name,
                                   final String latitude, final String longitude) {

        alert("i-MAV", "Berhasil Checkin");

        pilihan_kateg.setVisibility(View.GONE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -100);
        animate.setDuration(500);
        animate.setFillAfter(false);
        Checkinbtn.setAnimation(animate);
        Checkinbtn.setVisibility(View.GONE);
        lin_kateg_bawah.setVisibility(View.GONE);

        lin_kateg_atas.setVisibility(View.VISIBLE);
        lin_start.setVisibility(View.VISIBLE);
        TranslateAnimation animate2 = new TranslateAnimation(0, 0, 100, 0);
        animate2.setDuration(500);
        animate2.setFillAfter(false);
        isiData.setAnimation(animate2);
        isiData.setVisibility(View.VISIBLE);
        timer.setVisibility(View.VISIBLE);
        timer.start();

        Notify(aucan, site_name, start_time);
        kirim_sms_in(site_id, category, pic, start_time, aucan, site_name, latitude, longitude);

    }

    private void FinishVisitOffline(final String site_id, final String category, final String pic,
                                    final String finish_time, final Boolean aucan, final String site_name,
                                    final String busv, final String loadi, final String rpd, final String rh,
                                    final String fuelrest, final String refuel, final String remark) {

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        finishtimetxt.setText(currentDateandTime);


        lin_finish.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -100);
        animate.setDuration(500);
        animate.setFillAfter(false);
        Checkoutbtn.setAnimation(animate);
        Checkoutbtn.setVisibility(View.GONE);
        timer.setVisibility(View.GONE);
        rel_pp.setVisibility(View.VISIBLE);

        timer.stop();
        String start_time = "Selesai Aktivitas";
        Notify(aucan, site_name, start_time);

        kirim_sms_out(site_id, category, pic, start_time, finish_time, busv, loadi, rpd, rh, fuelrest, refuel, remark);


        AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
        View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
                (ViewGroup) findViewById(android.R.id.content), false);
        final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
        final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
        titletxt.setText("i-MAV");
        messagetxt.setText("Silahkan kirimkan capture aktivitas ini");
        builder.setView(viewInflated);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap bitmap = takeScreenshot();
                saveBitmap(bitmap);
                //kirimsms.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });

        Dialog dd = builder.show();


    }

    private void kirim_sms_in(final String site_id, final String category, final String pic,
                              final String start_time, final Boolean aucan, final String site_name,
                              final String latitude, final String longitude) {

        final String sms_text_in = pic + "[*A:" + site_id + " *B:" + category + " *C:" + start_time + " *LOC:" + latitude + "," + longitude + "]";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("08112188211", null, sms_text_in, null, null);

        Toast.makeText(getApplicationContext(), "SMS Check-In terkirim..",
                Toast.LENGTH_LONG).show();

    }

    private void kirim_sms_out(final String site_id, final String category, final String pic, final String start_time,
                               final String finish_time, final String busv, final String loadi, final String rpd, final String rh,
                               final String fuelrest, final String refuel, final String remark) {

        final String sms_text_out = pic + "[*A:" + site_id + " *D:" + finish_time + " *E:" + busv + "v *F:" + loadi + "a *G:" + rpd + "/day *H:" + rh + "H *I:" + fuelrest + "L *J:" + refuel + "L *K:" + remark + "]";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("08112188211", null, sms_text_out, null, null);

        //Uri uri = Uri.parse("smsto:089655993365");
        //Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        //it.putExtra("sms_body", "The SMS text");
        //startActivity(it);
        Toast.makeText(getApplicationContext(), "SMS Check-Out terkirim..",
                Toast.LENGTH_LONG).show();


    }

    private void alert(String title, String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(VisitAdd.this);
        View viewInflated = LayoutInflater.from(VisitAdd.this).inflate(R.layout.message_dialog,
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

    private void shareImage(File file) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "No App Available", Toast.LENGTH_SHORT).show();
        }
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
                color = getResources().getColor(R.color.orange300);
            } else {

                message = "Using " + conType2 + " Network\nConnected to Internet";
                color = getResources().getColor(R.color.orange300);
            }


        } else {
            message = conType + "\nCheck your settings again";
            color = Color.RED;


        }

        Snackbar snackbar3 = Snackbar.make(findViewById(R.id.kirimSMS), message, Snackbar.LENGTH_LONG);
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
        showSnack(isConnected, conType, conType2);
    }

    private void loadPP() {

        File imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "img");
        imgDir.mkdirs();

        File picUser = new File(imgDir + "/" + str_img);
        if (picUser.exists()) {
            Picasso.with(getApplicationContext())
                    .load(picUser)
                    .error(R.drawable.avatar_default_round)
                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .transform(new jp.wasabeef.picasso.transformations.CropCircleTransformation())
                    .into(imgProfile);
        }
    }
}
