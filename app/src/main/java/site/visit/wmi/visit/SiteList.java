package site.visit.wmi.visit;

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
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import site.visit.wmi.R;
import site.visit.wmi.activity.LoginActivity;
import site.visit.wmi.activity.MainActivity;
import site.visit.wmi.app.AppConfig;

import static site.visit.wmi.app.AppConfig.URL_SITE_LIST;


public class SiteList extends ListActivity {

    private ProgressDialog pDialog;

    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> siteList;

    private EditText cari;
    private  ListAdapter adapter;
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

    private String ip_pref;
    private String port_pref;
    private String ipport;
    private SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_list);

        close = (ImageView) findViewById(R.id.close_button1);
        siteList = new ArrayList<HashMap<String, String>>();

        ListView lv = getListView();
        cari = (EditText) findViewById(R.id.editSearch);
        url = URL_SITE_LIST;
        cari.requestFocus();
        cari.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(cari, InputMethodManager.SHOW_IMPLICIT);


        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server","");
        port_pref = setting.getString("port_server","");
        ipport =  "http://"+ip_pref+":"+port_pref+"/";



        cari.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    try {
                        String sch = cari.getText().toString();
                        String tt = URLEncoder.encode(sch, "UTF-8");
                        url = URL_SITE_LIST + tt;
                        Log.e("a", url);
                        new LoadAllSite().execute();
                        siteList.clear();
                    }catch(Exception e){

                    }

                }
            return false;
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SiteList.this, MainActivity.class);
                startActivity(intent);
                finish();

            }

        });

        Button cari_but = (Button) findViewById(R.id.btnSearch);
        cari_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sch = cari.getText().toString();
                if (!sch.isEmpty()) {
                    try {
                        String tt = URLEncoder.encode(sch, "UTF-8");
                        url = URL_SITE_LIST + tt;
                        Log.e("a", url);
                        new LoadAllSite().execute();
                        siteList.clear();
                    } catch (Exception e) {
                        return;
                    }
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(SiteList.this);
                    View viewInflated = LayoutInflater.from(SiteList.this).inflate(R.layout.message_dialog,
                            (ViewGroup) findViewById(android.R.id.content), false);
                    final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                    final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                    titletxt.setText("Error!");
                    messagetxt.setText("Masukan nama site yang ingin dicari.");
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
                startActivityForResult(in, 100);
                // getting values from selected ListItem

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
                    JSONArray sitelist = json.getJSONArray(TAG_SITELIST);

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


                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_SITEID, siteid);
                        map.put(TAG_HOSTCODE,hostcode);
                        map.put(TAG_SITENAME, sitename);
                        map.put(TAG_CLUSTER,cluster);
                        map.put(TAG_MONITORING,monitoring);
                        map.put(TAG_CUSTOMER,customer);

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


                }
            } catch (JSONException e) {
                e.printStackTrace();
                alert("Error!", "Koneksi Internet atau Server bermasalah segera hubungi NOC WMI");
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
                            SiteList.this, siteList,
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

        AlertDialog.Builder builder = new AlertDialog.Builder(SiteList.this);
        View viewInflated = LayoutInflater.from(SiteList.this).inflate(R.layout.message_dialog,
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

        Dialog dd =  builder.show();

    }

    public void onBackPressed() {

        Intent intent = new Intent(SiteList.this, MainActivity.class);
        startActivity(intent);
        finish();

    }
}
