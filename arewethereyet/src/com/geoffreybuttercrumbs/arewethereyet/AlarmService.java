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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.os.Process;

public class AlarmService extends Service {
    private ServiceHandler mServiceHandler;
	
	private static int ONGOING_NOTIFICATION = 1;
	private static Location loc;
	private static int radius;
	private static Uri tone;
    private static LocationManager locationManager;
	private static LocationListener myLocationListener;
	private static long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 0; // in Meters
	private static long MINIMUM_TIME_BETWEEN_UPDATE = 1000 * 30 * 1; // in Milliseconds
	private static final int ONE_MINUTE = 1000 * 60 * 1;
	private static final String RADIUS = "radius";
	private static final String LOC = "loc";
	private static final String TONE = "tone";
	private static final String ADDRESS = "address";
	private MediaPlayer mMediaPlayer;
	private boolean everAlarmed = false;
	private static Location oldLocation;

	
	// Handler that receives messages from the thread
	  private final class ServiceHandler extends Handler {
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      @Override
	      public void handleMessage(Message msg) {
	    	  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    	  myLocationListener = new MyLocationListener();
	  		
	  		/**
	  		 * Start the location manager...
	  		 */
	    	  makeManager();
	  		
	  		/**
	  		 * Do a check to see if we are already in the zone (TODO eventually we could use this to toggle to OutOfZone alarm.)
	  		 */
	  		if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
	  			checkLoc(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
	  		}
	  		else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
	  			checkLoc(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
	      }
	  }

	  @Override
	  public void onCreate() {
	    // Start up the thread running the service.  Note that we create a
	    // separate thread because the service normally runs in the process's
	    // main thread, which we don't want to block.  We also make it
	    // background priority so CPU-intensive work will not disrupt our UI.
	    HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();
	    
	    // Get the HandlerThread's Looper and use it for our Handler 
        Looper mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);
	    

	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	      Bundle extras = intent.getExtras();
	      radius = extras.getInt(RADIUS);
	      loc = (Location) extras.get(LOC);
	      tone = (Uri) extras.get(TONE);
          String address = (String) extras.get(ADDRESS);

	      // For each start request, send a message to start a job and deliver the
	      // start ID so we know which request we're stopping when we finish the job
	      Message msg = mServiceHandler.obtainMessage();
	      msg.arg1 = startId;
	      mServiceHandler.sendMessage(msg);
	      
	      
		    /**
			 * Start running the service in the foreground so it will not be killed.
			 */
			CharSequence tickerText;
			if (address != null)
				tickerText = getText(R.string.ticker_text)+ ": " + address;
			else
				tickerText = getText(R.string.ticker_text)+ ": Address unknown";
			Notification notification = new Notification(R.drawable.ic_stat_alarm, tickerText, System.currentTimeMillis());
			Intent notificationIntent = new Intent(this, ZonePicker.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			
			CharSequence nt;
			if (address != null)
				nt = getText(R.string.notification_title) + ": " + address;
			else
				nt = getText(R.string.notification_title) + ": Address unknown";
			notification.setLatestEventInfo(this, nt, getText(R.string.notification_message), pendingIntent);
			startForeground(ONGOING_NOTIFICATION, notification);
	      
	      // If we get killed, after returning from here, restart
	      return START_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	      // We don't provide binding, so return null
	      return null;
	  }
	  
	  @Override
	  public void onDestroy() {
		    locationManager.removeUpdates(myLocationListener);
		    if (mMediaPlayer != null) {
	            mMediaPlayer.release();
	            mMediaPlayer = null;
	        }
	  }
	  
	  
	  public class MyLocationListener implements LocationListener {
	        public void onLocationChanged(Location location) {
	        	if (isBetterLocation(location, oldLocation)){
	        		checkLoc(location);
	        		oldLocation = location;
	        	}
	        }
	        public void onStatusChanged(String s, int i, Bundle b) {            
	        }
	        public void onProviderDisabled(String s) {
	        }
	        public void onProviderEnabled(String s) {            
	        }
	    }
	  
	  public void checkLoc(Location location){
//		  oldDistanceLogic(location);
		  distanceLogic(location);

		  if ((loc.distanceTo(location) <= radius) && (!everAlarmed)){
      		final WakeLock wl = AlarmWakeLock.createFullWakeLock(AlarmService.this);
            wl.acquire();
	      		Intent dialogIntent = new Intent(getBaseContext(), Alarm.class);
	      		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	      				//TESTING TODO REMOVE
			      		try
			      		{
			      			Thread.sleep(500); // do nothing
			      		}
			      		catch(InterruptedException e)
			      		{
			      			e.printStackTrace();
			      		}
			    playAudio();
	      		getApplication().startActivity(dialogIntent);
	      		everAlarmed = true;
      		wl.release();
      	}
	  }
	  
	  private void distanceLogic(Location location) {
		  if (Math.round((loc.distanceTo(location)-radius)/1000) != Math.sqrt(MINIMUM_TIME_BETWEEN_UPDATE/1000) && !everAlarmed) {
			  
			  if (Math.round((loc.distanceTo(location)-radius)/1000) != 0){
				  MINIMUM_TIME_BETWEEN_UPDATE = (long) (Math.pow(Math.round((loc.distanceTo(location)-radius)/1000), 2)*1000);
				  
				  //If time gets larger than 12.15min, that is too much.
				  if (MINIMUM_TIME_BETWEEN_UPDATE > 1000 * 27 * 27)
					  MINIMUM_TIME_BETWEEN_UPDATE = 1000 * 27 * 27;

				  makeManager();
			  }
		  }
	  }

	  //Archived in favor of exponential ramp (this one is stepped)
/*	  private void oldDistanceLogic(Location location) {
		  if (loc.distanceTo(location)-radius <= 10000 && loc.distanceTo(location)-radius > 5000 && MINIMUM_TIME_BETWEEN_UPDATE != 60000 && everAlarmed == false) {
			  MINIMUM_TIME_BETWEEN_UPDATE = 60000;
			  makeManager();
			  //Testing
			  Toast.makeText(getApplicationContext(), "Distantce:" + loc.distanceTo(location) + " Radius:" + radius, Toast.LENGTH_SHORT).show();
		  }
		  if (loc.distanceTo(location)-radius <= 5000 && loc.distanceTo(location)-radius > 1000 && MINIMUM_TIME_BETWEEN_UPDATE > 30000 && everAlarmed == false) {
			  MINIMUM_TIME_BETWEEN_UPDATE = 30000;
			  makeManager();
			  //Testing
			  Toast.makeText(getApplicationContext(), "Distantce :" + loc.distanceTo(location) + " Radius:" + radius, Toast.LENGTH_SHORT).show();
		  }
		  if (loc.distanceTo(location)-radius <= 1000 && MINIMUM_TIME_BETWEEN_UPDATE > 1000 && everAlarmed == false) {
			  MINIMUM_TIME_BETWEEN_UPDATE = 1000;
			  makeManager();
			  //Testing
			  Toast.makeText(getApplicationContext(), "Distantce  :" + loc.distanceTo(location) + " Radius:" + radius, Toast.LENGTH_SHORT).show();
		  }
	  }*/
	  
	  private void playAudio() {
	    	try {
	    		
//	    		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); 
//	    		if(alert == null){
//	    	         alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//	    	         if(alert == null){
//	    	             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
//	    	         }
//	    	     }
	    		Uri alert;
	    		
	    		if (tone != null){
	    			alert = tone ;
	    		}
	    		else
	    			alert = null;
	    		
	    		mMediaPlayer = new MediaPlayer();
	    		mMediaPlayer.setDataSource(this, alert);
	    		
	    		final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	    		 if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
	    		            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
	    		            mMediaPlayer.setLooping(true);
	    		            mMediaPlayer.prepare();
	    		            mMediaPlayer.start();
	    		  }

	    		
//	    		mMediaPlayer.start();

	    	} catch (Exception e) {
//	    		Log.e("ex", "error: " + e.getMessage(), e);
	    	}
	    }
	  
	  /** Determines whether one Location reading is better than the current Location fix
	   * @param location  The new Location that you want to evaluate
	   * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	   */
	 protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	     if (currentBestLocation == null) {
	         // A new location is always better than no location
	         return true;
	     }

	     // Check whether the new location fix is newer or older
	     long timeDelta = location.getTime() - currentBestLocation.getTime();
	     boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
	     boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
	     boolean isNewer = timeDelta > 0;

	     // If it's been more than two minutes since the current location, use the new location
	     // because the user has likely moved
	     if (isSignificantlyNewer) {
	         return true;
	     // If the new location is more than two minutes older, it must be worse
	     } else if (isSignificantlyOlder) {
	         return false;
	     }

	     // Check whether the new location fix is more or less accurate
	     int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	     boolean isLessAccurate = accuracyDelta > 0;
	     boolean isMoreAccurate = accuracyDelta < 0;
	     boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	     // Check if the old and new location are from the same provider
	     boolean isFromSameProvider = isSameProvider(location.getProvider(),
	             currentBestLocation.getProvider());

	     // Determine location quality using a combination of timeliness and accuracy
	     if (isMoreAccurate) {
	         return true;
	     } else if (isNewer && !isLessAccurate) {
	         return true;
	     } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	         return true;
	     }
	     return false;
	 }

	 /** Checks whether two providers are the same */
	 private boolean isSameProvider(String provider1, String provider2) {
	     if (provider1 == null) {
	       return provider2 == null;
	     }
	     return provider1.equals(provider2);
	 }
	  
	  private void makeManager() {
		  locationManager.removeUpdates(myLocationListener);
		  locationManager.requestLocationUpdates(
                  LocationManager.GPS_PROVIDER, 
                  MINIMUM_TIME_BETWEEN_UPDATE, 
                  MINIMUM_DISTANCECHANGE_FOR_UPDATE,
                  myLocationListener
		  );
		  locationManager.requestLocationUpdates(
				  LocationManager.NETWORK_PROVIDER, 
                  MINIMUM_TIME_BETWEEN_UPDATE, 
                  MINIMUM_DISTANCECHANGE_FOR_UPDATE,
                  myLocationListener
		  );
	  }
}
