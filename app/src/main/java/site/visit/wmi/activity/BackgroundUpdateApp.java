package site.visit.wmi.activity;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import java.io.File;

/**
 * Created by NOC WMI on 24/11/2016.
 */

public class BackgroundUpdateApp extends IntentService {

    Handler mHandler;
    public BackgroundUpdateApp() {
        super("BackgroundUpdateApp"); //untuk debug saja
        mHandler = new Handler();
    }

    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String updatelinkstr = extras.getString("link");
        String versionstr = extras.getString("version");

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
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.setDataAndType(uri,manager.getMimeTypeForDownloadedFile(downloadId));
                startActivity(install);
                unregisterReceiver(this);

                getApplicationContext().sendBroadcast(new Intent("xyz"));
            }
        };
        //register receiver for when .apk download is compete

        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


    }



}
