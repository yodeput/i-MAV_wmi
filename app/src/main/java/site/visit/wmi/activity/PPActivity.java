package site.visit.wmi.activity;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import site.visit.wmi.R;
import site.visit.wmi.app.CropingOption;
import site.visit.wmi.app.CropingOptionAdapter;
import site.visit.wmi.app.RequestHandler;

import static site.visit.wmi.app.AppConfig.URL_USER_DATA;
import static site.visit.wmi.app.AppConfig.URL_USER_IMG;


public class PPActivity extends AppCompatActivity {

    private final static int REQUEST_PERMISSION_REQ_CODE = 34;
    private static final int CAMERA_CODE = 101, GALLERY_CODE = 201, CROPING_CODE = 301;

    public static final String UPLOAD_URL = "http://121.52.87.128:8888/imav/apiv2/app/picture_upload.php";
    public static final String UPLOAD_KEY = "file_name";
    private String packageName;
    private AlertDialog dialog, alert;
    private ImageView btn_save,btn_back;
    private ImageView imageView;
    private Uri mImageCaptureUri;
    private File outPutFile = null;
    private Bitmap photo;
    private String ext;
    private RelativeLayout atas;

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

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_selector);
        packageName =  this.getPackageName();
        File imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "img");
        imgDir.mkdirs();
        outPutFile = new File(imgDir+"/"+ "temp.jpg");

        atas = (RelativeLayout) findViewById(R.id.rl_atas);
        btn_save = (ImageView)  findViewById(R.id.save_button);
        btn_back = (ImageView)  findViewById(R.id.back_button);
        btn_back.setVisibility(View.GONE);
        btn_save.setVisibility(View.GONE);
        imageView = (ImageView) findViewById(R.id.img_photo);

        setting = getSharedPreferences("WSV_SETTINGS", 0);
        ip_pref = setting.getString("ip_server", "");
        port_pref = setting.getString("port_server", "");
        str_username = setting.getString("username", "");
        str_name = setting.getString("name", "");
        str_email = setting.getString("email", "");
        str_img = setting.getString("img", "");
        str_myvisit = setting.getString("my_visit","");
        str_myoutstanding = setting.getString("my_outstanding","");
        str_myongoing = setting.getString("my_ongoing", "");
        str_ttopen = setting.getString("my_tt","");
        str_autoupdate = setting.getString("auto_update","");
        ipport = "http://" + ip_pref + ":" + port_pref + "/";

        try {
            String pic = URLEncoder.encode(str_name, "UTF-8");
            URL_2 = ipport + URL_USER_DATA + pic;
            URL_3 = ipport + URL_USER_IMG+str_img;
            //Log.e(TAG,URL_1);
            //Log.e(TAG,URL_2);

        } catch (Exception e) {
            return;
        }

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        loadPPPicture();

        selectImageOption();

        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
                //Snackbar.make(v, "Edit Button Clicked", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            }
        });

        btn_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);
                //Snackbar.make(v, "Edit Button Clicked", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            }
        });

        btn_save.setOnTouchListener(new View.OnTouchListener() {

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
        btn_back.setOnTouchListener(new View.OnTouchListener() {

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

    private void loadPPPicture() {

        File imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "img");
        imgDir.mkdirs();

        File picUser = new File(imgDir +"/"+ str_img);
        Picasso.with(getApplicationContext())
                .load(picUser)
                .error(R.drawable.avatar_default_round)
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .transform(new jp.wasabeef.picasso.transformations.CropSquareTransformation())
                .into(imageView);

    }

    private void selectImageOption() {
        final CharSequence[] items = { "Camera", "Gallery", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(PPActivity.this);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Camera")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory(), "temp1.jpg");
                    mImageCaptureUri = Uri.fromFile(f);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    startActivityForResult(intent, CAMERA_CODE);
                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                    btn_back.setVisibility(View.VISIBLE);


                } else if (items[item].equals("Gallery")) {

                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, GALLERY_CODE);
                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                    btn_back.setVisibility(View.VISIBLE);

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                    btn_back.setVisibility(View.VISIBLE);
                }
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
                    btn_back.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

            if (ContextCompat.checkSelfPermission(PPActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_REQ_CODE);
                return;
            }


    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {

            mImageCaptureUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(mImageCaptureUri , filePathColumn, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                ext = filePath.substring(filePath.lastIndexOf(".") + 1);
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            }
            cursor.close();
            System.out.println("Gallery Image URI : "+mImageCaptureUri);
            CropingIMG();

        } else if (requestCode == CAMERA_CODE && resultCode == Activity.RESULT_OK) {

            ext="jpg";
            System.out.println("Camera Image URI : "+mImageCaptureUri);
            CropingIMG();

        } else if (requestCode == CROPING_CODE) {


            try {
                if(outPutFile.exists()){
                    photo = decodeFile(outPutFile);
                    imageView.setImageBitmap(photo);
                    btn_back.setVisibility(View.VISIBLE);
                    btn_save.setVisibility(View.VISIBLE);
                }
                else {
                    finish();
                    overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void uploadImage(){
        class UploadImage extends AsyncTask<Bitmap,Void,String> {

            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog.setMessage("Uploading..");
                showDialog();



            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                File imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "img");
                imgDir.mkdirs();

                File file = new File(imgDir +"/"+ str_img);
                file.delete();

                if(outPutFile.exists()){
                    outPutFile.renameTo(file);
                }

                hideDialog();
                if(s.contains("false")) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(PPActivity.this);
                    View viewInflated = LayoutInflater.from(PPActivity.this).inflate(R.layout.message_dialog,
                            (ViewGroup) findViewById(android.R.id.content), false);
                    final TextView titletxt = (TextView) viewInflated.findViewById(R.id.title_txt);
                    final TextView messagetxt = (TextView) viewInflated.findViewById(R.id.message_txt);
                    titletxt.setText("i-MAV");
                    messagetxt.setText("Berhasil memperbaharui Foto");
                    builder.setView(viewInflated);


                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            finish();
                            overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);
                            dialog.dismiss();
                        }
                    });

                    Dialog dd = builder.show();


                } else {

                    alert("i-MAV","Gagal, silahkan coba kembali");

                }


            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String user = str_username;
                String uploadImage = getStringImage(bitmap);

                HashMap<String,String> data = new HashMap<String, String>();

                data.put(UPLOAD_KEY, uploadImage);
                data.put("u", str_username);
                data.put("e",ext);
                String result = rh.sendPostRequest(UPLOAD_URL,data);


                return result;
            }
        }

        UploadImage ui = new UploadImage();
        ui.execute(photo);
    }

    private void alert(String title, String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(PPActivity.this);
        View viewInflated = LayoutInflater.from(PPActivity.this).inflate(R.layout.message_dialog,
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


    private void CropingIMG() {

        final ArrayList<CropingOption> cropOptions = new ArrayList<CropingOption>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Cann't find image croping app", Toast.LENGTH_SHORT).show();
            return;
        } else {
            intent.setData(mImageCaptureUri);
            intent.putExtra("outputX", 512);
            intent.putExtra("outputY", 512);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);

            //TODO: don't use return-data tag because it's not return large image data and crash not given any message
            //intent.putExtra("return-data", true);

            //Create output file here
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outPutFile));

            if (size == 1) {
                Intent i   = new Intent(intent);
                ResolveInfo res = (ResolveInfo) list.get(0);
                i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROPING_CODE);

            } else {
                for (ResolveInfo res : list) {
                    final CropingOption co = new CropingOption();

                    co.title  = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon  = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent= new Intent(intent);
                    co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    cropOptions.add(co);
                }

                CropingOptionAdapter adapter = new CropingOptionAdapter(getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Croping App");
                builder.setCancelable(false);

                builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int item ) {
                        startActivityForResult( cropOptions.get(item).appIntent, CROPING_CODE);
                    }
                });

                builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel( DialogInterface dialog ) {

                        if (mImageCaptureUri != null ) {
                            getContentResolver().delete(mImageCaptureUri, null, null );
                            mImageCaptureUri = null;
                        }
                    }
                } );

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 512;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.anim_slide_out_up, R.anim.anim_slide_in_up);

    }
}


