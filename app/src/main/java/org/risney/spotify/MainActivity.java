package org.risney.spotify;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private GridViewAdapter gridViewAdapter;
    private GridView gridView;
    private List<Item> items = new ArrayList<Item>();
    private int imageCount;
    private SharedPreferences sharedPref;

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

                Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
                startActivity(intent);
            }
        });

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }


    /**
     * react to the user tapping/selecting an options menu item
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

                imageCount = 0;
                items.clear();

                int searchResults = sharedPref.getInt("MAX_SEARCH_RESULTS", 33);
                SearchTaskParams taskParams = new SearchTaskParams(query, searchResults);

                new SearchTask().execute(taskParams);

                searchView.clearFocus();

                return false;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    // Async Search Images Task
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
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
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

            Item item = (Item) getItem(i);
            picture.setImageBitmap(item.bitmap);
            name.setText(item.name);

            return v;
        }
    }

    private class Item {
        final String name;
        final Bitmap bitmap;

        Item(String name, Bitmap bitmap) {
            this.name = name;
            this.bitmap = bitmap;
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
            for (String src : srcLinks) {
                new DownloadImagTask().execute(src);
            }
        }

    }

    // DownloadImage AsyncTask
    private class DownloadImagTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            imageCount++;
            items.add(new Item("Image " + imageCount, result));
            Log.d(TAG, "finished downloading image " + imageCount);
            gridViewAdapter.notifyDataSetChanged();
        }
    }
}


