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
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.*;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.*;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class ZonePicker extends SlidingFragmentActivity implements LocationListener, GoogleMap.OnCameraChangeListener {     //LocationSource,
	
	private long updateTime = 2000;//90000; // in Milliseconds
	private long updateRange = 25;//1000; // in Meters
	private static final NumberFormat nf = new DecimalFormat("##.###");
	private static final String POINT_LATITUDE_KEY = "POINT_LATITUDE_KEY";
	private static final String POINT_LONGITUDE_KEY = "POINT_LONGITUDE_KEY";
	private static final String POINT_RADIUS_KEY = "POINT_RADIUS_KEY";
	private static final String POINT_ADDRESS_KEY = "POINT_ADDRESS_KEY";

    private static final String RADIUS = "radius";
    private static final String LOC = "loc";
    private static final String TONE = "tone";
    private static final String ADDRESS = "address";

	private LocationManager locationManager;
    private Criteria crit = new Criteria();

    //--Variables for Map--//
    private GoogleMap mMap;
    public Boolean everTouched = false;

    //Objects for alarm
    private Zone zone;

    //--Variables for ringtone--//
    private Uri uri;

    //--Variables for slider drawer--//
    protected Fragment mFrag;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.main);

        // Setup the map
        setUpMapIfNeeded();
        AlarmOverlay alarmView = (AlarmOverlay) findViewById(R.id.alarm_overlay);
        alarmView.setMap(mMap, this);

        // Set the Behind View
        setBehindContentView(R.layout.menu_frame);
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        mFrag = new DrawerFragment();
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Stop existing alarms
        stopService(new Intent(ZonePicker.this,
                AlarmService.class));
        
        //Reset to default ring tone. (Otherwise it is silent!)
        initTone();
        
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                updateTime,
                updateRange,
                this);

        //If system has a last known location, use that...
        if (lastKnownLocation!=null)
        {
            zone = new Zone(lastKnownLocation, 1000);
            animateTo(zone.getLocation());
            alarmView.setZone(zone);
        }
        //Else use an arbitrary point
        else{
            Location tempLocation = new Location("");
            tempLocation.setLatitude(42.36544);
            tempLocation.setLongitude(-71.103644);
            zone = new Zone(tempLocation, 1000);
            animateTo(zone.getLocation());
            alarmView.setZone(zone);
        }

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_black));
    }

    private void animateTo(Location location) {
        animateTo(new LatLng(location.getLatitude(), location.getLongitude()));
    }
    private void animateTo(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {}
            @Override
            public void onCancel() {}
        });
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putInt("radiusMeters", zone.getRadius());
//        outState.putParcelable("alarmLocation", alarmLocation);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        radiusMeters = savedInstanceState.getInt("radiusMeters");
//        alarmLocation = savedInstanceState.getParcelable("alarmLocation");
    }

    //Set Default Ringtone
    protected void initTone() {
    	Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); 
		if(alert == null){
	         alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	         if(alert == null){
	             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
	         }
	     }
		uri = alert;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        int received_Set_Ringtone = 131072;
        int set_Pinned = 1;
        if (requestCode == received_Set_Ringtone && resultCode == RESULT_OK)
        {
            uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                Toast.makeText(this, "Setting ringtone", Toast.LENGTH_LONG).show();
            }
            else
            	Toast.makeText(this, "Ringtone: Silent" , Toast.LENGTH_LONG).show();
        }
        else if (requestCode == 10001)
        {
            TextView donateView = (TextView) mFrag.getView().findViewById(R.id.row_title_donate);
            if (donateView !=null)
            {
                donateView.setText("Thanks!");
                donateView.setBackgroundColor(0xFF11FF11);
            }
        }
        else if (requestCode == set_Pinned && resultCode == RESULT_OK)
        {
        	if(data.getExtras() != null){
                setNewAlarmZone(data.getExtras());
            }
        }
    }
    
    //Set Alarm
    public void setNewAlarmZone(Bundle extras)
    {
        zone.setRadius(extras.getInt(RADIUS));
        zone.setCenter((Location) extras.get(LOC));
        animateTo(zone.getLocation());
	    everTouched = true;
    }

    //User clicks "Set Alarm"
    private void saveAlarmPoint() {
        Location location = zone.getLocation();
    	if (location==null) {
    		Toast.makeText(this, "No location. Try again...", Toast.LENGTH_LONG).show();
    		return;
    	}

      	Geocoder geocoder = new Geocoder(this);
		List<Address> addresses;
		String address;
		try {
			addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality();
		} catch (IOException e) {
//			Log.e("Geoffrey", "Geocoding error...");
			e.printStackTrace();
			address = "Unknown address";
		}
    	
    	saveCoordinatesInPreferences((float)location.getLatitude(), (float)location.getLongitude(), zone.getRadius(), address);
    	locationManager.removeUpdates(this);
    	
    	Intent bdintent = new Intent();
    	bdintent.setClassName("com.geoffreybuttercrumbs.arewethereyet", "com.geoffreybuttercrumbs.arewethereyet.AlarmService");
    	bdintent.putExtra(RADIUS, zone.getRadius());
    	bdintent.putExtra(LOC, location);
    	bdintent.putExtra(TONE, uri);
    	bdintent.putExtra(ADDRESS, address);
    	startService(bdintent);
    	
    	Toast.makeText(this, "Saving Alarm...", Toast.LENGTH_LONG).show();
    	
    	finish();
	}
    
    //Store Alarm info
    private void saveCoordinatesInPreferences(float latitudeSP, float longitudeSP, int radiusSP, String address) {
    	SharedPreferences prefs = this.getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);
    	SharedPreferences.Editor prefsEditor = prefs.edit();
    	for (int i=5; i>1; i--){
	    	prefsEditor.putFloat(POINT_LATITUDE_KEY + i, prefs.getFloat(POINT_LATITUDE_KEY + (i-1), 0));
	    	prefsEditor.putFloat(POINT_LONGITUDE_KEY + i, prefs.getFloat(POINT_LONGITUDE_KEY + (i-1), 0));
	    	prefsEditor.putInt(POINT_RADIUS_KEY + i, prefs.getInt(POINT_RADIUS_KEY + (i-1), 0));
	    	prefsEditor.putString(POINT_ADDRESS_KEY + i, prefs.getString(POINT_ADDRESS_KEY + (i-1), ""));
    	}
    	prefsEditor.putFloat(POINT_LATITUDE_KEY+1, latitudeSP);
    	prefsEditor.putFloat(POINT_LONGITUDE_KEY+1, longitudeSP);
    	prefsEditor.putInt(POINT_RADIUS_KEY+1, radiusSP);
    	prefsEditor.putString(POINT_ADDRESS_KEY+1, address);
    	prefsEditor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
            case R.id.menu_set:
                saveAlarmPoint();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
	
	public void onPause() {
        mMap.setLocationSource(null);
		locationManager.removeUpdates(this);
		super.onPause();
	}
	
	public void onResume(){
		super.onResume();

        locationManager.requestLocationUpdates(
        		LocationManager.GPS_PROVIDER,
        		updateTime,
        		updateRange,
        		this);

        if(mMap.getMyLocation()!=null && !everTouched)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()), 14));
        }
	}

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

	@Override
	public void onLocationChanged(Location location) {
        if (everTouched != null && !everTouched) {
            if (zone != null)
                zone.setCenter(location);
            else {
                zone = new Zone(location, zone.getRadius());
            }
            animateTo(zone.zoneCenter);
        }
	}

	@Override
	public void onProviderDisabled(String provider) {
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER))
        Toast.makeText(getApplicationContext(), "GPS is turned off. Are We There Yet only works when GPS is turned on.", Toast.LENGTH_LONG).show();
	}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

    //----Map Methods----//
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        if (checkReady())
        {
            UiSettings mUiSettings = mMap.getUiSettings();
            mUiSettings.setZoomControlsEnabled(false);
            mMap.setMyLocationEnabled(true);
            mUiSettings.setMyLocationButtonEnabled(false);
            mUiSettings.setTiltGesturesEnabled(false);
            mUiSettings.setRotateGesturesEnabled(false);
            mMap.setOnCameraChangeListener(this);
        }
    }

    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, "Unable to generate map.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    class Zone {
        LatLng zoneCenter;
        int radiusMeters;
        Marker mMarker;
        Circle circle;

       public Zone(Location zoneCenterLocation, int radiusMeters)
       {
           super();
           this.zoneCenter = new LatLng(zoneCenterLocation.getLatitude(), zoneCenterLocation.getLongitude());
           this.radiusMeters = radiusMeters;
           //TODO: BitmapDescriptor causes crash when play APIs are not available (i.e. factory data reset).
           mMarker = mMap.addMarker(new MarkerOptions().position(zoneCenter).icon(BitmapDescriptorFactory.fromResource(R.drawable.zonecenter)).anchor(.5f, .75f));
           circle = mMap.addCircle(new CircleOptions().center(zoneCenter).radius(radiusMeters).strokeColor(0xEEFF0000).fillColor(0x20FF0000).strokeWidth(4));
       }

        public void setCenter(Location zoneCenterLocation)
        {
            this.zoneCenter = new LatLng(zoneCenterLocation.getLatitude(), zoneCenterLocation.getLongitude());
            this.setCenter(zoneCenter);
        }
        public void setCenter(LatLng zoneCenterLatLng)
        {
            this.zoneCenter = zoneCenterLatLng;
            mMarker.setPosition(zoneCenter);
            circle.setCenter(zoneCenter);
        }

        public void setRadius(int radiusMeters)
        {
            this.radiusMeters = radiusMeters;
            circle.setRadius(radiusMeters);
        }

        public Location getLocation() {
            Location location = new Location("");
            location.setLatitude(zoneCenter.latitude);
            location.setLongitude(zoneCenter.longitude);
            return location;
        }

        public int getRadius() {
            return radiusMeters;
        }
    }
}