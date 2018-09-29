package site.visit.wmi.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
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
import java.util.HashMap;

import site.visit.wmi.R;
import site.visit.wmi.app.AppController;
import site.visit.wmi.helper.SQLiteHandler;

import static site.visit.wmi.app.AppConfig.URL_MY_VISIT;
import static site.visit.wmi.app.AppConfig.URL_OUTSTANDING;
import static site.visit.wmi.app.AppConfig.URL_UPDATE_SITE;
import static site.visit.wmi.app.AppConfig.URL_USER_DATA;
import static site.visit.wmi.app.AppConfig.URL_USER_DETAIL;
import static site.visit.wmi.app.AppConfig.URL_USER_IMG;

public class SplashscreenActivity extends AppCompatActivity {
    private static String TAG = SplashscreenActivity.class.getSimpleName();

    private static final String TAG_MYVISIT = "myvisit";
    private static final String TAG_OUTSTANDING = "outstanding";
    private static final String TAG_ONGOING = "ongoing";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VISITCOUNT = "visitcount";
    private String URL_FINAL;
    private String URL_SITE;
    private String URL_MV;
    private String URL_OV;

    private SQLiteHandler db;
    protected boolean _active = true;
    protected int _splashTime = 7000;

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

    private String URL_1;
    private String URL_2;
    private String URL_3;
    private String URL_UD;


    private String data_username;
    private String data_name;
    private String data_email;
    private String data_img;

    private String myvisitstr;
    private String outstandingstr;
    private String ongoing_str;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        packageName = this.getPackageName();
        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        str_username = setting.getString("username", "");
        str_name = setting.getString("name", "");
        str_email = setting.getString("email", "");
        str_img = setting.getString("img", "");
        str_myvisit = setting.getString("my_visit", "");
        str_myoutstanding = setting.getString("my_outstanding", "");
        str_myongoing = setting.getString("my_ongoing", "");
        str_ttopen = setting.getString("my_tt", "");
        str_autoupdate = setting.getString("auto_update", "");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        final String namedb = user.get("name");
        final String usernamedb = user.get("username");
        final String level = user.get("level");
        final String imgdb =  usernamedb+".png";


        try {

            URL_2 = ipport + URL_USER_DATA + usernamedb;
            URL_3 = ipport + URL_USER_IMG + imgdb;
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Integer kode = pInfo.versionCode;
            URL_SITE = ipport + URL_UPDATE_SITE;
            URL_MV = ipport + URL_MY_VISIT + usernamedb;
            URL_OV = ipport + URL_OUTSTANDING + usernamedb;
            URL_UD = ipport + URL_USER_DETAIL + usernamedb;

        } catch (Exception e) {
            return;
        }



        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "img");
            imgDir.mkdirs();
            File file = new File(imgDir + "/" + str_img);
            if (file.exists()) {
                file.delete();
            }
        }

        userDataRequest(URL_2);
        userDetailRequest();


        final ImageView image = (ImageView)findViewById(R.id.splashIMG);
        final Animation fadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        final Animation fadeout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        image.startAnimation(fadein);

        //new loadDataProfile().execute();
        dashDatav2();
        updateSitelist();
        updateoutstandingVisit();
        updatemyVisit();

        Picasso.with(getApplicationContext())
                .load(URL_3)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(target);


        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;

                        }
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {

                    if (level.contains("Manager")) {

                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {

                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();

                    }


                }
            }
        };
        splashTread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _active = false;
        }
        return true;
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

    private void userDataRequest(String URL_DATA) {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_DATA, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, response.toString());

                try {

                    data_username = response.getString("username");
                    data_name = response.getString("name");
                    data_email = response.getString("email");
                    data_img = response.getString("file_name");
                    editor = setting.edit();
                    editor.putString("name", data_name);
                    editor.putString("username", data_username);
                    editor.putString("email", data_email);
                    editor.putString("img", data_img);
                    editor.commit();

                    try {

                        String state = Environment.getExternalStorageState();

                        File picDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
                        picDir.mkdir();

                        FileWriter file = new FileWriter(picDir + File.separator  + "userdata");
                        file.write(response.toString());
                        file.flush();
                        file.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {


            try {
                JSONObject c = new JSONObject(getDataUser());

                String last_update_json= c.getString("last_update");
                String level_json= c.getString("level");
                String name_json= c.getString("name");
                String username_json = c.getString("username");
                String email_json = c.getString("email");
                String phone1_json = c.getString("phone1");
                String phone2_json = c.getString("phone2");
                String address_json = c.getString("address");

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
            //if(phone1_json.isEmpty()){
            //phone1_json="-";
            //}
            //if(phone2_json.isEmpty()){
            //    phone2_json="-";
            //}
            //if(address_json.isEmpty()){
            //address_json="-";
            //}

        }


    }

    private void updateSitelist() {

        URL_2 = ipport+URL_UPDATE_SITE;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_2, null, new Response.Listener<JSONObject>() {

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
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();

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


                    setting = getSharedPreferences("WSV_SETTINGS", 0);
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putString("my_visit", myvisitstr);
                    editor.putString("my_outstanding", outstandingstr);
                    editor.putString("my_ongoing", ongoing_str);
                    editor.commit();


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


}
