package imageloader;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import http.HttpDownloadImageRequest;
import images.android.by.testimages.R;

/**
 * Created by ychabatarou on 20.07.2015.
 */
public final class ImageLoader {
    static LruCache cache = new LruCache(15000000);

    public static String TAG = "DEBUG IMAGE LOADER";

    final static Set<String> urls = new HashSet<String>();
    final static Set<SubmitTask> tasks = new HashSet<SubmitTask>();
    private static final Map<String, ReentrantLock> uriLocks = new WeakHashMap<String, ReentrantLock>();

    final static ImageLoaderManager manager = new ImageLoaderManager();
    private static int loadCount = 0;


   static Map<Integer,String>hashs = Collections.synchronizedMap(new HashMap<Integer,String>());

    public static void loadImage(final String url, final ImageView imageView, final int pos) {
        if (urls.contains(url)) {
            Log.e(TAG, "dublicate url");
            return;
        }

        Log.v(TAG,"load pos," + pos  + " hash : " + imageView.hashCode() +"url" + url);
        hashs.put(imageView.hashCode(), url);

        Bitmap bitmap = (Bitmap) cache.get(url);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
            hashs.put(imageView.hashCode(),url);
            return;
        }


        LoadEntity entity = new LoadEntity(url, imageView,pos);
        final SubmitTask task = new SubmitTask(entity){
            @Override
            public void onLoadComplete(Bitmap bitmap) {
                final LoadEntity resultEntity = getEntity();
                final ImageView imageView =resultEntity.imageRef.get();
                if(imageView == null){
                    Log.e(TAG,"imageView is null");
                    return;
                }

                Log.v(TAG,"load hashs:" + hashs.get(imageView.hashCode()));
                Log.v(TAG,"load complete:" + resultEntity);

                Bitmap cacheResult = (Bitmap) cache.get(resultEntity.url);

                if(cacheResult == null){
                    cache.put(resultEntity.url,bitmap);
                }else{
                    bitmap = (Bitmap) cache.get(resultEntity.url);
                }

                final Bitmap resultBitmap = bitmap;
                final Activity context = (Activity) imageView.getContext();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(resultBitmap);
                    }
                });

                tasks.remove(this);
            }

            @Override
            public void run() {
                Log.v(TAG,"start run another thread:" + !Thread.currentThread().equals(Looper.getMainLooper().getThread()));
                final LoadEntity runEntity = getEntity();
                final ImageView imageView = runEntity.imageRef.get();
                final ReentrantLock lockForUri = getLockForUri(runEntity.url);
                try {
                    if (lockForUri.isLocked()) {
                        Log.e(TAG, "task locked");
                    }
                    Log.v(TAG, "start locking...");
                    lockForUri.lock();
                    Log.v(TAG, "enter locking...");
                    final Object tag = imageView.getTag();
                    if (imageView == null || tag != this) {
                        throw new IllegalArgumentException("wrong tag: " + pos);
                    } else {
                        Log.v(TAG,"start get bitmap");
                        HttpDownloadImageRequest request = new HttpDownloadImageRequest();
                        Bitmap bitmap = request.getBitmap(runEntity.url);
                        if (imageView != null && bitmap != null) {
                            onLoadComplete(bitmap);
                        } else {
                            throw new IllegalArgumentException("weak ref is null");
                        }
                    }
                }catch (IllegalArgumentException e){
                    Log.e(TAG,"error:" + e.getLocalizedMessage());
                }finally {
                    lockForUri.unlock();
                    Log.v(TAG, "exit locking...");
                }
            }
        };

        if(!tasks.contains(task)){
            tasks.add(task);
        }else{
            Log.e(TAG,"task exist");
            return;
        }

        imageView.setImageResource(R.drawable.abc_ab_share_pack_mtrl_alpha);
        imageView.setTag(task);
        manager.submitLoadImage(task);
    }

    private  interface SubmitInterface extends Runnable,ImageCallBack{
    }

    private static class SubmitTask implements SubmitInterface{
        private LoadEntity entity;
        public SubmitTask(LoadEntity entity) {
            this.entity = entity;
        }

        @Override
        public void onLoadComplete(Bitmap entity) {

        }

        @Override
        public void onLoadFailed() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubmitTask that = (SubmitTask) o;
            return !(getEntity() != null ? !getEntity().equals(that.getEntity()) : that.getEntity() != null);
        }

        @Override
        public int hashCode() {
            return getEntity() != null ? getEntity().hashCode() : 0;
        }

        public LoadEntity getEntity() {
            return entity;
        }
        @Override
        public void run() {
            throw  new IllegalAccessError("");
        }
    }
    private static void initCache(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
//        limitLruCache = new LimitLruCache<String, Bitmap>(cacheSize/4) {
//            @Override
//            public int sizeOf(String key, Bitmap value) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
//                    return value.getByteCount();
//                } else {
//                    return value.getRowBytes()
//                            * value.getHeight();
//                }
//            }
//        };
    }

    /**
     * Created by ychabatarou on 17.07.2015.
     */
    public interface ImageCallBack {

        public void onLoadComplete(Bitmap bitmap);

        public void onLoadFailed();

    }

   static ReentrantLock getLockForUri(String uri) {
        ReentrantLock lock = uriLocks.get(uri);
        if (lock == null) {
            lock = new ReentrantLock();
            uriLocks.put(uri, lock);
        }
        return lock;
    }

}
