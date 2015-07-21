package imageloader;

import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ychabatarou on 17.07.2015.
 */
public class ImageLoaderManager {
    static int availableProcessors = Runtime.getRuntime().availableProcessors();
    ExecutorService bitmapLoadservice = availableProcessors > 2 ? Executors.newFixedThreadPool(availableProcessors - 1) : Executors.newFixedThreadPool(1);

    public void submitLoadImage(Runnable runnable) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalArgumentException("wrong thread");
        }

        bitmapLoadservice.submit(runnable);
    }

}
