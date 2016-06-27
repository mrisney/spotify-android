package org.risney.cache.android.tasks;

import android.os.AsyncTask;

import org.risney.cache.android.utils.GoogleImageSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marcrisney on 6/25/16.
 */


public class SearchTask extends AsyncTask<SearchTask.Parameters, Void, Void> {

    List<String> srcLinks;

    @Override
    protected void onPreExecute() {
        srcLinks = new ArrayList<String>();
    }

    @Override
    protected Void doInBackground(SearchTask.Parameters... params) {
        srcLinks = GoogleImageSearch.findImages(params[0].query, params[0].maxSearchResults);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        for (String src : srcLinks) {
            new GetImageTask(null,null,null,null).execute(src);
        }
    }

    public class Parameters {
        String query;
        int maxSearchResults;
        public Parameters(String query,int maxSearchResults ){
            this.query = query;
            this.maxSearchResults = maxSearchResults;
        }
    }
}