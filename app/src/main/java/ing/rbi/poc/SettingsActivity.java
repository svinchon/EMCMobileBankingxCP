/** -------------------------------------------------------------------------
* Copyright  2013 EMC Corporation.  All rights reserved.
---------------------------------------------------------------------------- */

package ing.rbi.poc;

import emc.captiva.mobile.sdk.CaptureImage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * This class handles managing all of the global preferences.
 */

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    /* (non-Javadoc)
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
     */
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        
        // Make sure our preferences conform to our allowed values.
        @SuppressWarnings("deprecation")
        Preference p = findPreference(key);
        if (p instanceof EditTextPreference) {
            EditTextPreference pref = (EditTextPreference)p;            
            String temp = "";
            Integer i = 0;
            Float f = 0.0f;
            if (key.compareToIgnoreCase("xCP BPS URI Address") == 0) {
            	//The URI has been changed
            	temp = pref.getText();
            	pref.setText(temp);
            }
            if (key.compareToIgnoreCase("Results URI Address") == 0) {
            	//The URI has been changed
            	temp = pref.getText();
            	pref.setText(temp);
            }
            if (key.compareToIgnoreCase("URI Address") == 0) {
            	//The URI has been changed
            	temp = pref.getText();
            	pref.setText(temp);
            }
            if (key.compareToIgnoreCase("Default Customer Name") == 0) {
            	//The Default Customer Name has been changed
            	temp = pref.getText();
            	pref.setText(temp);
            }
            if (key.compareToIgnoreCase("Default Account Name") == 0) {
            	//The Default Customer Name has been changed
            	temp = pref.getText();
            	pref.setText(temp);
            }
            if (key.compareToIgnoreCase("GPREF_SENSOR_LIGHT_VALUE") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 50);
                i = Math.max(0, i);
                i = Math.min(5000, i);
                pref.setText(i.toString());
            }
            
            if (key.compareToIgnoreCase("GPREF_SENSOR_MOTION_VALUE") == 0) {
                temp = pref.getText();
                f = CoreHelper.getFloat(temp, .30f);
                f = Math.max(0.0f, f);
                f = Math.min(10.0f, f);
                pref.setText(f.toString());
            }
            
            if (key.compareToIgnoreCase("GPREF_CAPTUREDELAY") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 500);
                i = Math.max(0, i);
                pref.setText(i.toString());
            }
            
            if (key.compareToIgnoreCase("GPREF_CAPTURETIMEOUT") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 0);
                i = Math.max(0, i);
                pref.setText(i.toString());
            }
            
            if (key.compareToIgnoreCase("GPREF_IMAGEFORMAT") == 0) {
                temp = pref.getText();
                if (temp == null || temp.isEmpty()) {
                    pref.setText(CaptureImage.SAVE_JPG);
                } 
                else if (temp.compareToIgnoreCase(CaptureImage.SAVE_JPG) != 0 && 
                		temp.compareToIgnoreCase(CaptureImage.SAVE_PNG) != 0 && 
                		temp.compareToIgnoreCase(CaptureImage.SAVE_TIF) != 0) {
                    pref.setText(CaptureImage.SAVE_JPG);
                }
            }
            
            if (key.compareToIgnoreCase("GPREF_JPGQUALITY") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 95);
                if (i < 0 || i > 100) {
                    pref.setText("95");
                } else {
                    pref.setText(i.toString());
                }
            }
            
            if (key.compareToIgnoreCase("GPREF_DPIX") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 0);
                if (i <= 0) {
                    pref.setText("");
                }                
            }

            if (key.compareToIgnoreCase("GPREF_DPIY") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 0);
                if (i <= 0) {
                    pref.setText("");
                }
            }
            
            if (key.compareToIgnoreCase("GPREF_FILTER_CROP_PADDING") == 0) {
            	temp = pref.getText();
                f = CoreHelper.getFloat(temp, 0.0f);
                f = Math.max(0.0f, f);
                f = Math.min(1.0f, f);
                pref.setText(f.toString());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        addPreferencesFromResource(ing.rbi.poc.R.xml.preferences);
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        
        // Listen for changes.
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        
        // Cancel listening for changes.
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
