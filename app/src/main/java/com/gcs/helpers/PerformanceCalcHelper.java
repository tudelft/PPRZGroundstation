package com.gcs.helpers;

import android.location.Location;
import android.util.SparseArray;

import com.gcs.core.Aircraft;
import com.google.android.gms.maps.model.LatLng;

public class PerformanceCalcHelper {

    public static final double calcPerformance(int ROIRadius, int acCoverageRadius, LatLng ROIcenter, SparseArray<Aircraft> mAircraft) {

        /* TODO: finish the performance score calculation (add: conflict time score, collisions) */

        return ROIcovered(ROIRadius,acCoverageRadius,ROIcenter,mAircraft)*LossOfCommunicationCheck(mAircraft)*100; //Percentage;
    }

    //Calculate the percentage of the Region of Interest (ROI) that is covered by surveillance aircraft
    private final static double ROIcovered(int ROIRadius, int acCoverageRadius, LatLng ROIcenter, SparseArray<Aircraft> mAircraft){
        //Region of interest parameters
        double AREA = ROIRadius*ROIRadius*Math.PI;

        //Calculate the overlap between covered region by aircraft and the ROI area
        double overlapArea = 0;

        for(int i = 1; i<=mAircraft.size(); i++) { //Loop over all aircraft
            if(mAircraft.get(i).getCommunicationSignal()>0 && mAircraft.get(i).isSurveillance()) { //Only calculate coverage if the aircraft can communicate with the ground station and has a surveillance status (at correct altitude)

                //Add overlap between aircraft coverage and ROI
                double overlap = circleOverlap(ROIRadius, acCoverageRadius, ROIcenter, mAircraft.get(i).getLatLng());
                double doubleOverlap = 0;
    /* TODO: account for overlap by decreasing performance score in case of overlap/violation of the separation standard instead of calculating overlap */
                //NOTE THAT THE OVERLAP OF 3+ CIRCLES IS NOT COVERED!!
                if (overlap > 0) { //If not outside the ROI
                    for (int j = i + 1; j <= mAircraft.size(); j++) {
                        //Account for overlap of the two UAVs
                        doubleOverlap += circleOverlap(acCoverageRadius, acCoverageRadius, mAircraft.get(i).getLatLng(), mAircraft.get(j).getLatLng());
                    }
                }
                //Calculate the total coverage ove the ROI
                overlapArea += overlap - doubleOverlap;
            }
        }
        //Coverage percentage
        return overlapArea/AREA;
    }

    //Calculate the overlap area of two (coverage) circles
    private final static double circleOverlap(double radius1, double radius2, LatLng c1, LatLng c2){
        //Calculation of distance between two LatLng coordinates
        float[] distance = new float[1];
        Location.distanceBetween(c1.latitude, c1.longitude, c2.latitude, c2.longitude, distance);

        //Define the used radii
        double R = radius1;
        double r = radius2;

        //Make sure R is the largest of the two circles
        if(R < r) {
            R = radius2;
            r = radius1;
        }

        //Check whether the circles overlap, do not intersect or are inside each other. Then calculate accordingly
        double overlapArea;
        if(distance[0] > (R+r)) {                  //No overlap
            overlapArea = 0;
        } else if((distance[0]+r) <= R) {  //inside
            //Entire area of the small circle
            overlapArea = r*r*Math.PI;
        } else {                                   //Overlap
            double part1 = r*r*Math.acos((distance[0]*distance[0] + r*r - R*R)/(2*distance[0]*r));
            double part2 = R*R*Math.acos((distance[0]*distance[0] + R*R - r*r)/(2*distance[0]*R));
            double part3 = 0.5*Math.sqrt((-distance[0]+r+R)*(distance[0]+r-R)*(distance[0]-r+R)*(distance[0]+r+R));
            //Subtract the triangle areas from the cone areas to end up with the overlap area
            overlapArea = part1 + part2 - part3;
        }
        return overlapArea;
    }

    //Calculate the percentage of aircraft that have connection with the ground station
    private final static double LossOfCommunicationCheck(SparseArray<Aircraft> mAircraft) {
        float numberOfAircraft = mAircraft.size();
        int activeAircraft = 0;

        //Loop over aircraft in system
        for(int i=1; i<mAircraft.size()+1; i++) {
            if(mAircraft.get(i).getCommunicationSignal()>0) {
                activeAircraft++;
            }
        }
        return activeAircraft/numberOfAircraft;
    }
}