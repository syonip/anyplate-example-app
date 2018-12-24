package com.anyplate.example;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
    PlateAdapter listViewAdapter;
    static int scanCounter = 0;
    private WorkerFragment workerFragment;
    private static final String TAG_WORKER_FRAGMENT = "WorkerFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Activity activity = this;

        FragmentManager fm = getFragmentManager();
        workerFragment = (WorkerFragment) fm.findFragmentByTag(TAG_WORKER_FRAGMENT);

        // create the fragment and data the first time
        if (workerFragment == null) {
            // add the fragment
            workerFragment = new WorkerFragment();
            fm.beginTransaction().add(workerFragment, TAG_WORKER_FRAGMENT).commit();
            // load data from a data source or perform any calculation
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
                intent.setAction("com.anyplate.app.RUN");
                startActivityForResult(intent, REQUEST_CODE);
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
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
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
        if (data == null) return;

        final Activity activity = this;

        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {

            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                final Uri uri = data.getData();
                if (uri == null) return;

//                final Bitmap finalBitmap = bitmap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = null;
                        try {
                            // Works with content://, file://, or android.resource:// URIs
                            InputStream inputStream =
                                    getContentResolver().openInputStream(uri);
                            bitmap = BitmapFactory.decodeStream(inputStream);
                        } catch (FileNotFoundException e) {
                            // Inform the user that things have gone horribly wrong
                            Log.e("TEST", e.toString());
                        }

                        String plateNumber = data.getStringExtra("PlateNumber");
                        float confidence = data.getFloatExtra("Confidence", 0f);

//                        File source = new File(uri.toString());
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
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
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
        // Inflate the menu; this adds items to the action bar if it is present.
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            deleteSavedImages();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
