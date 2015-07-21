package memory;

import android.graphics.Bitmap;

/**
 * Created by ychabatarou on 20.07.2015.
 */
public class FileCache implements MemoryCache<String,Bitmap> {

    @Override
    public boolean put(String key, Bitmap value) {
        return false;
    }

    @Override
    public Bitmap get(String key) {
        return null;
    }

    @Override
    public Bitmap remove(String key) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public int sizeOf(String key, Bitmap value) {
        return 0;
    }
}
