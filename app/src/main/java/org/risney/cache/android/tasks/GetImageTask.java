package org.risney.cache.android.tasks;

/**
 * Created by marcrisney on 6/25/16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.risney.cache.android.adapter.GridViewAdapter;
import org.risney.cache.android.model.Image;
import org.risney.cache.lib.ImageCache;
import org.risney.cache.lib.utils.ConversionUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class GetImageTask extends AsyncTask<String, Void, GetImageTask.Parameters> {
    private static final String TAG = GetImageTask.class.getSimpleName();
    private Context context;
    private GridViewAdapter gridViewAdapter;
    private ImageCache imageCache;
    private List<Image> imageList;

    public GetImageTask(Context context, GridViewAdapter gridViewAdapter, ImageCache imageCache, List<Image> imageList){
        this.context = context;
        this.gridViewAdapter = gridViewAdapter;
        this.imageCache = imageCache;
        this.imageList = imageList;

    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Parameters doInBackground(String... URL) {

        String imageURL = URL[0];
        Bitmap bitmap = null;
        Parameters params = new Parameters();
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
        protected void onPostExecute(GetImageTask.Parameters params) {
            boolean cached = imageCache.containsKey(params.key) ? true : false;
            imageList.add(new Image(imageList.size() + 1, params.src, params.key, params.value, params.image, cached, params.contentLength));
            gridViewAdapter.notifyDataSetChanged();
        }



    public class Parameters {
        String src;
        ByteBuffer key;
        ByteBuffer value;
        Bitmap image;
        int contentLength;
    }
}