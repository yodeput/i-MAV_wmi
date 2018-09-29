package site.visit.wmi.visit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import site.visit.wmi.R;

import static site.visit.wmi.app.AppConfig.URL_SITE_REPORT;

public class SiteReport extends ListActivity {

    private ProgressDialog pDialog;

    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> siteList;

    private EditText cari;
    private ListAdapter adapter;
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_SITE = "site";
    private static final String TAG_SN = "site_name";
    private static final String TAG_TR = "dtime_received";
    private static final String TAG_T = "dtime";
    private static final String TAG_ALARM = "alarm";
    private static final String TAG_DESC = "descript";
    private static final String TAG_VBUS = "vbus";
    private static final String TAG_IBAT = "ibatt";
    private static final String TAG_ILOAD = "iload";
    private static final String TAG_K1 = "kwh1";
    private static final String TAG_K2 = "kwh2";
    private static String url= "";
    private ImageView close;

    // products JSONArray
    JSONArray sitelist= null;

    private String ip_pref;
    private String port_pref;
    private String ipport;
    private SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_report);

        close = (ImageView) findViewById(R.id.close_button1);
        siteList = new ArrayList<HashMap<String, String>>();

        ListView lv = getListView();

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server","");
        port_pref = setting.getString("port_server","");
        ipport =  "http://"+ip_pref+":"+port_pref+"/";

        Intent i= getIntent();
        String site_id = i.getStringExtra("site_id");
        String host_code = i.getStringExtra("host_code");

        try {
            String tt = URLEncoder.encode(host_code, "UTF-8");
            url = URL_SITE_REPORT+ tt;
            Log.e("a", url);
            new LoadAllSite().execute();
            siteList.clear();
        } catch (Exception e) {
            return;
        }


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }

        });

        new LoadAllSite().execute();

    }




    class LoadAllSite extends AsyncTask<String, String, JSONObject> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //pDialog = new ProgressDialog(SiteList.this);
            //pDialog.setMessage("Searching..");
            //pDialog.setIndeterminate(false);
            //pDialog.setCancelable(false);
            //pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected JSONObject doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONParser jParser = new JSONParser();


            JSONObject json = jParser.getJSONFromUrl(ipport+url);


            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    JSONArray sitelist = json.getJSONArray(TAG_SITE);

                    // looping through All Products
                    for (int i = 0; i < sitelist.length(); i++) {
                        JSONObject c = sitelist.getJSONObject(i);

                        // Storing each json item in variable
                        String site_name = c.getString(TAG_SN);
                        String dtime_received = c.getString(TAG_TR);
                        String dtime = c.getString(TAG_T);
                        String alarm = c.getString(TAG_ALARM);
                        String descript = c.getString(TAG_DESC);
                        String vbus = c.getString(TAG_VBUS);
                        String ibatt = c.getString(TAG_IBAT);
                        String iload = c.getString(TAG_ILOAD);
                        String kwh1 = c.getString(TAG_K1);
                        String kwh2 = c.getString(TAG_K2);


                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_SN, site_name);
                        map.put(TAG_TR, dtime_received);
                        map.put(TAG_T, dtime);
                        map.put(TAG_ALARM, alarm);
                        map.put(TAG_DESC, descript);
                        map.put(TAG_VBUS, vbus);
                        map.put(TAG_IBAT, ibatt);
                        map.put(TAG_ILOAD, iload);
                        map.put(TAG_K1, kwh1);
                        map.put(TAG_K2, kwh2);

                        // adding HashList to ArrayList

                        siteList.add(map);
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity
                    //Intent i = new Intent(getApplicationContext(),
                    //NewProductActivity.class);
                    // Closing all previous activities
                    //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //startActivity(i);

                    String errorMsg = json.getString("message");
                    alert("Error!",errorMsg);

                }
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
            //pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    adapter = new SimpleAdapter(
                            SiteReport.this, siteList,
                            R.layout.list_report, new String[]{TAG_SN, TAG_TR, TAG_T, TAG_ALARM, TAG_DESC,
                            TAG_VBUS},
                            new int[]{R.id.site_id_txt,R.id.host_code_txt, R.id.site_name_txt,
                                    R.id.cluster_txt, R.id.monitoring_txt, R.id.customer_txt});
                    setListAdapter(adapter);
                }
            });

        }


    }



    private void alert(String title, String msg){

        AlertDialog.Builder builder = new AlertDialog.Builder(SiteReport.this);
        View viewInflated = LayoutInflater.from(SiteReport.this).inflate(R.layout.message_dialog,
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
