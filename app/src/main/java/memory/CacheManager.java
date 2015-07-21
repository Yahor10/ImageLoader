package memory;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ychabatarou on 21.07.2015.
 */
public class CacheManager {

    //FIXME write custom file cache;

    static Map<Integer, String> hashs = Collections.synchronizedMap(new HashMap<Integer, String>()); // validate result image hash code with  loading url

    private LimitMemoryCache<String, SoftReference<Bitmap>> limitMemoryLruCache;

    private LruCache<String, Bitmap> fileCache = new LruCache<>(120000);

    public CacheManager(Context context) {
        if (limitMemoryLruCache == null) {
            initMemoryCache(context);
        }
    }

    public void addHash(Integer hash, String url) {
        hashs.put(hash, url);
    }

    public String getUrlbyHash(Integer hash) {
        final String s = hashs.get(hash);
        return s;
    }

    public Bitmap getFromCache(String url) {
        Bitmap bitmap = getFromMemory(url);
        if (bitmap == null) {
            bitmap = getFromFileCache(url);
        }
        return bitmap;
    }

    public Bitmap getFromFileCache(String url) {
        final Bitmap bitmap = fileCache.get(url);
        return bitmap;
    }

    private Bitmap getFromMemory(String url) {
        final SoftReference<Bitmap> reference = limitMemoryLruCache.get(url);
        if (reference == null) return null;
        final Bitmap bitmap = reference.get();
        return bitmap;
    }

    private void initMemoryCache(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
        limitMemoryLruCache = new LimitMemoryCache<String, SoftReference<Bitmap>>(cacheSize) {
            @Override
            public int sizeOf(String key, SoftReference<Bitmap> value) {
                final Bitmap bitmap = value.get();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount();
                } else {
                    return bitmap.getRowBytes()
                            * bitmap.getHeight();
                }
            }
        };
    }

    public void put(String url, Bitmap bitmap) {
        if (limitMemoryLruCache.get(url) != null || fileCache.get(url) != null) {
            return;
        }

        boolean put = false;
        try {
            put = limitMemoryLruCache.put(url, new SoftReference<Bitmap>(bitmap));
        } catch (Exception e) {
            Log.e(null, "exeption " + e.getMessage());
        }

        if (!put) {
            fileCache.put(url, bitmap);
        }
    }
}
