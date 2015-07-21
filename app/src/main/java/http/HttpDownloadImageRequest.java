package http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ychabatarou on 20.07.2015.
 */
public class HttpDownloadImageRequest {

    private static int TARGET_WIDTH = 100;
    private static int TARGET_HEIGHT = 100;
    final BitmapFactory.Options options;

    public HttpDownloadImageRequest(){
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Boolean scaleByHeight = Math.abs(options.outHeight - TARGET_HEIGHT) >= Math.abs(options.outWidth - TARGET_WIDTH);

        if(options.outHeight * options.outWidth * 2 >= 200*200*2){
            // Load, scaling to smallest power of 2 that'll get it <= desired dimensions
            double sampleSize = scaleByHeight
                    ? options.outHeight / TARGET_HEIGHT
                    : options.outWidth / TARGET_WIDTH;
            options.inSampleSize =
                    (int)Math.pow(2d, Math.floor(
                            Math.log(sampleSize)/Math.log(2d)));
        }
        // Do the actual decoding
        options.inJustDecodeBounds = false;
    }

    public Bitmap getBitmap(String url) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        try {
            URL imageUrl = new URL(url);
            conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is, null, options);
            conn.disconnect();
        } catch (RuntimeException r) {
            r.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            bitmap = null;
        } catch (Throwable ex) {
            ex.printStackTrace();
            bitmap = null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return bitmap;
    }

}
