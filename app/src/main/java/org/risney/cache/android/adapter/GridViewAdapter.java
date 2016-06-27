package org.risney.cache.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.risney.cache.android.model.Image;
import org.risney.cache.lib.ImageCache;
import org.risney.spotify.R;

import java.util.List;

/**
 * Created by marcrisney on 6/25/16.
 */
public class GridViewAdapter extends BaseAdapter {

    private static final String TAG = GridViewAdapter.class.getSimpleName();
    private Context context;
    private ImageCache imageCache;
    LayoutInflater inflater;
    List<Image> imageList;

    public GridViewAdapter(Context context, ImageCache imageCache, List<Image> imageList) {
        this.context = context;
        this.imageCache = imageCache;
        this.imageList = imageList;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int i) {
        return imageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        try {
            return imageList.get(i).getId();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View v = view;
        ImageView picture;
        TextView name;

        Log.d(TAG,"adding image to gridview");
        if (v == null) {
            v = inflater.inflate(R.layout.gridview_item, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));
        }

        // before preparing view, do an update on which images are cached, and which are not
        syncImageCacheInfo();

        Image image = (Image) getItem(i);
        name = (TextView) v.getTag(R.id.text);
        String fileSize = android.text.format.Formatter.formatFileSize(context, image.getByteCount());
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

    private void syncImageCacheInfo() {
        for (Image image : imageList) {
            if (imageCache.containsKey(image.getKey())) {
                image.setCached(true);
            } else {
                image.setCached(false);
            }
        }
        String cacheSize = android.text.format.Formatter.formatFileSize(context, imageCache.getTotalCacheSize());
        long entries = imageCache.getEntryCount();
        //cacheIn
    }
}