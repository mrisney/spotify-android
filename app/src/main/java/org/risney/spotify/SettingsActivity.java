package org.risney.spotify;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by marcrisney on 6/19/16.
 */

public class SettingsActivity extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call super :
        super.onCreate(savedInstanceState);

        // Set the activity's fragment :
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }


    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {


        private SliderBarPreference maxSearchResultsPref;
        private SliderBarPreference maxCacheImagesPref;


        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // Get widgets :
            maxSearchResultsPref = (SliderBarPreference) this.findPreference("MAX_SEARCH_RESULTS");
            maxCacheImagesPref = (SliderBarPreference) this.findPreference("MAX_CACHE_IMAGES");

            // Set listener :
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            // Set maximum search results summary :
            int maxSearchResults = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_SEARCH_RESULTS", 33);
            maxSearchResultsPref.setSummary(this.getString(R.string.max_search_results_summary).replace("$1", "" + maxSearchResults));

            // Set maximum cache images :
            int maxCacheImages = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_CACHE_IMAGES", 33);
            maxCacheImagesPref.setSummary(this.getString(R.string.number_of_images_summary).replace("$2", "" + maxCacheImages));

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            // Set maximum search results summary :
            int maxSearchResults = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_SEARCH_RESULTS", 33);
            maxSearchResultsPref.setSummary(this.getString(R.string.max_search_results_summary).replace("$1", "" + maxSearchResults));

            // Set maximum cache images :
            int maxCacheImages = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_CACHE_IMAGES", 33);
            maxCacheImagesPref.setSummary(this.getString(R.string.number_of_images_summary).replace("$2", "" + maxCacheImages));

        }
    }
}