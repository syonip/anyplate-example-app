package com.anyplate.example;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.anyplate.example.LicensePlateContent.loadSavedImages;

public class MainActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener{

    static final int REQUEST_CODE = 1;
    private static final String TAG = "AnyPlateExample";
    static int scanCounter = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Activity activity = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(getString(R.string.anyplate_run_intent));
                try {
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(R.string.download_message)
                            .setTitle(R.string.download_title);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String appPackageName = getString(R.string.anyplate_package_name);
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_store_url) + appPackageName)));
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (LicensePlateContent.ITEMS.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadSavedImages(getApplicationContext().getFilesDir());
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // This is where the AnyPlate result is received
        if (data == null) return;

        final Activity activity = this;

        if (requestCode == REQUEST_CODE) {
            // Checking if recognition was successful
            if (resultCode == RESULT_OK) {
                // Getting the recognized plate image uri
                final Uri uri = data.getData();
                if (uri == null) return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = null;
                        try {
                            InputStream inputStream =
                                    getContentResolver().openInputStream(uri);
                            bitmap = BitmapFactory.decodeStream(inputStream);
                        } catch (FileNotFoundException e) {
                            // Inform the user that things have gone horribly wrong
                            Log.e(TAG, e.toString());
                        }

                        // Getting the recognized plate number
                        String plateNumber = data.getStringExtra("PlateNumber");

                        // Getting the average recognition confidence
                        float confidence = data.getFloatExtra("Confidence", 0f);

                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        String fileName = ts+"_"+plateNumber+"_"+confidence+".jpg";
                        File dest = new File(activity.getFilesDir(), fileName);
                        scanCounter++;

                        saveBmp(bitmap, dest.getPath());

                        LicensePlate newLicensePlate = new LicensePlate();
                        newLicensePlate.PlateImage = Uri.fromFile(dest);
                        newLicensePlate.PlateNumber = plateNumber;
                        newLicensePlate.Confidence = confidence;
                        LicensePlateContent.addItem(newLicensePlate);
                    }
                });
            }
        }
    }

    public static void saveBmp(Bitmap bmp, String destPath){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(destPath);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void deleteSavedImages() {
        File dir = this.getFilesDir();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                String absolutePath = file.getAbsolutePath();
                String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
                if (extension.equals(".jpg")) {
                    file.delete();
                }
            }
        }
        LicensePlateContent.ITEMS.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear) {
            deleteSavedImages();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(LicensePlate item) {

    }
}
