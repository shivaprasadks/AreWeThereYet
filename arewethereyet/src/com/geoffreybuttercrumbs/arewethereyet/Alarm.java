package com.geoffreybuttercrumbs.arewethereyet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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

public class Alarm extends Activity {
	private MediaPlayer mMediaPlayer;
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	setContentView(R.layout.alarm_dialog);
    	
    	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    	PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My Tag");
    	wl.acquire();
    	
        Button okB = (Button)findViewById(R.id.ok);
        okB.setOnClickListener(okListener);

   	 	wl.release();
    }
    
	private OnClickListener okListener = new OnClickListener()
	{
		public void onClick(View v)
		{
    		finish();
		}
	};
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();

        stopService(new Intent(Alarm.this, AlarmService.class));
    }
    
    
 // you cannot start animations in the onCreate() method because the assets
	// haven't loaded yet, hence the reason we check for this in the
	// onWindowFocusChanged method
	public void onWindowFocusChanged(boolean hasFocus) {
		startAnimation();
	}

	// this function is called to start playing the animation since it has to be
	// done in code
	public void startAnimation() {
		ImageView i;
		
		i = (ImageView) findViewById(R.id.spinner);
		if (i != null) {
			AnimationDrawable loadingAnim = (AnimationDrawable) i.getDrawable();
			loadingAnim.start();
		}
	}
}