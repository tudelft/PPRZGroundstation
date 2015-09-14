package com.gcs.fragments;

import com.gcs.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MissionButtonFragment extends Fragment {

    private static final String TAG = MissionButtonFragment.class.getName();
	
	private View rootView;
    private Button homeButton, landButton, takeOffButton, loadWaypointsButton, loadBlocksButton;
    private Boolean homeButtonClicked = false, landButtonClicked = false, takeOffButtonClicked = false;
    private String landBlockName, takeoffBlockName;

    private enum ButtonName {
        HOME, LAND, TAKEOFF
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
		
		// Inflate the layout for this fragment
        return inflater.inflate(R.layout.mission_buttons, container, false);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create handles for the buttons
        homeButton          = (Button) getView().findViewById(R.id.goHomeButton);
        landButton          = (Button) getView().findViewById(R.id.landButton);
        takeOffButton       = (Button) getView().findViewById(R.id.takeOffButton);
        loadWaypointsButton = (Button) getView().findViewById(R.id.loadWaypointsButton);
        loadBlocksButton    = (Button) getView().findViewById(R.id.loadBlocksButton);

        landBlockName    = getResources().getString(R.string.land_block);
        takeoffBlockName = getResources().getString(R.string.take_off_button);
    }

	public void onLandRequest(View v) {

        if(landButtonClicked) {
            landButtonClicked = false;
        } else {
            landButtonClicked = true;
        }

        //Set button to active
        setButtonAppearance(landButtonClicked, ButtonName.LAND);
    }
	
	public void onTakeOffRequest(View v) {

        if(takeOffButtonClicked) {
            takeOffButtonClicked = false;
        } else {
            takeOffButtonClicked = true;
        }

        //Set button to active
        setButtonAppearance(takeOffButtonClicked, ButtonName.TAKEOFF);
	}

	public void onGoHomeRequest(View v) {

        if(homeButtonClicked) {
            homeButtonClicked = false;
        } else {
            homeButtonClicked = true;
        }

		//Set button to active
        setButtonAppearance(homeButtonClicked, ButtonName.HOME);
	}

	public void onWaypointsRequest(View v) {

        Log.d("COMMAND", "Update waypoints");
    }

    public void onBlocksRequest(View v) {
        Log.d("COMMAND", "Update blocks");
    }

    public void updateWaypointsButton() {
        loadWaypointsButton.setBackgroundResource(R.drawable.wp_button_green);
    }

    public void updateBlocksButton(boolean allBlocksLoaded) {
        if(allBlocksLoaded) {
            loadBlocksButton.setBackgroundResource(R.drawable.blocks_button_green);
        } else {
            loadBlocksButton.setBackgroundResource(R.drawable.blocks_button_blackwhite);
        }
    }

    public void updateExecutedMissionButton(String currentBlock) {
        switch (currentBlock) {
            case "Takeoff":
                takeOffButton.setBackgroundResource(R.drawable.take_off_button_active);
                homeButton.setBackgroundResource(R.drawable.home_button_inactive);
                landButton.setBackgroundResource(R.drawable.land_button_inactive);
                break;
            case "land":
                landButton.setBackgroundResource(R.drawable.land_button_active);
                homeButton.setBackgroundResource(R.drawable.home_button_inactive);
                takeOffButton.setBackgroundResource(R.drawable.take_off_button_inactive);
                break;
            case "HOME":
                homeButton.setBackgroundResource(R.drawable.home_button_active);
                takeOffButton.setBackgroundResource(R.drawable.take_off_button_inactive);
                landButton.setBackgroundResource(R.drawable.land_button_inactive);
                break;
            default:
                landButton.setBackgroundResource(R.drawable.land_button_inactive);
                takeOffButton.setBackgroundResource(R.drawable.take_off_button_inactive);
                homeButton.setBackgroundResource(R.drawable.home_button_inactive);
                break;
        }
    }

    // Method to change button appearance (active/inactive)
    public void setButtonAppearance(Boolean active, ButtonName buttonName) {

        switch (buttonName) {
            case HOME:
                if(active) {
                    homeButton.setBackgroundResource(R.drawable.home_button_active);
                } else {
                    homeButton.setBackgroundResource(R.drawable.home_button_inactive);
                }
                break;
            case LAND:
                if(active) {
                    landButton.setBackgroundResource(R.drawable.land_button_active);
                } else {
                    landButton.setBackgroundResource(R.drawable.land_button_inactive);
                }
                break;
            case TAKEOFF:
                if(active) {
                    takeOffButton.setBackgroundResource(R.drawable.take_off_button_active);
                } else {
                    takeOffButton.setBackgroundResource(R.drawable.take_off_button_inactive);
                }
                break;
            default:
                Log.e(TAG,"Button appearance could not be changed because the provided button name was not recognized");
            break;
        }
    }
}