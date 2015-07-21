package imageloader;

import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by ychabatarou on 20.07.2015.
 */
public class LoadEntity {
    final String url;
    final WeakReference<ImageView>imageRef;

    public LoadEntity(String url, ImageView image) {
        this.url = url;
        this.imageRef = new WeakReference<ImageView>(image);
    }

    @Override
    public String toString() {
        return "LoadEntity{" +
                ", url='" + url + '\'' +
                ", imageRef=" + imageRef.get().getTag() +
                ", imageRef=" + imageRef.get().hashCode() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoadEntity entity = (LoadEntity) o;

        if (url != null ? !url.equals(entity.url) : entity.url != null) return false;
        return !(imageRef.get() != null ? imageRef.get().hashCode() == (entity.imageRef.get().hashCode()) : entity.imageRef.get() != null);

    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (imageRef.get() != null ? imageRef.get().hashCode() : 0);
        return result;
    }
}
