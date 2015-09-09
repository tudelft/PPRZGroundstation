package com.gcs.fragments;

import com.gcs.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TelemetryFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.telemetry, container, false);
	}

	public void setTextColor(int color) {
		TextView textViewVALUE = (TextView) getView().findViewById(R.id.altitudeValue);
		textViewVALUE.setTextColor(color);
		TextView textViewUNIT = (TextView) getView().findViewById(R.id.altitudeUnitLabel);
		textViewUNIT.setTextColor(color);
	}
	
	public void setText(String item) {
		TextView textView = (TextView) getView().findViewById(R.id.altitudeValue);
		textView.setText(item);
	}
}

