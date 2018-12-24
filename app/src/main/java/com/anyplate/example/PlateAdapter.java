package com.anyplate.example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyplate.example.R;

import java.util.ArrayList;

public class PlateAdapter extends ArrayAdapter<LicensePlate> {
    public PlateAdapter(Context context, ArrayList<LicensePlate> licensePlates) {
        super(context, 0, licensePlates);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        LicensePlate plate = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item, parent, false);
        }
        // Lookup view for data population
        TextView plateNumberView = convertView.findViewById(R.id.plate_number);
        TextView confidenceView = convertView.findViewById(R.id.confidence);
        ImageView plateImageView = convertView.findViewById(R.id.plate_image);
        // Populate the data into the template view using the data object
        plateNumberView.setText(plate.PlateNumber);
        confidenceView.setText("Confidence: "+Integer.toString((int)plate.Confidence)+ "%");
        plateImageView.setImageURI(plate.PlateImage);
        // Return the completed view to render on screen
        return convertView;
    }

}
