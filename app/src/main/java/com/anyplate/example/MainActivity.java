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
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.anyplate.example.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_CODE = 1;
    private static final String TAG = "AnyPlateExample";
    PlateAdapter listViewAdapter;
    static int scanCounter = 0;
    private static final String TAG_WORKER_FRAGMENT = "WorkerFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Activity activity = this;

        FragmentManager fm = getSupportFragmentManager();
        WorkerFragment workerFragment = (WorkerFragment) fm.findFragmentByTag(TAG_WORKER_FRAGMENT);

        if (workerFragment == null) {
            workerFragment = new WorkerFragment();
            fm.beginTransaction().add(workerFragment, TAG_WORKER_FRAGMENT).commit();
            workerFragment.setData(new ArrayList<LicensePlate>());
        }

        ArrayList<LicensePlate> licensePlates = workerFragment.getData();

        ListView listView = findViewById(R.id.list_view);

        listViewAdapter = new PlateAdapter(this, licensePlates);
        listView.setAdapter(listViewAdapter);

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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadSavedImages(activity.getFilesDir());
            }
        });
    }

    private void loadSavedImages(File dir) {
        listViewAdapter.clear();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                String absolutePath = file.getAbsolutePath();
                String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
                if (extension.equals(".jpg")) {
                    loadImage(file);
                }
            }
        }
    }

    private void loadImage(File file) {
        LicensePlate newLicensePlate = new LicensePlate();
        String fileNameWithOutExt = file.getName().replaceFirst("[.][^.]+$", "");
        String[] separated = fileNameWithOutExt.split("_");

        newLicensePlate.PlateImage = Uri.fromFile(file);
        if (separated.length >= 2) {
            newLicensePlate.PlateNumber = separated[1];
        }
        if (separated.length >= 3) {
            newLicensePlate.Confidence = Float.parseFloat(separated[2]);
        }
        listViewAdapter.add(newLicensePlate);
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
                        listViewAdapter.add(newLicensePlate);
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
        listViewAdapter.clear();
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
}
