package org.risney.cache.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.risney.spotify.R;

/**
 *
 * Simple Image View Activity, shows details of image, src, size in bytes
 *
 *
 * @author marc.risney@gmail.com
 * @version 1.0
 * @since 2016-06-20
 *
 */


public class ViewImageActivity extends Activity {
    private static final String TAG = ViewImageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        byte[] bytes = getIntent().getByteArrayExtra("IMAGE_BYTES");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        ImageView image = (ImageView) findViewById(R.id.imageView);

        image.setImageBitmap(getScaledBitmap(bitmap));


/*
        TextView textView = (TextView) findViewById(R.id.text);
        String src = getIntent().getStringExtra("src");
        if (src != null) {
            String html = "Link to Google Image Search result <a href=\"" +src+"\">here</a>";
            textView.setText(Html.fromHtml(html));
        }
*/

        TextView textView = (TextView) findViewById(R.id.text);
        StringBuffer sb = new StringBuffer();

        Boolean cached = getIntent().getBooleanExtra("cached",false);
        Log.d(TAG,"cached = "+cached);

        String imageSize =  android.text.format.Formatter.formatFileSize(ViewImageActivity.this, bytes.length);

        sb.append(imageSize);
        sb.append("\n");
        sb.append("Cached = "+cached);

        textView.setText(sb.toString());

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String evictionPolicy = sharedPref.getString("EVICTION_POLICY", "LRU");

    }

    private Bitmap getScaledBitmap(Bitmap bitmap) {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int width = displaymetrics.widthPixels;
        float scaleFactor = (float) width / (float) bitmap.getWidth();
        int newHeight = (int) (bitmap.getHeight() * scaleFactor);

        return Bitmap.createScaledBitmap(bitmap, width, newHeight, true);
    }

}
