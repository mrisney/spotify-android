package org.risney.cache.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import org.risney.spotify.R;

public class ImageViewActivity extends Activity {
    private static final String TAG = ImageViewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        byte[] bytes = getIntent().getByteArrayExtra("bitmapbytes");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        ImageView image = (ImageView) findViewById(R.id.imageView);

        image.setImageBitmap(getScaledBitmap(bitmap));
        Log.d(TAG,"updated image ...");

        String src = getIntent().getStringExtra("src");
        Log.d(TAG,"src ="+src);

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
