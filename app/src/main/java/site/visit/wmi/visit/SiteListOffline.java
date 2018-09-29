package site.visit.wmi.visit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import site.visit.wmi.R;
import site.visit.wmi.activity.MainActivity;

import static site.visit.wmi.app.AppConfig.URL_SITE_LIST;

/**
 * Created by NOC WMI on 27/09/2016.
 */

public class SiteListOffline extends ListActivity {

    private ProgressDialog pDialog;
    private ProgressDialog pDialog2;
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> siteList;

    private EditText cari;
    private ListAdapter adapter;
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_SITELIST= "site";
    private static final String TAG_SITEID = "site_id";
    private static final String TAG_HOSTCODE= "host_code";
    private static final String TAG_SITENAME = "site_name";
    private static final String TAG_CLUSTER= "cluster";
    private static final String TAG_MONITORING = "monitoring";
    private static final String TAG_CUSTOMER= "customer";
    private static String url= "";
    private ImageView close;
    // products JSONArray
    JSONArray sitelist= null;
    private String sch;

    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_list);

        packageName = this.getPackageName();
        siteList = new ArrayList<HashMap<String, String>>();

        ListView lv = getListView();

        cari = (EditText) findViewById(R.id.editSearch);
        //url = url_sitelist;

        new LoadAllSite().execute();

        close = (ImageView) findViewById(R.id.close_button1);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                finish();
                overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);


            }

        });

        sch="";
        cari.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {



                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    sch = cari.getText().toString();
                    pDialog2 = new ProgressDialog(SiteListOffline.this);
                    pDialog2.setMessage("Loading...");
                    pDialog2.setIndeterminate(false);
                    pDialog2.setCancelable(false);
                    new CountDownTimer(2000, 2000) {

                        public void onTick(long millisUntilFinished) {
                            pDialog2.show();
                            siteList.clear();
                        }

                        public void onFinish() {

                            pDialog2.dismiss();
                            new LoadAllSite().execute();
                        }
                    }.start();


                }
                return false;
            }
        });


        Button cari_but = (Button) findViewById(R.id.btnSearch);
        cari_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sch = cari.getText().toString();

                if (!sch.isEmpty()) {

                    pDialog2 = new ProgressDialog(SiteListOffline.this);
                    pDialog2.setMessage("Loading...");
                    pDialog2.setIndeterminate(false);
                    pDialog2.setCancelable(false);
                    new CountDownTimer(2000, 2000) {

                        public void onTick(long millisUntilFinished) {
                            pDialog2.show();
                            siteList.clear();
                        }

                        public void onFinish() {

                            pDialog2.dismiss();
                            new LoadAllSite().execute();
                        }

                    }.start();



                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(SiteListOffline.this);
                    View viewInflated = LayoutInflater.from(SiteListOffline.this).inflate(R.layout.message_dialog,
                            (ViewGroup) findViewById(android.R.id.content), false);
                    final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                    final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                    titletxt.setText("i-MAV");
                    messagetxt.setText("Masukan nama site yang dicari");
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
        });

        Intent i = getIntent();
        final String mode_checkin = i.getStringExtra("mode");
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                String site_id = ((TextView) view.findViewById(R.id.site_id_txt)).getText().toString();
                String host_code = ((TextView) view.findViewById(R.id.host_code_txt)).getText().toString();
                String site_name = ((TextView) view.findViewById(R.id.site_name_txt)).getText().toString();
                String cluster = ((TextView) view.findViewById(R.id.cluster_txt)).getText().toString();
                String monitoring = ((TextView) view.findViewById(R.id.monitoring_txt)).getText().toString();
                String customer = ((TextView) view.findViewById(R.id.customer_txt)).getText().toString();

                Intent in = new Intent(getApplicationContext(),VisitAdd.class);
                in.putExtra(TAG_SITEID,site_id);
                in.putExtra(TAG_HOSTCODE,host_code);
                in.putExtra(TAG_SITENAME,site_name);
                in.putExtra(TAG_CLUSTER,cluster);
                in.putExtra(TAG_MONITORING,monitoring);
                in.putExtra(TAG_CUSTOMER,customer);
                in.putExtra("mode",mode_checkin);
                startActivityForResult(in, 100);
                SiteListOffline.this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                // getting values from selected ListItem

            }
        });
    }





    public String getDataSite() {
        try {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db"+ File.separator + "sitelist");
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

    class LoadAllSite extends AsyncTask<String, String, JSONObject> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SiteListOffline.this);
            pDialog.setMessage("Memuat Site..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */ JSONObject d;
        protected JSONObject doInBackground(String... args) {

            try {


                JSONObject json = new JSONObject(getDataSite());
                // Checking for SUCCESS TAG
                                // products found
                    // Getting Array of Products
                    JSONArray sitelist = json.getJSONArray(TAG_SITELIST);
                    siteList.clear();

                    // looping through All Products
                    for (int i = 0; i < sitelist.length(); i++) {
                        JSONObject c = sitelist.getJSONObject(i);


                        // Storing each json item in variable
                        String siteid = c.getString(TAG_SITEID);
                        String hostcode = c.getString(TAG_HOSTCODE);
                        String sitename = c.getString(TAG_SITENAME);
                        String cluster = c.getString(TAG_CLUSTER);
                        String monitoring = c.getString(TAG_MONITORING);
                        String customer = c.getString(TAG_CUSTOMER);

                        String search = sch.toLowerCase();
                        HashMap<String, String> map = new HashMap<String, String>();

                        if (sitename.toLowerCase().contains(search)){

                            map.put(TAG_SITEID, siteid);
                            map.put(TAG_HOSTCODE,hostcode);
                            map.put(TAG_SITENAME, sitename);
                            map.put(TAG_CLUSTER,cluster);
                            map.put(TAG_MONITORING,monitoring);
                            map.put(TAG_CUSTOMER,customer);

                            siteList.add(map);

                        } else if (siteid.toLowerCase().contains(search)){

                            map.put(TAG_SITEID, siteid);
                            map.put(TAG_HOSTCODE,hostcode);
                            map.put(TAG_SITENAME, sitename);
                            map.put(TAG_CLUSTER,cluster);
                            map.put(TAG_MONITORING,monitoring);
                            map.put(TAG_CUSTOMER,customer);

                            siteList.add(map);

                        }

                   }
                return d;


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(JSONObject json) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    adapter = new SimpleAdapter(
                            SiteListOffline.this, siteList,
                            R.layout.list_site_list, new String[]{TAG_SITEID,
                            TAG_HOSTCODE,TAG_SITENAME,TAG_CLUSTER, TAG_MONITORING, TAG_CUSTOMER},
                            new int[]{R.id.site_id_txt,R.id.host_code_txt, R.id.site_name_txt,
                                    R.id.cluster_txt, R.id.monitoring_txt, R.id.customer_txt});
                    setListAdapter(adapter);


                }
            });

        }


    }

    public void clearList(){




    }

    private void alert(String title, String msg){

        AlertDialog.Builder builder = new AlertDialog.Builder(SiteListOffline.this);
        View viewInflated = LayoutInflater.from(SiteListOffline.this).inflate(R.layout.message_dialog,
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

    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);


    }


}
