package images.android.by.testimages;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import imageloader.ImageLoaderManager;


public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listView = getListView();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    Log.i("a", "scrolling stopped...");
                    Log.v(null, "vis " + listView.getLastVisiblePosition());
                }
            }
        });

        String [] songsArray = new String[110];

        // Fill the songs array by using a for loop
        for(int i=0; i < songsArray.length; i++){
            songsArray[i] = "Song " + i;
        }

        // For this moment, you have list of songs and a ListView where you can display a list.
        //But how can we put this data set to the list?
        //This is where you need an Adapter

        //context -  The current context.
        //resource - The resource ID for a layout file containing a layout to use when instantiating views.
        //textViewResourceId - The id of the TextView within the layout resource to be populated
        //From the third parameter, you plugged the data set to adapter
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, songsArray);

        listView.setAdapter(arrayAdapter);
        int visibleChildCount = (listView.getLastVisiblePosition() - listView.getFirstVisiblePosition()) + 1;

        ImageLoaderManager loader = new ImageLoaderManager();
        for(int i =0;i < 100;i++) {
            loader.loadImage();
        }
    }

    private final static class ImageAdapter extends ArrayAdapter<String>{
        final LayoutInflater mInflater;

        public ImageAdapter(Context context) {
            super(context, 0);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final View inflate = mInflater.inflate(R.layout.item_list_image, null);
            return inflate;
        }

        private  static class ViewHolder {
            ImageView imageView;
        }
    }


}
