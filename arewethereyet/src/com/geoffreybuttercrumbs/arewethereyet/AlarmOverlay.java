//      Copyright 2013 Geoffrey Buttercrumbs
//
//        Licensed under the Apache License, Version 2.0 (the "License");
//        you may not use this file except in compliance with the License.
//        You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//        Unless required by applicable law or agreed to in writing, software
//        distributed under the License is distributed on an "AS IS" BASIS,
//        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//        See the License for the specific language governing permissions and
//        limitations under the License.

package com.geoffreybuttercrumbs.arewethereyet;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class AlarmOverlay extends View {
    //--Variables for AlarmOverlay--//
    private Boolean drag = false;
    private Boolean move = false;

    //--Variables for Map--//
    private GoogleMap mMap;
    private ZonePicker ZonePicker;
    private ZonePicker.Zone zone;

    public AlarmOverlay(Context context) {
        super(context);
    }
    public AlarmOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public AlarmOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMap(GoogleMap mMap, ZonePicker ZonePicker)
    {
        this.mMap = mMap;
        this.ZonePicker = ZonePicker;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        double DEGREE_METERS = 111133.745;

        LatLng tapLatLng = mMap.getProjection().fromScreenLocation(new Point(Math.round(me.getX()), Math.round(me.getY())));
        Location tapLocation = new Location("");
        tapLocation.setLatitude(tapLatLng.latitude);
        tapLocation.setLongitude(tapLatLng.longitude);

        Location alarmLocation = new Location("");
        alarmLocation.set(zone.getLocation());

        Location radiusLocation = new Location("");
        radiusLocation.setLatitude(zone.getLocation().getLatitude() + (zone.getRadius()/ DEGREE_METERS));
        radiusLocation.setLongitude(zone.getLocation().getLongitude());

        if (me.getAction() == 0){
            if (tapLocation.distanceTo(alarmLocation) < (radiusLocation.distanceTo(alarmLocation)*1.1) && tapLocation.distanceTo(alarmLocation) > radiusLocation.distanceTo(alarmLocation)*.25){
                drag=true;
            }
            else if (tapLocation.distanceTo(alarmLocation) < radiusLocation.distanceTo(alarmLocation)*.25){
                move=true;
            }
            ZonePicker.everTouched = true;
        }
        else if (me.getAction() == 2){
            if (drag){
                zone.setRadius(Math.round(tapLocation.distanceTo(alarmLocation)));
            }
            else if (move){
                zone.setCenter(tapLatLng);
            }
            zone.circle.setFillColor(0x05FF0000);
        }
        else if (me.getAction() == 1){
            if (drag)
            {
                drag=false;
                zone.setRadius(Math.round(tapLocation.distanceTo(alarmLocation)));
            }
            if (move)
            {
                move=false;
                zone.setCenter(tapLatLng);
            }
            zone.circle.setFillColor(0x20FF0000);
        }

        return drag || move || super.onTouchEvent(me);
    }

    public void setZone(com.geoffreybuttercrumbs.arewethereyet.ZonePicker.Zone zone) {
        this.zone = zone;
    }
}
