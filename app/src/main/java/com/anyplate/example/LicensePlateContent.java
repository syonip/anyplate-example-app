package com.anyplate.example;

import android.net.Uri;

import com.anyplate.example.LicensePlate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
class LicensePlateContent {

    /**
     * An array of sample (dummy) items.
     */
    static final List<LicensePlate> ITEMS = new ArrayList<>();

    private static final Map<String, LicensePlate> ITEM_MAP = new HashMap<>();

    static void loadSavedImages(File dir) {
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

    private static void loadImage(File file) {
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
        addItem(newLicensePlate);
    }


    static void addItem(LicensePlate item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }
}
