package org.risney.cache.lib.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ConversionUtils {

    private static final Charset STANDARD_CHARSET = Charset.forName("UTF-8");
    private static final int BYTES_PER_READ = 1000;

    public static ByteBuffer stringToByteBuffer(String msg) {
        return ByteBuffer.wrap(msg.getBytes(STANDARD_CHARSET));
    }

    public static String byteBufferToString(ByteBuffer buffer) {
        String msg = "";
        try {
            byte[] bytes;
            if (buffer.hasArray()) {
                bytes = buffer.array();
            } else {
                bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
            }
            msg = new String(bytes, STANDARD_CHARSET);
        } catch (Exception e) {

        }
        return msg;
    }

    public static ByteBuffer readToBuffer(String filename) throws IOException {

        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Illegal Empty String");
        }
        File file = new File(filename);
        ByteBuffer bb = ByteBuffer.allocate((int) file.length());
        FileInputStream fis = new FileInputStream(filename);

        int bytesRead = 0;
        byte[] buf = new byte[BYTES_PER_READ];

        while (bytesRead != -1) {
            bb.put(buf, 0, bytesRead);
            bytesRead = fis.read(buf);
        }
        fis.close();
        return bb;
    }


    public static Bitmap byteBufferToBitmap(ByteBuffer byteBuffer) {

        byte[] bytes = null;
        if (byteBuffer.hasArray()) {
            final byte[] array = byteBuffer.array();
            final int arrayOffset = byteBuffer.arrayOffset();
            bytes = Arrays.copyOfRange(array, arrayOffset + byteBuffer.position(),
                    arrayOffset + byteBuffer.limit());
        }

        InputStream is = new ByteArrayInputStream(bytes);
        BitmapFactory.Options o = new BitmapFactory.Options();

        return BitmapFactory.decodeStream(is, null, o);
    }
    public static ByteBuffer bitmapToByteBuffer(Bitmap bitmap){

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bitmap.getWidth() * bitmap.getHeight());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer);

        return ByteBuffer.wrap(buffer.toByteArray());
    }

    public static int getByteCount(ByteBuffer byteBuffer) {
        int len = 0;
        final ByteBuffer buffer;
        if (byteBuffer.hasArray()) {
            final byte[] array = byteBuffer.array();
            len = array.length;
        }
        return len;
    }

   public static final int sizeOf(Object object) throws IOException {

        if (object == null)
            return -1;

        // Special output stream use to write the content
        // of an output stream to an internal byte array.
        ByteArrayOutputStream byteArrayOutputStream =
                new ByteArrayOutputStream();

        // Output stream that can write object
        ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(byteArrayOutputStream);

        // Write object and close the output stream
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        objectOutputStream.close();

        // Get the byte array
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // TODO can the toByteArray() method return a
        // null array ?
        return byteArray == null ? 0 : byteArray.length;
    }

}


