package org.risney.cache.android;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.risney.cache.lib.ImageCache;
import org.risney.cache.lib.policies.EvictionPolicy;
import org.risney.cache.lib.utils.ConversionUtils;
import org.risney.spotify.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Setting values
    private int MAX_SEARCH_RESULTS;
    private int MAX_CACHE_IMAGES;
    private int MAX_CACHE_BYTES;
    private String EVICTION_POLICY;

    private ImageCache imageCache;
    private SharedPreferences sharedPref;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private GridViewAdapter gridViewAdapter;
    private GridView gridView;
    private List<Image> images = new ArrayList<Image>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setTitle("");

        gridViewAdapter = new GridViewAdapter(this);

        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(gridViewAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


                final Image image = (Image) v.getTag();
                if (null == imageCache.get(image.getKey())) {
                    imageCache.putIfAbsent(image.getKey(), ConversionUtils.bitmapToByteBuffer(image.getBitmap()));
                }

                Log.d(TAG, "size of image = " + image.getBitmap().getByteCount());

                gridView.startLayoutAnimation();
                gridViewAdapter.notifyDataSetChanged();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] bytes = stream.toByteArray();
                        intent.putExtra("bitmapbytes", bytes);
                        intent.putExtra("src",image.getSrc());
                        intent.putExtra("size",image.getBitmap().getByteCount());
                        intent.putExtra("cached",image.isCached());
                        startActivity(intent);
                    }
                }, 300);
            }
        });

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                reConfigureImageCache(sharedPreferences, key);
            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(listener);

        MAX_SEARCH_RESULTS = sharedPref.getInt("MAX_SEARCH_RESULTS", 33);
        MAX_CACHE_IMAGES = sharedPref.getInt("MAX_CACHE_IMAGES", 33);
        MAX_CACHE_BYTES = sharedPref.getInt("MAX_CACHE_BYTES", 1024 * 1024);
        EVICTION_POLICY = sharedPref.getString("EVICTION_POLICY", "LRU");

        imageCache = new ImageCache.Builder(EvictionPolicy.valueOf(EVICTION_POLICY))
                .maxBytes(MAX_CACHE_BYTES)
                .maxImages(MAX_CACHE_IMAGES)
                .build();

        popUp();
    }

    private void reConfigureImageCache(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, key + " setting changed ...");

        switch (key) {
            case "MAX_SEARCH_RESULTS":
                MAX_SEARCH_RESULTS = sharedPreferences.getInt("MAX_SEARCH_RESULTS", 33);
                Log.d(TAG, "new max search results = " + MAX_SEARCH_RESULTS);
                break;
            case "EVICTION_POLICY":
                EVICTION_POLICY = sharedPreferences.getString("EVICTION_POLICY", "LRU");
                Log.d(TAG, "new eviction policy = " + EVICTION_POLICY);
                break;
            case "MAX_CACHE_IMAGES":
                MAX_CACHE_IMAGES = sharedPreferences.getInt("MAX_CACHE_IMAGES", 33);
                Log.d(TAG, "new max cache images = " + MAX_CACHE_IMAGES);
                break;
            case "MAX_CACHE_KBYTES":
                MAX_CACHE_BYTES = sharedPreferences.getInt("MAX_CACHE_KBYTES", 1024) * 1024;
                Log.d(TAG, "new max cache size = " + MAX_CACHE_BYTES);
                break;
            default:
                Log.d(TAG, "unknown setting changed");
        }

        imageCache = new ImageCache.Builder(EvictionPolicy.valueOf(EVICTION_POLICY))
                .maxBytes(MAX_CACHE_BYTES)
                .maxImages(MAX_CACHE_IMAGES)
                .build();
        images.clear();

    }

    private void popUp() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup, (ViewGroup) findViewById(R.id.like_popup_iv));
        ImageView imageView = (ImageView) layout.findViewById(R.id.like_popup_iv);
        imageView.setBackgroundResource(R.drawable.spotify);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 150);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    /**
     * React to the user tapping/selecting an options menu item for settings
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set up the application bar, primarily as a search bar
     * using Google Image Search, and JSoup to retrieve an image
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);


        // Search input on app bar.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Search input listener
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

            public boolean onQueryTextChange(String newText) {
                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getApplicationContext(), "Searching images for '" + query + "'", Toast.LENGTH_LONG).show();
                images.clear();
                SearchTaskParams taskParams = new SearchTaskParams(query, MAX_SEARCH_RESULTS);
                new SearchTask().execute(taskParams);
                searchView.clearFocus();
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }


    // Async images search task
    private static class SearchTaskParams {
        String query;
        int maxSearchResults;

        SearchTaskParams(String query, int maxSearchResults) {
            this.query = query;
            this.maxSearchResults = maxSearchResults;
        }
    }

    private class GridViewAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public GridViewAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int i) {
            return images.get(i);
        }

        @Override
        public long getItemId(int i) {
            //return items.get(i).drawableId;
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View v = view;
            ImageView picture;
            TextView name;


            if (v == null) {
                v = inflater.inflate(R.layout.gridview_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }

            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);

            Image image = (Image) getItem(i);
            picture.setImageBitmap(image.getBitmap());

            if (null != imageCache.get(image.getKey())) {
                name.setText("cached");
                image.setCached(true);
            } else {
                name.setText("non-cached");
                image.setCached(false);
            }

            v.setTag(image);
            return v;
        }
    }

    private class SearchTask extends AsyncTask<SearchTaskParams, Void, Void> {

        List<String> srcLinks;

        @Override
        protected void onPreExecute() {
            srcLinks = new ArrayList<String>();
        }

        @Override
        protected Void doInBackground(SearchTaskParams... params) {
            srcLinks = GoogleImageSearch.findImages(params[0].query, params[0].maxSearchResults);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            images.clear();
            for (String src : srcLinks) {
                new GetImageTask().execute(src);
            }
        }

    }


    private class Wrapper {
        public String src;
        public ByteBuffer key;
        public Bitmap image;

    }

    // Get images asynchronously
    private class GetImageTask extends AsyncTask<String, Void, Wrapper> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Wrapper doInBackground(String... URL) {

            String imageURL = URL[0];
            Bitmap bitmap = null;
            Wrapper wrapper = new Wrapper();
            wrapper.src = imageURL;
            try {

                ByteBuffer key = ConversionUtils.stringToByteBuffer(imageURL);
                if (imageCache.containsKey(key)) {
                    ByteBuffer byteBuffer = imageCache.get(key);
                    bitmap = ConversionUtils.byteBufferToBitmap(byteBuffer);
                    Log.d(TAG, "found image in cache for key : " + imageURL);
                } else {
                    InputStream is = new java.net.URL(imageURL).openStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    ByteBuffer value = ConversionUtils.bitmapToByteBuffer(bitmap);
                    imageCache.putIfAbsent(key, value);
                    Log.d(TAG, "putting image in cache for key : " + imageURL);
                }
                wrapper.key = key;
                wrapper.image = bitmap;
            } catch (Exception e) {
                Log.e(TAG, "error downloading images " + e.toString());
            }
            return wrapper;
        }

        @Override
        protected void onPostExecute(Wrapper wrapper) {
            // Set the bitmap into ImageView
            images.add(new Image(wrapper.src, wrapper.key, wrapper.image));
            gridViewAdapter.notifyDataSetChanged();
        }
    }
}


