package org.risney.spotify;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by marcrisney on 6/19/16.
 */

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = PreferenceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call super :
        super.onCreate(savedInstanceState);

        // Set the activity's fragment :
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }


    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        private ListPreference evictionPoliciesPref;
        private SliderBarPreference maxSearchResultsPref;
        private SliderBarPreference maxCacheImagesPref;
        private SliderBarPreference maxCacheSizePref;


        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // Get widgets :
            evictionPoliciesPref = (ListPreference) this.findPreference("EVICTION_POLICY");
            maxSearchResultsPref = (SliderBarPreference) this.findPreference("MAX_SEARCH_RESULTS");
            maxCacheImagesPref = (SliderBarPreference) this.findPreference("MAX_CACHE_IMAGES");
            maxCacheSizePref = (SliderBarPreference) this.findPreference("MAX_CACHE_SIZE");

            // Set listener :
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            // Set maximum search results summary :
            try {
                int maxSearchResults = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_SEARCH_RESULTS", 33);
                maxSearchResultsPref.setSummary(this.getString(R.string.max_search_results_summary).replace("$1", "" + maxSearchResults));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // Set eviction policy  :
            try {
                String evictionPolicy = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("EVICTION_POLICY", "LRU");
                evictionPoliciesPref.setSummary(this.getString(R.string.cache_algorithm_summary).replace("$2", "" + evictionPolicy));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // Set maximum cache images :
            try {
                int maxCacheImages = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_CACHE_IMAGES", 33);
                maxCacheImagesPref.setSummary(this.getString(R.string.number_of_images_summary).replace("$3", "" + maxCacheImages));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // Set maximum cache size :
            try {
                int maxCacheSize = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_CACHE_SIZE", 1024);
                maxCacheSizePref.setSummary(this.getString(R.string.size_of_cache_summary).replace("$4", "" + maxCacheSize));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            // Set maximum search results summary :
            try {
                int maxSearchResults = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_SEARCH_RESULTS", 33);
                maxSearchResultsPref.setSummary(this.getString(R.string.max_search_results_summary).replace("$1", "" + maxSearchResults));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // Set eviction policy  :
            try {
                String evictionPolicy = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("EVICTION_POLICY", "LRU");
                evictionPoliciesPref.setSummary(this.getString(R.string.cache_algorithm_summary).replace("$2", "" + evictionPolicy));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // Set maximum cache images :
            try {

                int maxCacheImages = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_CACHE_IMAGES", 33);
                maxCacheImagesPref.setSummary(this.getString(R.string.number_of_images_summary).replace("$3", "" + maxCacheImages));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // Set maximum cache size :
            try {
                int maxCacheSize = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("MAX_CACHE_SIZE", 33);
                maxCacheSizePref.setSummary(this.getString(R.string.size_of_cache_summary).replace("$4", "" + maxCacheSize));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}