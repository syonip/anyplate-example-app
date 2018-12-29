package com.anyplate.example.dummy;

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
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<LicensePlate> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, LicensePlate> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    public static void loadSavedImages(File dir) {
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

    static {
//         Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
//        }

    }

    private static void addItem(LicensePlate item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}