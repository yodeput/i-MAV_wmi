package site.visit.wmi.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;

import site.visit.wmi.R;
import site.visit.wmi.app.AppController;
import site.visit.wmi.helper.SQLiteHandler;
import site.visit.wmi.visit.outstandingVisit;

import static site.visit.wmi.app.AppConfig.URL_DASHBOARD;
import static site.visit.wmi.app.AppConfig.URL_MY_VISIT;

/**
 * Created by NOC WMI on 17/11/2016.
 */

public class BackgroundCheckVisit extends Service {
    Bundle b;
    NotifyServiceReceiver notifyServiceReceiver;
    private static final int MY_NOTIFICATION_ID=1;
    private NotificationManager notificationManager;
    private Notification myNotification;
    private Runnable runnableCode;
    private SQLiteHandler db;


    private static String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_MYVISIT = "myvisit";
    private static final String TAG_OUTSTANDING = "outstanding";
    private static final String TAG_ONGOING = "ongoing";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VISITCOUNT = "visitcount";

    private String URL_FINAL;
    private String statusInternet;
    private String str_myvisit;
    private String str_myoutstanding;
    private String str_myongoing;
    private String version;
    private String ip_pref;
    private String port_pref;
    private String ipport;
    private String namedb, emaildb;
    private SharedPreferences setting;
    SharedPreferences.Editor editor;


    @Override
    public void onCreate() {



        super.onCreate();
    }

    String myvisitstr;
    String outstandingstr;
    String ongoing_str;
    private void dashDatav2() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_FINAL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "BG SERVICE" +response.toString());

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

    private void dataHandler(){

        final Handler handler = new Handler();

        runnableCode = new Runnable() {
            @Override
            public void run() {

               dashDatav2();

                handler.postDelayed(runnableCode, 1000);

            }

        };

        handler.post(runnableCode);

    }

    public void showNotification(String title, String message, String sound){

        // define sound URI, the sound to be played when there's a notification
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        HashMap<String, String> user = db.getUserDetails();

        String pic_checkin = namedb;
        String URL = URL_MY_VISIT;
        Intent intent = new Intent(BackgroundCheckVisit.this, outstandingVisit.class);
        intent.putExtra("pic", pic_checkin);
        intent.putExtra("URL", URL);
        intent.putExtra("title", "OUTSTANDING DATA");
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)

                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.header_logo)
                .setContentIntent(pIntent)
                .setSound(Uri.parse(sound))
                .addAction(0, "View", pIntent)
                .setVibrate(new long[] {1, 1, 1})
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below
        // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mNotification);
    }

    private void notifhandler() {

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
                    time = 30000;

                } else {
                    if (int_os > 0) {
                        String Sound = "android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.twirl;
                        showNotification(int_os + " Visit Oustanding", "Segera kirimkan dokumentasi!", Sound);
                        time = 5 * 60 * 1000;
                    }
                }

                // Repeat this the same runnable code block again another 2 seconds
                handler.postDelayed(runnableCode, time);


            }

        };

        handler.post(runnableCode);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        namedb = user.get("name");
        final String leveldb = user.get("level");

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        str_myvisit = setting.getString("my_visit", "");
        str_myoutstanding = setting.getString("my_outstanding", "");
        str_myongoing = setting.getString("my_ongoing","");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";
        if (str_myongoing==""){
            editor = setting.edit();
            editor.putString("my_ongoing", "0");
            editor.commit();
            str_myongoing = setting.getString("my_ongoing","");
        }

        String pic_name = namedb;
        try {
            String tt = URLEncoder.encode(pic_name, "UTF-8");
            URL_FINAL = ipport + URL_DASHBOARD + tt;
            //Log.e("a", URL_FINAL);
        } catch (Exception e) {

        }

        dataHandler();

        notifhandler();


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
// TODO Auto-generated method stub
        this.unregisterReceiver(notifyServiceReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
// TODO Auto-generated method stub
        return null;
    }

    public class NotifyServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            }
        }
}





