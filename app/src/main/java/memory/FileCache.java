package memory;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import utils.Utils;

/**
 * Created by ychabatarou on 20.07.2015.
 */

//FIXME not already created

public class FileCache<K,V> implements MemoryCache<String, Bitmap> {

    private final File mCacheDir;
    private final LinkedHashMap<String, File> lruEntries =
            new LinkedHashMap<String, File>(0, 0.75f, true);


    private static final class CacheEntry {
        public String contentUrl;
        private CacheEntry( String contentUrl) {
            this.contentUrl = contentUrl;
        }

        public String getKeyFromUrl(){
            return Md5.encode(contentUrl);
        }
    }

    private class Editor {
        public void edit() {

        }
    }

    public FileCache(Context context) {
        mCacheDir = getDiskCacheDir(context, "myTestCache");
        Log.v(null,"cache path" + mCacheDir.getAbsolutePath());
        if(!mCacheDir.exists()){
            mCacheDir.mkdir();
        }

    }

    @Override
    public boolean put(String key, Bitmap value) {

        final CacheEntry cacheEntry = new CacheEntry(key);
        final String keyFromUrl = cacheEntry.getKeyFromUrl();
        final File file = new File(mCacheDir, keyFromUrl);

        lruEntries.put(keyFromUrl,file);
        storeBitmap(value, file);
        return true;
    }

    private int count = 0;

    private void storeBitmap(Bitmap _bitmap, File file) {
        try {
            if(!file.exists()){
                file.createNewFile();
            }

            FileOutputStream f = new FileOutputStream(file);
            _bitmap.compress(Bitmap.CompressFormat.JPEG, 100, f);
            f.flush();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bitmap get(String key) {
        final String encode = Md5.encode(key);
        final File file = lruEntries.get(encode);

        if(file == null){
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return bitmap;
    }

    @Override
    public Bitmap remove(String key) {
        final File remove = lruEntries.remove(key);
        if(remove != null){
            remove.delete();
        }
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public int sizeOf(String key, Bitmap value) {
        return 0;
    }

    @Override
    public long size() {
        return mCacheDir.listFiles().length;
    }


    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        if (Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }
}
