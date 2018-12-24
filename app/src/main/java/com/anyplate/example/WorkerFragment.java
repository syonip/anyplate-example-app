package com.anyplate.example;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;

public class WorkerFragment extends Fragment {

    // data object we want to retain
    private ArrayList<LicensePlate> licensePlates;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(ArrayList<LicensePlate> licensePlates) {
        this.licensePlates = licensePlates;
    }

    public ArrayList<LicensePlate> getData() {
        return licensePlates;
    }
}