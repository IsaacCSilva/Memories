package edu.csulb.memoriesapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by Francisco on 12/6/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
            mThumbUrls = new ArrayList<String>();
        }

        public int getCount() {
            return mThumbUrls.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                int size = (int)  mContext.getResources().getDimension(R.dimen.grid_image_size);
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            //load image from firebase using Glide
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference imgReference = firebaseStorage.getReferenceFromUrl(mThumbUrls.get(position));

            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(imgReference)
                    .bitmapTransform(new RotateTransformation(mContext, 90), new CenterCrop(mContext))
                    .placeholder(R.color.cardview_dark_background)
                    .into(imageView);

            return imageView;
        }

        // references to our images
        private ArrayList<String> mThumbUrls;

        public void addImageUrlString(String url){
            mThumbUrls.add(url);
        }
}
