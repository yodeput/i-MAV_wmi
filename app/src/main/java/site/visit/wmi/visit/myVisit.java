package site.visit.wmi.visit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import site.visit.wmi.R;
import site.visit.wmi.activity.MainActivity;
import site.visit.wmi.helper.SQLiteHandler;
import site.visit.wmi.helper.SessionManager;


public class myVisit extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> visitList;

    // url to get all products list
    private static String URL_FINAL;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VISIT = "visit";
    private static final String TAG_SITEID = "siteid";
    private static final String TAG_HOSTCODE = "hostcode";
    private static final String TAG_SITENAME = "sitename";
    private static final String TAG_CAT = "category";
    private static final String TAG_PIC = "pic";
    private static final String TAG_CLUSTER= "cluster";
    private static final String TAG_MONITORING = "monitoring";
    private static final String TAG_CUSTOMER = "customer";
    private static final String TAG_STARTTIME = "start_time";
    private static final String TAG_REMARK = "remark";

    // products JSONArray
    JSONArray products = null;

    private ImageView close;

    private SQLiteHandler db;
    private SessionManager session;
    private CardView sv;

    private String ip_pref;
    private String port_pref;
    private String ipport;
    private SharedPreferences setting;
    private TextView title_txt;
    private TextView onGoing;

    private String url;
    private String pic;
    private String title;

    private String packageName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_all);

        packageName = this.getPackageName();
        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server","");
        port_pref = setting.getString("port_server","");
        ipport =  "http://"+ip_pref+":"+port_pref+"/";

        Intent i= getIntent();
        url = i.getStringExtra("URL");
        pic = i.getStringExtra("pic");
        title = i.getStringExtra("title");
        sv = (CardView) findViewById(R.id.card_view);

        close = (ImageView) findViewById(R.id.close_button1);
        title_txt = (TextView) findViewById(R.id.judul_visit_txt);

        title_txt.setText(title);
        visitList = new ArrayList<HashMap<String, String>>();

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        String namedb = user.get("name");
        String emaildb = user.get("email");

        try {
            String tt = URLEncoder.encode(pic, "UTF-8");
            URL_FINAL = ipport+url+tt;
            Log.e("a", URL_FINAL);
        }catch(Exception e){
            return;
        }

        ListView lv = getListView();

        LayoutInflater inflater = this.getLayoutInflater();
        View aView = inflater.inflate(R.layout.list_visit_all, null);
        onGoing = (TextView) findViewById(R.id.txt_ongoing) ;

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String site_name= ((TextView) view.findViewById(R.id.txt_sitename)).getText().toString();
                String site_id = ((TextView) view.findViewById(R.id.txt_siteid)).getText().toString();
                String host_code = ((TextView) view.findViewById(R.id.txt_hostcode)).getText().toString();
                String cluster = ((TextView) view.findViewById(R.id.txt_cluster)).getText().toString();
                String monitoring = ((TextView) view.findViewById(R.id.txt_monitoring)).getText().toString();
                String customer = ((TextView) view.findViewById(R.id.txt_customer)).getText().toString();
                String remark = ((TextView) view.findViewById(R.id.txt_remark)).getText().toString();
                String start_time = ((TextView) view.findViewById(R.id.txt_starttime)).getText().toString();
                String category = ((TextView) view.findViewById(R.id.txt_kategori)).getText().toString();
                String pic = ((TextView) view.findViewById(R.id.txt_pic)).getText().toString();
                if (remark=="false") {

                    Intent in = new Intent(getApplicationContext(),VisitContinue.class);
                    in.putExtra(TAG_SITEID,site_id);
                    in.putExtra(TAG_HOSTCODE,host_code);
                    in.putExtra(TAG_SITENAME,site_name);
                    in.putExtra(TAG_CLUSTER,cluster);
                    in.putExtra(TAG_MONITORING,monitoring);
                    in.putExtra(TAG_CUSTOMER,customer);
                    in.putExtra(TAG_STARTTIME,start_time);
                    in.putExtra(TAG_CAT,category);
                    in.putExtra(TAG_PIC, pic);
                    startActivityForResult(in, 100);
                    finish();
                    overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);

                } else {
                    //Intent in = new Intent(getApplicationContext(),VisitAdd.class);
                    //in.putExtra("sid",siteid);
                    //startActivityForResult(in, 100);
                    // getting values from selected ListItem
                    Toast.makeText(getApplicationContext(), "SITE ID: " + site_id + "\nSITE NAME:"+ site_name, Toast.LENGTH_LONG).show();


                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);
            }

        });

        new LoadAllVisit().execute();
    }

    public String getData() {

            try {
                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db"+ File.separator +  "myVisit");
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
    public String getDataSite() {
        try {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db"+ File.separator + "myVisit");

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

    class LoadAllVisit extends AsyncTask<String, String, JSONObject> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(myVisit.this);
            pDialog.setMessage("Loading Data..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        JSONObject d;
        protected JSONObject doInBackground(String... args) {

            try {

                JSONObject json = new JSONObject(getData());
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    JSONArray visits = json.getJSONArray(TAG_VISIT);

                    // looping through All Products
                    for (int i = 0; i < visits.length(); i++) {
                        JSONObject c = visits.getJSONObject(i);

                        // Storing each json item in variable
                        String siteid = c.getString(TAG_SITEID);
                        String hostcode = c.getString(TAG_HOSTCODE);
                        String sitename = c.getString(TAG_SITENAME);
                        String kategori= c.getString(TAG_CAT);
                        String pic = c.getString(TAG_PIC);
                        String cluster = c.getString(TAG_CLUSTER);
                        String monitoring = c.getString(TAG_MONITORING);
                        String customer = c.getString(TAG_CUSTOMER);
                        String start_time = c.getString(TAG_STARTTIME);
                        String remark = c.getString(TAG_REMARK);
                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();
                        String Ongoing = "";

                        if (remark=="false") {
                           Ongoing = "ON GOING";


                        } else {

                            Ongoing = "";
                        }

                        // adding each child node to HashMap key => value
                        map.put(TAG_SITEID, siteid);
                        map.put(TAG_HOSTCODE, hostcode);
                        map.put(TAG_SITENAME, sitename);
                        map.put(TAG_CAT, kategori);
                        map.put(TAG_PIC, pic);
                        map.put(TAG_CLUSTER, cluster);
                        map.put(TAG_MONITORING, monitoring);
                        map.put(TAG_CUSTOMER, customer);
                        map.put(TAG_STARTTIME, start_time);
                        map.put(TAG_REMARK, remark);
                        map.put("OG",Ongoing);
                        // adding HashList to ArrayList
                        visitList.add(map);

                    }
                } else {
                    String errorMsg = json.getString("error_msg");

                    AlertDialog.Builder BackAlertDialog = new AlertDialog.Builder(myVisit.this);

                    BackAlertDialog.setTitle("WMI");

                    BackAlertDialog.setMessage(errorMsg);

                    BackAlertDialog.setNegativeButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {


                                    dialog.cancel();
                                }
                            });

                    BackAlertDialog.show();


                }
            } catch (JSONException e) {
                e.printStackTrace();
                alert("Error!", "Koneksi Internet atau Server bermasalah segera hubungi NOC WMI");
            }

            return d;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(JSONObject json) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();


            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            myVisit.this, visitList,
                            R.layout.list_visit_all, new String[]{TAG_SITEID,
                            TAG_HOSTCODE, TAG_SITENAME,TAG_PIC, TAG_CAT, TAG_STARTTIME, TAG_REMARK, TAG_CLUSTER, TAG_CUSTOMER, TAG_MONITORING, "OG"},
                            new int[]{R.id.txt_siteid,R.id.txt_hostcode, R.id.txt_sitename,
                            R.id.txt_pic, R.id.txt_kategori,R.id.txt_starttime,R.id.txt_remark,R.id.txt_cluster,R.id.txt_customer,R.id.txt_monitoring, R.id.txt_ongoing});


                    setListAdapter(adapter);



                }
            });

        }

    }
    private void alert(String title, String msg){

        AlertDialog.Builder builder = new AlertDialog.Builder(myVisit.this);
        View viewInflated = LayoutInflater.from(myVisit.this).inflate(R.layout.message_dialog,
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
