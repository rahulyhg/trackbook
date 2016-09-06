/**
 * Track.java
 * Implements the Track class
 * A Track stores a list of waypoints
 *
 * This file is part of
 * TRACKBOOK - Movement Recorder for Android
 *
 * Copyright (c) 2016 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 *
 * Trackbook uses osmdroid - OpenStreetMap-Tools for Android
 * https://github.com/osmdroid/osmdroid
 */

package org.y20k.trackbook.core;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.y20k.trackbook.helpers.LocationHelper;
import org.y20k.trackbook.helpers.LogHelper;
import org.y20k.trackbook.helpers.TrackbookKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Track class
 */
public class Track implements TrackbookKeys, Parcelable {

    /* Define log tag */
    private static final String LOG_TAG = Track.class.getSimpleName();


    /* Main class variables */
    private List<WayPoint> mWayPoints;
    private float mTrackLength;
    private long mTrackDuration;


    /* Constructor */
    public Track() {
        mWayPoints = new ArrayList<WayPoint>();
        mTrackLength = 0;
    }


    /* Constructor used by CREATOR */
    protected Track(Parcel in) {
        mWayPoints = in.createTypedArrayList(WayPoint.CREATOR);
        mTrackLength = in.readFloat();
    }


    /* CREATOR for Track object used to do parcel related operations */
    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };


    /* Adds new waypoint */
    public WayPoint addWayPoint(Location location) {
        // add up distance
        mTrackLength = addDistanceToTrack(location);

        boolean stopOver;
        int wayPointCount = mWayPoints.size();
        if (wayPointCount == 0) {
            stopOver = false;
        } else {
            Location lastLocation = mWayPoints.get(wayPointCount - 1).getLocation();
            stopOver = LocationHelper.isStopOver(lastLocation, location);
        }

        // create new waypoint
        WayPoint wayPoint = new WayPoint(location, stopOver, mTrackLength);

        // add new waypoint to track
        mWayPoints.add(wayPoint);

        // TODO remove log here
        LogHelper.v(LOG_TAG, "Waypoint No. " + mWayPoints.indexOf(wayPoint) + " Location: " + wayPoint.getLocation().toString());

        return wayPoint;
    }


    /* Setter for duration of track */
    public void setTrackDuration(long trackDuration) {
        mTrackDuration = trackDuration;
    }


    /* Getter for mWayPoints */
    public List<WayPoint> getWayPoints() {
        return mWayPoints;
    }


    /* Getter size of Track / number of WayPoints */
    public int getSize() {
        return mWayPoints.size();
    }


    /* Getter for duration of track */
    public String getTrackDuration() {
        return convertToReadableTime(mTrackDuration);
    }


    /* Getter for distance of track */
    public String getTrackDistance() {
        float trackDistance = mWayPoints.get(mWayPoints.size()-1).getDistanceToStartingPoint();

        return String.format ("%.0f", trackDistance)  + "m";
    }


    /* Getter for location of specific WayPoint */
    public Location getWayPointLocation(int index) {
        return mWayPoints.get(index).getLocation();
    }

    /* Adds distance to given location to length of track */
    private float addDistanceToTrack(Location location) {
        // get number of previously recorded waypoints
        int wayPointCount = mWayPoints.size();

        // at least two data points are needed
        if (wayPointCount >= 1) {
            // add up distance
            Location lastLocation = mWayPoints.get(wayPointCount - 1).getLocation();
            return mTrackLength + lastLocation.distanceTo(location);
        }

        return 0f;
    }


    /* Converts milliseconds to hh:mm:ss */
    private String convertToReadableTime(long milliseconds) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) % TimeUnit.MINUTES.toSeconds(1));
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(mWayPoints);
        parcel.writeFloat(mTrackLength);
    }


}
