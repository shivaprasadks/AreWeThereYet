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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class DrawerFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    //--Variables for fragment--//
    private View V;

    //--Variables for sharedprefs--//
    private static final String POINT_LATITUDE_KEY = "POINT_LATITUDE_KEY";
    private static final String POINT_LONGITUDE_KEY = "POINT_LONGITUDE_KEY";
    private static final String POINT_RADIUS_KEY = "POINT_RADIUS_KEY";
    private static final String POINT_ADDRESS_KEY = "POINT_ADDRESS_KEY";
    private static final String POINT_SAVED_INDEX = "POINT_SAVED_INDEX";
    private static final String SAVED_LATITUDE_KEY = "SAVED_LATITUDE_KEY";
    private static final String SAVED_LONGITUDE_KEY = "SAVED_LONGITUDE_KEY";
    private static final String SAVED_RADIUS_KEY = "SAVED_RADIUS_KEY";
    private static final String SAVED_ADDRESS_KEY = "SAVED_ADDRESS_KEY";

    private static final String RADIUS = "radius";
    private static final String LOC = "loc";

    //--Variables for setting ring tone--//
    private int Set_Ringtone = 0;
    private Uri uri;

    //--Variable for donations--//
    Object donater;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        V = inflater.inflate(R.layout.drawer, null);
        V.findViewById(R.id.row_title_ringtone).setOnClickListener(ringtoneListener);
        V.findViewById(R.id.row_title_donate).setOnClickListener(donateListener);

//        donater = new Donate(this.getActivity());

        return V;
	}

    @Override
    public void onResume() {
        super.onResume();
        saved();
        recent();
    }

    @Override
    public void onPause() {
        clear();
        super.onPause();
    }

    ////////R-E-C-E-N-T///////////
    private void recent() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        SharedPreferences prefs = getActivity().getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);

        for (int i=1; i<=5; i++){
            Location location = new Location("POINT_LOCATION");
            String address = prefs.getString(POINT_ADDRESS_KEY+i, "");
            location.setLatitude(0);
            location.setLongitude(0);
            if(prefs.contains(POINT_LATITUDE_KEY+i)){
                location.setLatitude(prefs.getFloat(POINT_LATITUDE_KEY+i, 0));
            }
            if(prefs.contains(POINT_LONGITUDE_KEY+i)){
                location.setLongitude(prefs.getFloat(POINT_LONGITUDE_KEY+i, 0));
            }

            LinearLayout RecentParent = (LinearLayout) V.findViewById(R.id.group_recent);
            View Recent = inflater.inflate(R.layout.saved_item, null);
            Recent.setOnClickListener(this);

            CharSequence name;
            if (!address.equals("")){
                name = address;
                ((TextView) Recent.findViewById(R.id.savedLabel)).setTextColor(0xDDFFFFFF);
                ((CompoundButton) Recent.findViewById(R.id.saveCB)).setOnCheckedChangeListener(this);
            }
            else {
                name = "No Recent Alarms";
                ((TextView) Recent.findViewById(R.id.savedLabel)).setTextColor(0xDD999999);
                Recent.findViewById(R.id.saveCB).setEnabled(false);
            }

            ((TextView) Recent.findViewById(R.id.savedLabel)).setText(name);
            ((TextView) Recent.findViewById(R.id.savedLabel)).setTextSize(14);
            Recent.findViewById(R.id.saveCB).setTag(i);
            Recent.setId(i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            RecentParent.setLayoutParams(params);
            RecentParent.addView(Recent);
        }
    }


    private void saved() {
        ////////S-A-V-E-D///////////
        LinearLayout SavedParent = (LinearLayout) V.findViewById(R.id.group_pinned);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //If there are no pinned alarms
        if(touchSaveIndex(0) < 1){
            View Saved = inflater.inflate(R.layout.saved_item, null);
            Saved.setOnClickListener(this);

            ((TextView) Saved.findViewById(R.id.savedLabel)).setText("No Pinned Alarms");
            ((TextView) Saved.findViewById(R.id.savedLabel)).setTextColor(0xDD999999);
            ((CheckBox) Saved.findViewById(R.id.saveCB)).setChecked(true);
            Saved.findViewById(R.id.saveCB).setEnabled(false);

            ((CompoundButton) Saved.findViewById(R.id.saveCB)).setOnCheckedChangeListener(this);

            LinearLayout.LayoutParams sparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            SavedParent.setLayoutParams(sparams);
            SavedParent.addView(Saved);
        }
        else{
            for (int i=1; i<=touchSaveIndex(0); i++){

//                View Saved = new View(getApplicationContext());
                View Saved = inflater.inflate(R.layout.saved_item, null);
                Saved.setOnClickListener(this);

                SharedPreferences prefs = getActivity().getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);
                Location location = new Location("POINT_LOCATION");
                location.setLatitude(0);
                location.setLongitude(0);
                String address = "No Pinned Alarms";
                if(prefs.contains(SAVED_LATITUDE_KEY+i)){
                    location.setLatitude(prefs.getFloat(SAVED_LATITUDE_KEY+i, 0));
                }
                if(prefs.contains(SAVED_LONGITUDE_KEY+i)){
                    location.setLongitude(prefs.getFloat(SAVED_LONGITUDE_KEY+i, 0));
                }
                if(prefs.contains(SAVED_ADDRESS_KEY+i)){
                    address = (prefs.getString(SAVED_ADDRESS_KEY+i, ""));
                }

                CharSequence name;
                if (!address.equals("")){
                    name = address;
                    ((TextView) Saved.findViewById(R.id.savedLabel)).setTextColor(0xDDFFFFFF);
                    ((CheckBox) Saved.findViewById(R.id.saveCB)).setChecked(true);
                    Saved.findViewById(R.id.saveCB).setTag(i);
                    ((CompoundButton) Saved.findViewById(R.id.saveCB)).setOnCheckedChangeListener(this);
                }
                else {
                    name = "Error";
                    ((TextView) Saved.findViewById(R.id.savedLabel)).setTextColor(0xDD999999);
                    ((CheckBox) Saved.findViewById(R.id.saveCB)).setChecked(true);
                    Saved.findViewById(R.id.saveCB).setEnabled(false);
                }


                ((CompoundButton) Saved.findViewById(R.id.saveCB)).setOnCheckedChangeListener(this);
                ((TextView) Saved.findViewById(R.id.savedLabel)).setText(name);
                ((TextView) Saved.findViewById(R.id.savedLabel)).setTextSize(14);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                SavedParent.setLayoutParams(params);
                SavedParent.addView(Saved);

            }
        }
    }

    //Store Saved item
    private void setSaved(int index) {
        SharedPreferences prefs = getActivity().getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Location location = new Location("POINT_LOCATION");
        location.setLatitude(0);
        location.setLongitude(0);
        int setRad = 0;
        String setAddress = "";
        if(prefs.contains(POINT_LATITUDE_KEY+index)){
            location.setLatitude(prefs.getFloat(POINT_LATITUDE_KEY+index, 0));
        }
        if(prefs.contains(POINT_LONGITUDE_KEY+index)){
            location.setLongitude(prefs.getFloat(POINT_LONGITUDE_KEY+index, 0));
        }
        if(prefs.contains(POINT_RADIUS_KEY+index)){
            setRad = prefs.getInt(POINT_RADIUS_KEY+index, 0);
        }
        if(prefs.contains(POINT_ADDRESS_KEY+index)){
            setAddress = prefs.getString(POINT_ADDRESS_KEY+index, "");
        }

        touchSaveIndex(1);

        float setLat = (float) location.getLatitude();
        float setLong = (float) location.getLongitude();
        prefsEditor.putFloat(SAVED_LATITUDE_KEY + touchSaveIndex(0), setLat);
        prefsEditor.putFloat(SAVED_LONGITUDE_KEY + touchSaveIndex(0), setLong);
        prefsEditor.putInt(SAVED_RADIUS_KEY + touchSaveIndex(0), setRad);
        prefsEditor.putString(SAVED_ADDRESS_KEY + touchSaveIndex(0), setAddress);
        prefsEditor.commit();
    }

    private int touchSaveIndex(int mod) {
        SharedPreferences prefs = getActivity().getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(POINT_SAVED_INDEX, (prefs.getInt(POINT_SAVED_INDEX, 0) + mod));
        prefsEditor.commit();
        return prefs.getInt(POINT_SAVED_INDEX, 0);
    }

    private View.OnClickListener ringtoneListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            initTone();
            ringtonepicker();
        }
    };

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

    protected void ringtonepicker() {
        Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_ALARM);
        intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        if( uri != null)
        {
            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
        }
        else
        {
            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri)null);
        }
        startActivityForResult( intent, Set_Ringtone);
        SlidingFragmentActivity sfa = (SlidingFragmentActivity) getActivity();
        sfa.showContent();
    }

    private View.OnClickListener donateListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            V.findViewById(R.id.row_title_donate).setClickable(false);
//            ((Donate) donater).donate();
        }
    };

    @Override
    public void onClick(View v) {
        SharedPreferences prefs = getActivity().getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);
        Intent intent = getActivity().getIntent();
        intent.setClass(getActivity(), ZonePicker.class);
        ZonePicker sfa = (ZonePicker) getActivity();
        switch (((View) v.getParent()).getId()){
            case R.id.group_pinned:
                intent.putExtra(RADIUS, prefs.getInt(SAVED_RADIUS_KEY + v.findViewById(R.id.saveCB).getTag(), 2000));
                intent.putExtra(LOC, retrieveSaved((Integer) v.findViewById(R.id.saveCB).getTag()));
                getActivity().setResult(Activity.RESULT_OK, intent);
                sfa.setNewAlarmZone(intent.getExtras());
                sfa.showContent();
                break;
            case R.id.group_recent:
                intent.putExtra(RADIUS, prefs.getInt(POINT_RADIUS_KEY + v.findViewById(R.id.saveCB).getTag(), 2000));
                intent.putExtra(LOC, retrieveRecent((Integer) v.findViewById(R.id.saveCB).getTag()));
                getActivity().setResult(Activity.RESULT_OK, intent);
                sfa.setNewAlarmZone(intent.getExtras());
                sfa.showContent();
                break;
        }
    }

    private Location retrieveRecent(int index) {
        SharedPreferences prefs = getActivity().getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);
        Location location = new Location("POINT_LOCATION");
        location.setLatitude(0);
        location.setLongitude(0);
        if(prefs.contains(POINT_LATITUDE_KEY+index)){
            location.setLatitude(prefs.getFloat(POINT_LATITUDE_KEY+index, 0));
        }
        if(prefs.contains(POINT_LONGITUDE_KEY+index)){
            location.setLongitude(prefs.getFloat(POINT_LONGITUDE_KEY+index, 0));
        }
        return location;
    }

    private Location retrieveSaved(int index) {
        SharedPreferences prefs = getActivity().getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);
        Location location = new Location("POINT_LOCATION");
        location.setLatitude(0);
        location.setLongitude(0);
        if(prefs.contains(SAVED_LATITUDE_KEY+index)){
            location.setLatitude(prefs.getFloat(SAVED_LATITUDE_KEY+index, 0));
        }
        if(prefs.contains(SAVED_LONGITUDE_KEY+index)){
            location.setLongitude(prefs.getFloat(SAVED_LONGITUDE_KEY+index, 0));
        }
        return location;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            View recentV = (View) buttonView.getParent();
            setSaved(recentV.getId());
        }
        else{
            cleanup((Integer) buttonView.getTag());
            touchSaveIndex(-1);
        }
        clear();
        saved();
        recent();
        LinearLayout SavedParent = (LinearLayout) V.findViewById(R.id.group_pinned);
        LinearLayout RecentParent = (LinearLayout) V.findViewById(R.id.group_recent);
        SavedParent.invalidate();
        RecentParent.invalidate();
    }


    private void clear() {
        LinearLayout SavedParent = (LinearLayout) V.findViewById(R.id.group_pinned);
        LinearLayout RecentParent = (LinearLayout) V.findViewById(R.id.group_recent);
        SavedParent.removeAllViews();
        RecentParent.removeAllViews();
    }

    private void cleanup(int tag) {
        SharedPreferences prefs = getActivity().getSharedPreferences("AreWeThereYet", Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.remove(SAVED_LATITUDE_KEY + tag);
        prefsEditor.remove(SAVED_LONGITUDE_KEY + tag);
        prefsEditor.remove(SAVED_RADIUS_KEY + tag);
        prefsEditor.remove(SAVED_ADDRESS_KEY + tag);

        for (int i = tag; i < touchSaveIndex(0); i++){
            prefsEditor.putFloat(SAVED_LATITUDE_KEY + i, prefs.getFloat(SAVED_LATITUDE_KEY + (i+1), 0));
            prefsEditor.putFloat(SAVED_LONGITUDE_KEY + i, prefs.getFloat(SAVED_LONGITUDE_KEY + (i+1), 0));
            prefsEditor.putString(SAVED_ADDRESS_KEY + i, prefs.getString(SAVED_ADDRESS_KEY + (i+1), ""));
        }
        prefsEditor.remove(SAVED_LATITUDE_KEY + touchSaveIndex(0));
        prefsEditor.remove(SAVED_LONGITUDE_KEY + touchSaveIndex(0));
        prefsEditor.remove(SAVED_ADDRESS_KEY + touchSaveIndex(0));

        prefsEditor.commit();
    }

    @Override
    public void onDestroy() {
//        ((Donate) donater).shutdown();
        super.onDestroy();
    }
}
