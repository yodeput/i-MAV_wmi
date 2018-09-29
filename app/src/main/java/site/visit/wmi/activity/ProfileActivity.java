package site.visit.wmi.activity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import site.visit.wmi.R;
import site.visit.wmi.app.AppController;
import site.visit.wmi.location.ConnectivityReceiver;
import site.visit.wmi.location.GPSTracker;

import static site.visit.wmi.app.AppConfig.URL_SAVE_DATA;
import static site.visit.wmi.app.AppConfig.URL_USER_DETAIL;


public class ProfileActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private String URL_2;
    private String packageName;
    private static String TAG = ProfileActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ImageView imgProfile;
    private TableRow tblEdit;

    private TextView text_username, text_email, text_name;
    private TextView text_phone1, text_phone2, text_adress;
    private AlertDialog dialog, alert;

    GPSTracker gps;
    private double latitude, longitude;
    private LinearLayout lin_place;
    private TextView txtLocTop, txtLocBottom;

    private boolean isConnected;
    private String conType;
    private String conType2;

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

    private String name_json;
    private String username_json;
    private String email_json;
    private String phone1_json;
    private String phone2_json;
    private String address_json;

    private String username_edit;
    private String email_edit;
    private String phone1_edit;
    private String phone2_edit;
    private String address_edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        packageName =  this.getPackageName();
        imgProfile = (ImageView) findViewById(R.id.pp_image);
        tblEdit = (TableRow) findViewById(R.id.tbl_editPict);

        text_username = (TextView) findViewById(R.id.text_username);
        text_email = (TextView) findViewById(R.id.text_email);
        text_name = (TextView) findViewById(R.id.text_name);
        text_phone1 = (TextView) findViewById(R.id.text_phone1);
        text_phone2 = (TextView) findViewById(R.id.text_phone2);
        text_adress = (TextView) findViewById(R.id.text_address);

        lin_place = (LinearLayout) findViewById(R.id.lin_place);
        txtLocTop = (TextView) findViewById(R.id.latitude);
        txtLocBottom = (TextView) findViewById(R.id.longitude);

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        str_username = setting.getString("username", "");
        str_name = setting.getString("name", "");
        str_email = setting.getString("email", "");
        str_img = setting.getString("img", "");
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";

        try {
            String pic = URLEncoder.encode(str_username, "UTF-8");
            URL_2 = ipport + URL_USER_DETAIL + pic;
            //Log.e(TAG,URL_1);
            //Log.e(TAG,URL_2);

        } catch (Exception e) {
            return;
        }

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        new loadDataProfile().execute();

        lin_place.setVisibility(View.GONE);

        loadPP();
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadPP();
                //Snackbar.make(v, "Image Profile Clicked", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent i = new Intent(ProfileActivity.this,PPActivity.class);
                i.putExtra("username",str_username);
                startActivity(i);
                overridePendingTransition(R.anim.anim_slide_in_down, R.anim.anim_slide_out_down);
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

        tblEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadPP();
                Intent i = new Intent(ProfileActivity.this,PPActivity.class);
                i.putExtra("username",str_username);
                startActivity(i);
                overridePendingTransition(R.anim.anim_slide_in_down, R.anim.anim_slide_out_down);
                //Snackbar.make(v, "Edit Button Clicked", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            }
        });

        ImageView btn_close = (ImageView) findViewById(R.id.close_button);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);

            }
        });
        btn_close.setOnTouchListener(new View.OnTouchListener() {

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

        ImageView btn_edit = (ImageView) findViewById(R.id.edit_button);
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setCancelable(false);
                View viewInflated = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.dialog_user_data,
                        (ViewGroup) findViewById(android.R.id.content), false);
                final EditText input_email = (EditText) viewInflated.findViewById(R.id.input_email);
                final EditText input_p1 = (EditText) viewInflated.findViewById(R.id.input_phone1);
                final EditText input_p2 = (EditText) viewInflated.findViewById(R.id.input_phone2);
                final EditText input_address = (EditText) viewInflated.findViewById(R.id.input_address);
                input_email.setText(email_json);
                input_p1.setText(phone1_json);
                input_p2.setText(phone2_json);
                input_address.setText(address_json);

                builder.setView(viewInflated);


                builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        username_edit=username_json;
                        email_edit=input_email.getText().toString();
                        phone1_edit=input_p1.getText().toString();
                        phone2_edit=input_p2.getText().toString();
                        address_edit=input_address.getText().toString();
                       savingData(email_edit,phone1_edit,phone2_edit,address_edit,username_edit);

                    }
                });
                builder.setNegativeButton("Batalkan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });


                alert = builder.create();
                alert.show();
                alert.setOnKeyListener(new AlertDialog.OnKeyListener(){
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {

                            alert.dismiss();
                        }
                        return true;
                    }
                });


            }
        });
        btn_edit.setOnTouchListener(new View.OnTouchListener() {

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

    }

    @Override
    protected void onResume() {
        super.onResume();


        loadPP();
        // register connection status listener
        AppController.getInstance().setConnectivityListener(this);
    }

    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);

    }

    private void checkConnection() {
        isConnected = ConnectivityReceiver.isConnected();
        conType = ConnectivityReceiver.getConnectivityType(getApplicationContext());
        conType2 = ConnectivityReceiver.getNetworkClass(getApplicationContext());
        showSnack(isConnected, conType, conType2);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected, String conType, String conType2) {
        showSnack(isConnected, conType, conType2);
    }


    private void showSnack(boolean isConnected, String conType, String conType2) {
        String message;
        int color;
        if (isConnected) {

            if (conType == "Using Wifi") {
                message = conType + "\nConnected to Internet";
                color = getResources().getColor(R.color.ijo_true);
            } else {

                message = "Using " + conType2 + " Network\nConnected to Internet";
                color = getResources().getColor(R.color.ijo_true);
            }


        } else {
            message = conType + "\nCheck your settings again";
            color = Color.RED;


        }

        Snackbar snackbar3 = Snackbar.make(findViewById(R.id.imageView), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar3.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.editText_bg));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar3.show();
    }

    private void buildAlertMessageNoGps() {


        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        View viewInflated = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.message_dialog,
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

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        View viewInflated = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.message_dialog,
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

    private void loadPP() {

        File imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "img");
        imgDir.mkdirs();

        File picUser = new File(imgDir +"/"+ str_img);

        Picasso.with(getApplicationContext())
                .load(picUser)
                .error(R.drawable.avatar_default_round)
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .transform(new jp.wasabeef.picasso.transformations.CropSquareTransformation())
                .into(imgProfile);

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

            Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
            return null;
        }
    }

    class loadDataProfile extends AsyncTask<Void, Void, String> {

        String value;
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
                    phone1_json = c.getString("phone1");
                    phone2_json = c.getString("phone2");
                    address_json = c.getString("address");

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

            return value;
        }


        protected void onPostExecute(String value) {
            super.onPostExecute(value);
            //if(phone1_json.isEmpty()){
                //phone1_json="-";
            //}
            //if(phone2_json.isEmpty()){
            //    phone2_json="-";
            //}
            //if(address_json.isEmpty()){
                //address_json="-";
            //}

            text_name.setText(name_json);
            text_username.setText(username_json);
            text_email.setText(email_json);
            text_phone1.setText(phone1_json);
            text_phone2.setText(phone2_json);
            text_adress.setText(address_json);

        }


    }

    private void savingData( final String email_post,final String p1_post,
                              final String p2_post, final String address_post, final String username_post) {

        String tag_string_req = "req_register";

        pDialog.setMessage("Saving...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                ipport+URL_SAVE_DATA, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        String errorMsg = jObj.getString("successMsg");
                        alert("i-MAV",errorMsg);
                        userDataRequest(URL_2);


                    } else {

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
                //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email_post);
                params.put("phone1", p1_post);
                params.put("phone2",p2_post);
                params.put("address", address_post);
                params.put("username", username_post);
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

    private void userDataRequest(String URL_DATA) {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_DATA, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    username_edit = response.getString("name");

                    try {
                        String state = Environment.getExternalStorageState();

                        File picDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
                        picDir.mkdir();

                        FileWriter file = new FileWriter(picDir + File.separator  + "userdetail");
                        file.write(response.toString());
                        file.flush();
                        file.close();

                        new loadDataProfile().execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                } catch (JSONException e) {
                    e.printStackTrace();
                    //Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                //Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

}
