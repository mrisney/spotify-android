package org.risney.cache.android;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.risney.cache.android.intro.CacheIntro;
import org.risney.cache.android.model.Image;
import org.risney.cache.android.utils.GoogleImageSearch;
import org.risney.cache.lib.ImageCache;
import org.risney.cache.lib.policies.EvictionPolicy;
import org.risney.cache.lib.utils.ConversionUtils;
import org.risney.spotify.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Primary cache  settings
    private int MAX_SEARCH_RESULTS;
    private int MAX_CACHE_IMAGES;
    private int MAX_CACHE_BYTES;
    private String EVICTION_POLICY;

    private ImageCache imageCache;
    private SharedPreferences sharedPref;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private GridViewAdapter gridViewAdapter;
    private GridView gridView;
    private List<Image> images = new LinkedList<Image>();
    private TextView cacheInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        cacheInfoTextView = (TextView) findViewById(R.id.text);
        gridViewAdapter = new GridViewAdapter(this);

        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnTouchListener(new View.OnTouchListener() {
            private View touchedView = null;
            private GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    Image image = (Image) touchedView.getTag();
                    Log.d(TAG, "Double tapped image id " + image.getId());
                    try {
                        Intent intent = new Intent(MainActivity.this, ViewImageActivity.class);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        if (image.isCached()) {
                            ByteBuffer value = imageCache.get(image.getKey());
                            baos.write(value.array());
                        } else {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] bytes = stream.toByteArray();
                            baos.write(bytes);
                        }
                        intent.putExtra("IMAGE_BYTES", baos.toByteArray());
                        intent.putExtra("src", image.getSrc());
                        intent.putExtra("size", baos.size());
                        intent.putExtra("cached", image.isCached());
                        startActivity(intent);

                    } catch (Exception ex) {

                    }

                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    Image image = (Image) touchedView.getTag();
                    Log.d(TAG, "Single tapped image id " + image.getId());
                    if (null == imageCache.get(image.getKey())) {
                        Toast.makeText(getApplicationContext(), "Adding image to cache", Toast.LENGTH_SHORT).show();
                        imageCache.put(image.getKey(), image.getValue());
                        // since not downloading images, but adding a bitmap into cache, update the image cache info
                        syncImageCacheInfo();

                    }
                    gridView.startLayoutAnimation();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            gridViewAdapter.notifyDataSetChanged();
                        }
                    }, 500);
                    return super.onSingleTapConfirmed(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int position = gridView.pointToPosition((int) event.getX(), (int) event.getY());
                touchedView = gridView.getChildAt(position);
                gestureDetector.onTouchEvent(event);
                return true;
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

        initCache();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {


                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = sharedPref.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, CacheIntro.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = sharedPref.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

    }

    private void initCache() {
        imageCache = new ImageCache.Builder(EvictionPolicy.valueOf(EVICTION_POLICY))
                .maxBytes(MAX_CACHE_BYTES)
                .maxImages(MAX_CACHE_IMAGES)
                .build();

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

        initCache();
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
                cacheInfoTextView.setText("");
                images.clear();
                imageCache.clear();
                SearchTaskParams taskParams = new SearchTaskParams(query, MAX_SEARCH_RESULTS);
                new SearchTask().execute(taskParams);
                searchView.clearFocus();
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     *
     */
    private void syncImageCacheInfo() {
        for (Image image : images) {
            if (imageCache.containsKey(image.getKey())) {
                image.setCached(true);
            } else {
                image.setCached(false);
            }
        }
        String cacheSize = android.text.format.Formatter.formatFileSize(MainActivity.this, imageCache.getTotalCacheSize());
        long entries = imageCache.getEntryCount();
        cacheInfoTextView.setText("current cache size : " + cacheSize + ", entries : " + entries);
    }

    /**
     * Async images search task parameters
     */
    private class SearchTaskParams {
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
            try {
                return images.get(i).getId();
            } catch (Exception e) {
                return 0;
            }
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

            // before preparing view, do an update on which images are cached, and which are not
            syncImageCacheInfo();

            Image image = (Image) getItem(i);
            name = (TextView) v.getTag(R.id.text);
            String fileSize = android.text.format.Formatter.formatFileSize(MainActivity.this, image.getByteCount());
            if (image.isCached()) {
                name.setText("cached \n" + fileSize);
            } else {
                name.setText("non-cached \n" + fileSize);
            }
            picture = (ImageView) v.getTag(R.id.picture);
            picture.setImageBitmap(image.getBitmap());
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


    private class GetImageTaskParams {
        public String src;
        public ByteBuffer key;
        public ByteBuffer value;
        public Bitmap image;
        public int contentLength;
    }

    // Get images asynchronously
    private class GetImageTask extends AsyncTask<String, Void, GetImageTaskParams> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected GetImageTaskParams doInBackground(String... URL) {

            String imageURL = URL[0];
            Bitmap bitmap = null;
            GetImageTaskParams params = new GetImageTaskParams();
            params.src = imageURL;

            try {

                ByteBuffer key = ConversionUtils.stringToByteBuffer(imageURL);
                if (imageCache.containsKey(key)) {
                    ByteBuffer byteBuffer = imageCache.get(key);
                    bitmap = ConversionUtils.byteBufferToBitmap(byteBuffer);
                    params.contentLength = byteBuffer.capacity();
                    params.value = byteBuffer;
                    Log.d(TAG, "Retrieved image from cache for key : " + imageURL);

                } else {
                    java.net.URL imageContent = new java.net.URL(imageURL);
                    params.contentLength = imageContent.openConnection().getContentLength();

                    InputStream is = imageContent.openStream();
                    byte[] bytes = IOUtils.toByteArray(is);

                    params.value = ByteBuffer.wrap(bytes);
                    bitmap = ConversionUtils.byteBufferToBitmap(params.value);

                    imageCache.put(key, params.value);
                    Log.d(TAG, "Putting image in cache for key : " + imageURL);
                }
                params.key = key;
                params.image = bitmap;
            } catch (Exception e) {
                Log.e(TAG, "Error downloading images " + e.toString());
            }
            return params;
        }

        @Override
        protected void onPostExecute(GetImageTaskParams params) {
            boolean cached = imageCache.containsKey(params.key) ? true : false;
            images.add(new Image(images.size() + 1, params.src, params.key, params.value, params.image, cached, params.contentLength));
            gridViewAdapter.notifyDataSetChanged();
        }
    }
}


