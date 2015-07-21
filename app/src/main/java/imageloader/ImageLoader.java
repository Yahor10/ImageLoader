package imageloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import http.HttpDownloadImageRequest;
import images.android.by.testimages.R;
import memory.CacheManager;


/**
 * Created by ychabatarou on 20.07.2015.
 */
public final class ImageLoader {
    public static String TAG = "DEBUG IMAGE LOADER";

    final static ImageLoaderManager manager = new ImageLoaderManager();

    private static CacheManager cacheManager = null;//caches

    public static void loadImage(final String url, final ImageView imageView) {

        if (cacheManager == null) {
            final Context context = imageView.getContext();
            cacheManager = new CacheManager(context);
        }

        cacheManager.addHash(imageView.hashCode(), url);

        final Bitmap bitmap = cacheManager.getFromCache(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            cacheManager.addHash(imageView.hashCode(), url);
            return;
        }


        LoadEntity entity = new LoadEntity(url, imageView);

        final SubmitTask task = new SubmitTask(entity) {
            @Override
            public void onLoadComplete(Bitmap bitmap) {
                final LoadEntity resultEntity = getEntity();
                final ImageView imageView = resultEntity.imageRef.get();
                if (imageView == null) {
                    Log.e(TAG, "imageView is null");
                    return;
                }

                final String urlhash = cacheManager.getUrlbyHash(imageView.hashCode());
                Log.v(TAG, "load complete:" + resultEntity);
                if (!urlhash.equals(resultEntity.url)) {
                    Log.e(TAG, "load complete with wrong url:" + resultEntity.imageRef.get().hashCode());
                    return;
                }

                Bitmap bitmapResult = cacheManager.getFromCache(resultEntity.url);
                if (bitmapResult == null) {
                    cacheManager.put(resultEntity.url, bitmap);
                } else {
                    bitmap = bitmapResult;
                }

                final Context context = imageView.getContext();
                if (context instanceof Activity) {
                    final Bitmap resultBitmap = bitmap;
                    final Activity activity = (Activity) context;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(resultBitmap);
                        }
                    });
                } else {
                    Log.e(TAG, "wrong context");
                }
            }

            @Override
            public void run() {
                final LoadEntity runEntity = getEntity();
                final ImageView imageView = runEntity.imageRef.get();
                try {
                    final Object tag = imageView.getTag();
                    if (imageView == null || tag != this) {
                        throw new IllegalArgumentException("wrong tag: ");
                    } else {
                        HttpDownloadImageRequest request = new HttpDownloadImageRequest();
                        Bitmap bitmap = request.getBitmap(runEntity.url);
                        if (imageView != null && bitmap != null) {
                            onLoadComplete(bitmap);
                        } else {
                            onLoadFailed();
                            throw new IllegalArgumentException("weak ref is null");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "error:" + e.getLocalizedMessage());
                }
            }

            @Override
            public void onLoadFailed() {
                final LoadEntity runEntity = getEntity();
                Log.e(TAG, "load failed:" + runEntity);

                if(runEntity != null && runEntity.imageRef.get() != null){
                    runEntity.imageRef.get().setImageResource(R.drawable.abc_ab_share_pack_mtrl_alpha);
                }

            }
        };

        imageView.setImageResource(R.drawable.abc_ab_share_pack_mtrl_alpha);
        imageView.setTag(task);
        manager.submitLoadImage(task);
    }

    private interface SubmitInterface extends Runnable, ImageCallBack {
    }

    private static class SubmitTask implements SubmitInterface {
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
            throw new IllegalAccessError("");
        }
    }


    /**
     * Created by ychabatarou on 17.07.2015.
     */
    public interface ImageCallBack {

        public void onLoadComplete(Bitmap bitmap);

        public void onLoadFailed();

    }


}
