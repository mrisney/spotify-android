package org.risney.cache.android.views;

import android.content.Context;
import android.view.GestureDetector;
import android.widget.GridView;

/**
 * Created by marcrisney on 6/26/16.
 */
public class ImageGridView  extends GridView {
    private static final String TAG = ImageGridView.class.getSimpleName();

    private OnTouchListener onTouchListener;
    private GestureDetector gestureDetector;
    private Context context;

    public ImageGridView(Context context) {
        super(context);
       // gestureDetector = new GestureDetector(new TapDetector());

    }

    /*
    public OnTouchListener getOnTouchListener() {
        return onTouchListener;
    }

    class TapDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            Image image = (Image) touchedView.getTag();
            Log.d(TAG, "Double tapped image id " + image.getId());
            try {
                Intent intent = new Intent(context, ViewImageActivity.class);

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
                //startActivity(intent);

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
               // syncImageCacheInfo();

            }
            gridView.startLayoutAnimation();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                   // gridViewAdapter.notifyDataSetChanged();
                }
            }, 500);
            return super.onSingleTapConfirmed(e);
        }
    });

    */
}
