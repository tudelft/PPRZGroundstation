package com.gcs.fragments;

import com.gcs.MainActivity;
import com.gcs.R;
import com.gcs.core.ConflictStatus;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.DragEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AltitudeTape extends Fragment {
	
	private FrameLayout framelayout;
	private View rootView;

    final SparseArray<Integer> labelList = new SparseArray<>();
    private ArrayList<Integer> aircraftInGroupList = new ArrayList<>();
    private ConcurrentHashMap<String, Integer> stringToLabelIdList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer,String> labelIdToStringList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> groupSelectionIdList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> groupSelectLabelList = new ConcurrentHashMap<>();

    private String selectedGroup = null;

    private int draggedLabel, textGravity;
    private int backgroundImg, yellowLabel, blueLabel, grayLabel, redLabel, LargeBlueLabel, LargeRedLabel;

    TextView label;

    private boolean groupDeselected = false, groupSelected = false, targetCreated = false;

    private int groundLevelTape, flightCeilingTape;
//    private final int groundLevelTape   = 890; //0 meter
//    private final int flightCeilingTape = 0;  //20 m

    private double flightCeiling, groundLevel, MSA; //[m]

    private static final Point smallLabelDimensions = new Point (80,70);
    private static final int dragShadowVerticalOffset = smallLabelDimensions.y*2;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        //Initial loading of the resources to be used later
        yellowLabel    = R.drawable.altitude_label_small_yellow_flipped;
        blueLabel      = R.drawable.altitude_label_small_blue;
        grayLabel      = R.drawable.altitude_label_small_gray;
        redLabel       = R.drawable.altitude_label_small_red;
        LargeBlueLabel = R.drawable.altitude_label_large_blue;
        LargeRedLabel  = R.drawable.altitude_label_large_red;
		
        // Inflate the layout for this fragment
		rootView = inflater.inflate(R.layout.altitude_tape, container, false);

        return rootView;    
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Load airspace data from resources (Settings are done there: numerical.xml)
        flightCeiling = getResources().getInteger(R.integer.flightCeiling);
        groundLevel   = getResources().getInteger(R.integer.groundLevel);
        MSA           = getResources().getInteger(R.integer.MSA);

        //Handle to the altitude tape view
        ImageView altitudeTape = (ImageView) getView().findViewById(R.id.altitudeTapeView);

        ////////////////Programmatically draw the altitude tape/////////////////
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int outerHeight = (int)(size.y*0.6);
        int outerWidth  = (int)(size.x*0.04);
        int vertOffset  = (int)(outerHeight/27.4);
        int horOffset   = (int)(0.2*outerWidth);

        int MSAheight = (int)((1-(float)MSA/(flightCeiling-groundLevel))*outerHeight);

        Bitmap bitmap = Bitmap.createBitmap(100,outerHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        //Blue fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.blueTape));
        canvas.drawRect(horOffset,outerHeight-vertOffset,outerWidth-horOffset,vertOffset,paint);
        //Fill brown
        paint.setColor(getResources().getColor(R.color.brownTape));
        canvas.drawRect(horOffset,outerHeight-vertOffset,outerWidth-horOffset,MSAheight,paint);
        //Frame
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawRect(horOffset,outerHeight-vertOffset,outerWidth-horOffset,vertOffset,paint);
        //Text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(20);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("MSA", outerWidth/2, MSAheight, textPaint);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        altitudeTape.setBackground(drawable);
        /////////////////////////////////

        //Set the location of the bounds of the tape to be able to determine label locations
        flightCeilingTape = 0;
        groundLevelTape   = outerHeight-(2*vertOffset);

        //OnCLickListener on the altitude tape (used for deselection of all labels)
        altitudeTape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set the group selection to null, go back to normal display of the labels
                selectedGroup = null;
                //Remove selected group labels from tape
                removeGroupSelectedAircraft();
                //Clear the list holding the id's of the group selection labels
                groupSelectLabelList.clear();
                //Change the selection status of all aircraft to not selected
                ((MainActivity) getActivity()).deselectAllAircraft();
            }
        });

        //Create a handle to the (frame)layout of the altitude tape to be able to place labels on it
        framelayout = (FrameLayout) rootView.findViewById(R.id.altitudeTapeFragment);
    }
	
	//OnCLickListener for individual labels
	View.OnClickListener onLabelClick(final View tv) {
	    return new View.OnClickListener() {
	        public void onClick(View v) {
                //If a group is selected, deselect it
                if(groupSelected) {
                    selectedGroup = null;
                    groupSelected = false;
                    groupDeselected = true;
                    groupSelectLabelList.clear();
                    removeGroupSelectedAircraft();
                }

                //Get the id of the selected aircraft and get its selection status from mainactivity
                int aircraftNumber = labelList.get(v.getId());
                boolean isAircraftSelected = ((MainActivity) getActivity()).isAircraftIconSelected(aircraftNumber);

                //Invert the selection status
                ((MainActivity) getActivity()).setIsSelected(aircraftNumber, !isAircraftSelected);
        	}
	    };
	}

    //OnCLickListener for the group labels
    View.OnClickListener onGroupLabelClick(final View tv) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                //Set (boolean) that a group is selected and store its labelcharacter string to later check if the group label should be drawn again or if it is selected
                groupSelected = true;
                selectedGroup = labelIdToStringList.get(v.getId());

                //Hide group label from view
                getView().findViewById(v.getId()).setVisibility(View.GONE);

                //Set the selection status aircraft icons(get all separate aircraft numbers from the characters and send them to mainactivity to set them selected as group)
                String[] acCharacters = labelIdToStringList.get(v.getId()).split(" ");
                int[] acNumbers = new int[ acCharacters.length ];
                for(int i = 0; i< acCharacters.length; i++) {
                    acNumbers[i] = Character.getNumericValue(acCharacters[i].charAt(0)) - 9;
                }
                ((MainActivity) getActivity()).setGroupSelected(acNumbers);
            }
        };
    }
	
	//OnLongClickListener for the altitude labels (A long click starts the drag feature)
	View.OnLongClickListener onLabelLongClick(final View tv) {
		return new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

                //Create a DragShadowBuilder
	            ClipData data = ClipData.newPlainText("", "");
                myDragShadowBuilder myShadow = new myDragShadowBuilder(tv);

                //Reference for the ondrag method to know which label is being dragged (used in the onDragListener)
                draggedLabel = v.getId();
	            
	            // Start the drag
	            v.startDrag(data,        // the data to be dragged
                        myShadow,    // the drag shadow builder
                        null,        // no need to use local data
                        0            // flags (not currently used, set to 0)
                );
				return true;
			}
		};
	}

    //Listener to the drag that was started using a long click
    View.OnDragListener labelDragListener(final View tv) {
        return new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        break;
                    case DragEvent.ACTION_DROP:
                        //Send the drop location to the method that implements the command (Note that an offset value was used to be able to see the label while dragging)
                        if (groupSelectLabelList.containsKey(draggedLabel)) {   //Group selection label
                            setTargetAltitude(groupSelectLabelList.get(draggedLabel), event.getY() - dragShadowVerticalOffset);
                        } else{                                                 //Normal label
                            setTargetAltitude(labelList.get(draggedLabel), event.getY() - dragShadowVerticalOffset);
                        }
                        break;
                    default:
                        break;
                }
            return true;
            }
        };
    }

    //Custom dragshadow builder, specifically used to offset the dragshadow from touchpoint because it otherwise would be under the user's finger
    public static class myDragShadowBuilder extends View.DragShadowBuilder {
        public myDragShadowBuilder(View view) {
            super(view);
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point touchPoint) {
            super.onProvideShadowMetrics(shadowSize, touchPoint);
            //Offset the dragshadow for better visibility
            touchPoint.set(smallLabelDimensions.x / 2, dragShadowVerticalOffset);
        }
    }

    ////////////LABEL DRAWING////////// (Mainactivity determines which labels it wants to draw, here colors are determined and it is checked if labels should be drawn again etc.)

    //Method to draw individual labels on the altitude tape
	public void setLabel(double altitude, int labelId, String labelCharacter, boolean isAircraftIconSelected, boolean labelCreated, int acNumber, int visibility){
        //Determine if a certain label(id) is already present in the list  that keeps track of all individual labels
        if (labelList.get(labelId) == null) {
            labelList.put(labelId, acNumber);
        }

        //If the aircraft is selected, use a yellow label, otherwise a label based on the conflict status should be used
        if (isAircraftIconSelected) {
            backgroundImg = yellowLabel;
        } else {
            //Get conflict status
            ConflictStatus conflictStatus = ((MainActivity) getActivity()).getConflictStatus(acNumber);
            switch (conflictStatus) {
                case BLUE:
                    backgroundImg = blueLabel;
                    break;
                case GRAY:
                    backgroundImg = grayLabel;
                    break;
                case RED:
                    backgroundImg = redLabel;
                    break;
                default:
                    backgroundImg = blueLabel;
            }
        }

        //Set the size of the label that will be drawn and add a topmargin to place it vertically on the tape
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(smallLabelDimensions.x,smallLabelDimensions.y);
        params.topMargin = altitudeToLabelLocation(altitude);

        //Set alignment of the label based on the selection status (selected ones left, unselected ones right)
        if (!((MainActivity) getActivity()).isAircraftIconSelected(acNumber)) {
            params.gravity = Gravity.RIGHT;
            textGravity = Gravity.CENTER;
        } else {
            params.gravity = Gravity.LEFT;
            textGravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        }

        //Creat a label if it is the first time it will be drawn on the tape, else only update it
        if (!labelCreated) {
            label = new TextView(getActivity());
            label.setId(labelId);
            label.setBackgroundResource(backgroundImg);
            label.setVisibility(visibility);
            label.setMinWidth(20);
            label.setText("   " + labelCharacter);
            label.setTypeface(null, Typeface.BOLD);
            label.setGravity(textGravity);
            label.setOnClickListener(onLabelClick(label));
            label.setOnLongClickListener(onLabelLongClick(label));
            rootView.setOnDragListener(labelDragListener(label));
            framelayout.addView(label, params);

            //Set the isLabelCreated status of the aircraft to prevent it will be created again in the upcoming loops
            ((MainActivity) getActivity()).setIsLabelCreated(true, acNumber);
        } else {
            label = (TextView) getView().findViewById(labelId);
            label.setBackgroundResource(backgroundImg);
            label.setVisibility(visibility);
            label.setGravity(textGravity);
            framelayout.updateViewLayout(label, params);
        }
	}

    //Method to draw group labels on the altitude tape
    public void drawGroupLabel(boolean inConflict, double altitude, String labelCharacters, int[] ac) {

        //Add the numbers of aircraft that are in a group to the list to avoid that they also get an individual label
        for(int i=0; i<ac.length; i++) {
            if(!aircraftInGroupList.contains(ac[i])) {
                aircraftInGroupList.add(ac[i]);
            }
        }

        groupDeselected = false;

        if(selectedGroup == null || !selectedGroup.equals(labelCharacters)) {
//            int backgroundImg;
            if (inConflict) {
                backgroundImg = LargeRedLabel;
            } else {
                backgroundImg = LargeBlueLabel;
            }

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(smallLabelDimensions.x,smallLabelDimensions.y);
            params.topMargin = altitudeToLabelLocation(altitude);
            params.gravity = Gravity.RIGHT;

            TextView groupLabel;
            if (!stringToLabelIdList.containsKey(labelCharacters)) {
                int labelId = TextView.generateViewId();
                groupLabel = new TextView(getActivity());
                groupLabel.setText("   " + labelCharacters);
                groupLabel.setTypeface(null, Typeface.BOLD);
                groupLabel.setGravity(Gravity.CENTER);
                groupLabel.setId(labelId);
                groupLabel.setBackgroundResource(backgroundImg);
                groupLabel.setOnClickListener(onGroupLabelClick(groupLabel));
                framelayout.addView(groupLabel, params);
                stringToLabelIdList.put(labelCharacters, labelId);
                labelIdToStringList.put(labelId, labelCharacters);
            } else {
                groupLabel = (TextView) getView().findViewById(stringToLabelIdList.get(labelCharacters));
                groupLabel.setBackgroundResource(backgroundImg);
                groupLabel.setGravity(Gravity.CENTER);
                groupLabel.setVisibility(View.VISIBLE);
                framelayout.updateViewLayout(groupLabel, params);
            }
        }
    }

    public void drawGroupSelection(double altitude, String labelCharacter, int i, int numberOfLabels, int acNumber) {

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(smallLabelDimensions.x,smallLabelDimensions.y);
        params.gravity = Gravity.LEFT;
        params.topMargin = altitudeToLabelLocation(altitude);

        //Add a margin to the right of a label if it will overlap with another label
        if(i==0) {
            params.leftMargin = 75 + (numberOfLabels-1)*smallLabelDimensions.x;
        } else {
            params.rightMargin = 75 + (smallLabelDimensions.x*i);
            params.leftMargin = (numberOfLabels-i)*smallLabelDimensions.x;
        }

        TextView groupSelectionLabel;

        if (!groupSelectionIdList.containsKey(labelCharacter)) {
            int labelId = TextView.generateViewId();
            groupSelectionLabel = new TextView(getActivity());
            groupSelectionLabel.setText("   " + labelCharacter);
            groupSelectionLabel.setTypeface(null, Typeface.BOLD);
            groupSelectionLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            groupSelectionLabel.setId(labelId);
            groupSelectionLabel.setBackgroundResource(yellowLabel);
            groupSelectionLabel.setOnLongClickListener(onLabelLongClick(groupSelectionLabel));
            rootView.setOnDragListener(labelDragListener(groupSelectionLabel));
            framelayout.addView(groupSelectionLabel, params);
            groupSelectionIdList.put(labelCharacter,labelId);
            groupSelectLabelList.put(labelId, acNumber);
        } else {
            groupSelectionLabel = (TextView) getView().findViewById(groupSelectionIdList.get(labelCharacter));
            framelayout.updateViewLayout(groupSelectionLabel, params);
        }
    }

    private void removeGroupSelectedAircraft() {
        for (String key : groupSelectionIdList.keySet()) {
            framelayout.removeView(getView().findViewById(groupSelectionIdList.get(key)));
        }
        groupSelectionIdList.clear();
    }

    //Method called from mainactivity if no group labels are drawn. Then if there are still labelId's in the list, remove them from the view and list.
    public void removeGroupLabels() {
//        if(!stringToLabelIdList.isEmpty() && groupDeselected) {
        if(!stringToLabelIdList.isEmpty()) {
            for (String key : stringToLabelIdList.keySet()) {
                framelayout.removeView(getView().findViewById(stringToLabelIdList.get(key)));
            }

            stringToLabelIdList.clear();
            labelIdToStringList.clear();
            aircraftInGroupList.clear();
        }
    }

    //Method to draw the target altitude on the altitude tape
	public void setTargetLabel(double targetAltitude, int targetLabelId) {

		/* TODO make a better indicating icon/bug for the target altitude */
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(smallLabelDimensions.x,smallLabelDimensions.y);
        params.topMargin = altitudeToLabelLocation(targetAltitude);
        params.gravity = Gravity.RIGHT;

		View target;
		if(!targetCreated) {
			target = new View(getActivity());
			target.setBackgroundResource(R.drawable.altitude_label_small_red);
            target.setId(targetLabelId);
            target.setVisibility(View.VISIBLE);
            framelayout.addView(target,params);
			targetCreated = true;
		} else {
			target = getView().findViewById(targetLabelId);
			target.setVisibility(View.VISIBLE);
            framelayout.updateViewLayout(target,params);
		}
	}
	
	//Method to remove the target label from the altitude tape
	public void deleteTargetLabel(int targetLabelId) {
		View targetLabel = getView().findViewById(targetLabelId);

		if(targetLabel!=null) {
			targetLabel.setVisibility(View.GONE);
		}
	}
	
	//Convert altitude to a label location on the tape
	private int altitudeToLabelLocation(double altitude) {
		
		int lengthBar = groundLevelTape - flightCeilingTape;
		double verticalRange = flightCeiling - groundLevel;
		int labelLocation = (int) (groundLevelTape-((altitude/verticalRange)*lengthBar));
		
		return labelLocation;
	}
	
	//Convert label location on the tape to an altitude 
	private double labelLocationToAltitude(float labelLocation) {
		
		int lengthBar = groundLevelTape - flightCeilingTape;
		double verticalRange = flightCeiling - groundLevel;
		double altitude = verticalRange*((double) groundLevelTape-labelLocation)/lengthBar;
		
		return altitude;
	}

	//Set the target altitude to the service
	private void setTargetAltitude(int aircraftNumber,float dropLocation) {

		double dropAltitude = labelLocationToAltitude(dropLocation);

		//If the label is dropped outside the altitude tape, set the target altitude at the bounds.
		if (dropAltitude < groundLevel) {
			dropAltitude = groundLevel;
		} else if (dropAltitude > flightCeiling) {
			dropAltitude = flightCeiling;
		}

		/* TODO Set the target altitude to the service once this function is available */
//		setTargetLabel(dropAltitude, 10); //Temporary setfunction to show a label
	}
}