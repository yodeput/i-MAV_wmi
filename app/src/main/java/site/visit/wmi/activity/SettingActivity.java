package site.visit.wmi.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import site.visit.wmi.R;
import site.visit.wmi.app.AppController;
import site.visit.wmi.location.ConnectivityReceiver;

import static site.visit.wmi.app.AppConfig.URL_APP;

public class SettingActivity extends AppCompatActivity implements MainFragment.Callbacks, ConnectivityReceiver.ConnectivityReceiverListener {

    String updatestr;
    String updatelinkstr;
    String versionstr;
    String codestr;
    private ProgressDialog mdialog;
    private ProgressDialog pDialog;
    private ImageView set_btn;
    private TextView ip_txt;
    private TextView port_txt;
    private TextView version_txt;
    private Switch auto_update;
    private Button update_btn;
    private LinearLayout ip_lin;
    private String version;
    private String versioncode;
    private String ip_Text;
    private String port_Text;
    private String str_autoupdate;
    private boolean isConnected;
    private String conType;
    private String erot;
    private String ip_pref;
    private String port_pref;
    private String ipport;
    private SharedPreferences setting;
    private String conType2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        set_btn = (ImageView) findViewById(R.id.set_button);

        ip_txt = (TextView) findViewById(R.id.ip_txt);
        port_txt = (TextView) findViewById(R.id.port_txt);
        version_txt = (TextView) findViewById(R.id.ver_txt);
        TextView auto_status = (TextView) findViewById(R.id.textView39);

        auto_update = (Switch) findViewById(R.id.simpleSwitch);
        update_btn = (Button) findViewById(R.id.btnupdate);


        ip_lin = (LinearLayout) findViewById(R.id.lin_ip);

        checkConnection();
        isConnected = ConnectivityReceiver.isConnected();
        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        str_autoupdate = setting.getString("auto_update", "");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";

        ip_txt.setText(ip_pref);
        port_txt.setText(port_pref);

        if (findViewById(R.id.container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, MainFragment.newInstance("Placeholder"))
                    .commit();
        }

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            Integer kode = pInfo.versionCode;
            versioncode = kode.toString().trim();
        } catch (Exception e) {

        }

        auto_status.setText(str_autoupdate);

        if (str_autoupdate.contains("true")) {

            auto_update.setChecked(true);

        } else if (str_autoupdate.contains("false")) {

            auto_update.setChecked(false);

        }


        auto_update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    SharedPreferences.Editor editor = setting.edit();
                    editor.putString("auto_update", "true");
                    editor.commit();
                } else {
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putString("auto_update", "false");
                    editor.commit();
                }

            }

        });

        version_txt.setText(version);

        ip_lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                View viewInflated = LayoutInflater.from(SettingActivity.this).inflate(R.layout.ip_layout_dialog,
                        (ViewGroup) findViewById(android.R.id.content), false);
                final EditText input_ip = (EditText) viewInflated.findViewById(R.id.input_ip);
                final EditText input_ip_backup = (EditText) viewInflated.findViewById(R.id.input_ip_backup);
                final EditText input_port = (EditText) viewInflated.findViewById(R.id.input_port);
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {
                        if (end > start) {
                            String destTxt = dest.toString();
                            String resultingTxt = destTxt.substring(0, dstart) +
                                    source.subSequence(start, end) +
                                    destTxt.substring(dend);
                            if (!resultingTxt.matches("^\\d{1,3}(\\." +
                                    "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                                return "";
                            } else {
                                String[] splits = resultingTxt.split("\\.");
                                for (int i = 0; i < splits.length; i++) {
                                    if (Integer.valueOf(splits[i]) > 255) {
                                        alert("i-MAV","Alamat IP Error! \nMaks 255.255.255.255");
                                    }
                                }
                            }
                        }
                        return null;
                    }
                };
                input_ip_backup.setFilters(filters);
                final RadioGroup radioGroup = (RadioGroup) viewInflated.findViewById(R.id.radioGroup);
                final RadioButton rb_primary = (RadioButton) viewInflated.findViewById(R.id.rb_primary);
                final RadioButton rb_backup = (RadioButton) viewInflated.findViewById(R.id.rb_backup);
                final LinearLayout ip_backup_lin = (LinearLayout) viewInflated.findViewById(R.id.custom_ip_lin);
                input_ip.setText(ip_pref);
                if (input_ip.getText().toString().contains("112.215.192.131")) {
                    radioGroup.check(R.id.rb_primary);
                    rb_primary.setChecked(true);
                    ip_backup_lin.setVisibility(View.GONE);
                } else {
                    input_ip_backup.setText(ip_pref);
                    radioGroup.check(R.id.rb_backup);
                    rb_backup.setChecked(true);
                    ip_backup_lin.setVisibility(View.VISIBLE);
                }

                input_port.setText(port_pref);
                builder.setView(viewInflated);


                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.rb_backup) {
                            ip_backup_lin.setVisibility(View.VISIBLE);
                        } else if (checkedId == R.id.rb_primary) {
                            ip_backup_lin.setVisibility(View.GONE);
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ip_Text = input_ip_backup.getText().toString();
                        if (rb_backup.isChecked()) {

                            if ((ip_Text=="") || (ip_Text==" ") || ip_Text.isEmpty()) {

                                alert("i-MAV", "Kolom IP Backup belum terisi");

                            } else {
                                port_Text = input_port.getText().toString();
                                ip_txt.setText(ip_Text);
                                port_txt.setText(port_Text);
                                SharedPreferences.Editor editor = setting.edit();
                                editor.putString("ip_server", ip_Text);
                                editor.putString("port_server", port_Text);
                                editor.commit();
                                Intent intent = new Intent(SettingActivity.this, SettingActivity.class);

                                startActivity(intent);

                                finish();
                            }
                        } else if (rb_primary.isChecked()) {

                            ip_Text = "112.215.192.131";
                            port_Text = input_port.getText().toString();
                            ip_txt.setText(ip_Text);
                            port_txt.setText(port_Text);
                            SharedPreferences.Editor editor = setting.edit();
                            editor.putString("ip_server", ip_Text);
                            editor.putString("port_server", port_Text);
                            editor.commit();
                            Intent intent = new Intent(SettingActivity.this, SettingActivity.class);

                            startActivity(intent);

                            finish();
                        }


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()

                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                Dialog d = builder.show();


            }
        });


        update_btn.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {

                if (conType == "No Internet Connection") {

                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                    View viewInflated = LayoutInflater.from(SettingActivity.this).inflate(R.layout.message_dialog,
                            (ViewGroup) findViewById(android.R.id.content), false);
                    final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                    final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                    titletxt.setText("i-MAV");
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
                    String appversion = version_txt.getText().toString().trim();
                    updateApp(SettingActivity.this, appversion);

                }


            }
        });

        set_btn.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                View viewInflated = LayoutInflater.from(SettingActivity.this).inflate(R.layout.ip_layout_dialog,
                        (ViewGroup) findViewById(android.R.id.content), false);
                final EditText input_ip = (EditText) viewInflated.findViewById(R.id.input_ip);
                final EditText input_ip_backup = (EditText) viewInflated.findViewById(R.id.input_ip_backup);
                final EditText input_port = (EditText) viewInflated.findViewById(R.id.input_port);
                final RadioGroup radioGroup = (RadioGroup) viewInflated.findViewById(R.id.radioGroup);
                final RadioButton rb_primary = (RadioButton) viewInflated.findViewById(R.id.rb_primary);
                final RadioButton rb_backup = (RadioButton) viewInflated.findViewById(R.id.rb_backup);
                final LinearLayout ip_backup_lin = (LinearLayout) viewInflated.findViewById(R.id.custom_ip_lin);
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {
                        if (end > start) {
                            String destTxt = dest.toString();
                            String resultingTxt = destTxt.substring(0, dstart) +
                                    source.subSequence(start, end) +
                                    destTxt.substring(dend);
                            if (!resultingTxt.matches("^\\d{1,3}(\\." +
                                    "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                                return "";
                            } else {
                                String[] splits = resultingTxt.split("\\.");
                                for (int i = 0; i < splits.length; i++) {
                                    if (Integer.valueOf(splits[i]) > 255) {
                                        alert("i-MAV","Alamat IP Error! \nMaksimal IP 255.255.255.255");
                                    }
                                }
                            }
                        }
                        return null;
                    }
                };
                input_ip_backup.setFilters(filters);
                if (input_ip.getText().toString().contains("112.215.192.131")) {
                    radioGroup.check(R.id.rb_primary);
                    rb_primary.setChecked(true);
                    ip_backup_lin.setVisibility(View.GONE);
                } else {
                    input_ip_backup.setText(ip_pref);
                    radioGroup.check(R.id.rb_backup);
                    rb_backup.setChecked(true);
                    ip_backup_lin.setVisibility(View.VISIBLE);
                }


                input_port.setText(port_pref);
                builder.setView(viewInflated);


                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.rb_backup) {
                            ip_backup_lin.setVisibility(View.VISIBLE);
                        } else if (checkedId == R.id.rb_primary) {
                            ip_backup_lin.setVisibility(View.GONE);
                        }
                    }
                });


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ip_Text = input_ip_backup.getText().toString();
                        if (rb_backup.isChecked()) {

                            if ((ip_Text=="") || (ip_Text==" ") || ip_Text.isEmpty()) {

                                alert("i-MAV", "Kolom IP Backup belum terisi");

                            } else {
                                port_Text = input_port.getText().toString();
                                ip_txt.setText(ip_Text);
                                port_txt.setText(port_Text);
                                SharedPreferences.Editor editor = setting.edit();
                                editor.putString("ip_server", ip_Text);
                                editor.putString("port_server", port_Text);
                                editor.commit();
                                Intent intent = new Intent(SettingActivity.this, SettingActivity.class);

                                startActivity(intent);

                                finish();
                            }
                        } else if (rb_primary.isChecked()) {

                            ip_Text = "112.215.192.131";
                            port_Text = input_port.getText().toString();
                            ip_txt.setText(ip_Text);
                            port_txt.setText(port_Text);
                            SharedPreferences.Editor editor = setting.edit();
                            editor.putString("ip_server", ip_Text);
                            editor.putString("port_server", port_Text);
                            editor.commit();
                            Intent intent = new Intent(SettingActivity.this, SettingActivity.class);

                            startActivity(intent);

                            finish();
                        }
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

        set_btn.setOnTouchListener(new View.OnTouchListener()

        {

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


        pDialog = new

                ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

    }

    @Override
    protected void onStart() {
        super.onStart();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Settings");
            actionBar.setDisplayHomeAsUpEnabled(true);

        }


    }

    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void passDataToActivity(String data) {
        // Do nothing yet...
    }

    public void onBackPressed() {

        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);

    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void updateApp(final Context context, final String appversion) {
        showpDialog();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                ipport + URL_APP, null, new Response.Listener<JSONObject>() {

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

                    Log.e(versionstr + " # " + appversion, codestr + " ## " + versioncode);

                    Integer codeserver = Integer.parseInt(codestr.toString());
                    Integer codedapp = Integer.parseInt(versioncode.toString().trim());

                    if ((codeserver - codedapp == 0) || (codedapp > codeserver)) {


                        alert("i-MAV", "Sudah menggunakan versi terbaru");


                    } else if (codeserver > codedapp) {


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
                                request.setTitle("i-MAV versi " + versionstr);

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
                    hidepDialog();
                    e.printStackTrace();
                    alert("i-MAV", "Koneksi Putus\nSilahkan hubungi NOC WMI");

                }

                hidepDialog();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                alert("i-MAV", "Koneksi Putus\nSilahkan hubungi NOC WMI");


            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
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

        Snackbar snackbar3 = Snackbar.make(findViewById(R.id.btnupdate), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar3.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.blue_dongker));
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

    private void alert(String title, String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        View viewInflated = LayoutInflater.from(SettingActivity.this).inflate(R.layout.message_dialog,
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
