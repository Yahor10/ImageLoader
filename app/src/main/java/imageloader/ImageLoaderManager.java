package imageloader;

import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ychabatarou on 17.07.2015.
 */
public class ImageLoaderManager {
    static int availableProcessors = Runtime.getRuntime().availableProcessors();
    static ExecutorService bitmapLoadservice  = availableProcessors > 2 ? Executors.newFixedThreadPool(availableProcessors - 1) : Executors.newFixedThreadPool(1);

    public void loadImage(){
        if(Thread.currentThread() == Looper.getMainLooper().getThread()){
            throw new IllegalArgumentException("wrong thread");

        }

        bitmapLoadservice.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(null,"exec submit target" );
            }
        });
    }

}
