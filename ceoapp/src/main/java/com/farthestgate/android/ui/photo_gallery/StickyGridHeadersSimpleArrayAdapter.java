/*
 Copyright 2013 Tonic Artos

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.farthestgate.android.ui.photo_gallery;

import android.content.Context;
import android.graphics.Bitmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.ui.components.timer.TimerObj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tonic Artos
 *
 * Modified by Hanson Aboagye 05/2014
 */

public class StickyGridHeadersSimpleArrayAdapter<String> extends BaseAdapter implements
        StickyGridHeadersSimpleAdapter {

    private int mHeaderResId;
    //private PrettyTime prettyTime;
    private static Context _context;
    private ArrayList<java.lang.String> _filePaths = new ArrayList<java.lang.String>();
    private int imageWidth;
    private int mNumColumns = 3;
    private static final java.lang.String IMAGE_CACHE_DIR = "thumbs";
    private int mImageThumbSize;
    private ImageResizer mImageFetcher;

    private JSONArray observations = new JSONArray();

    private LayoutInflater mInflater;

    public StickyGridHeadersSimpleArrayAdapter(Context context, int headerResId,
                                               ArrayList<java.lang.String> filePaths,
                                               int imageWidth, Bitmap empty) {
        init(context, headerResId,filePaths,imageWidth, empty);
    }


    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getCount() {
        return _filePaths.size();
    }


    @Override
    public java.lang.String getHeaderId(int position) {
        java.lang.String item = getItem(position);
        java.lang.String header = "";

        for (int index = 0; index < observations.length(); index++)
        {
            try
            {
                Boolean found               = false;
                JSONObject obsObject        = observations.getJSONObject(index);
                Integer observation         = obsObject.optInt("observationNumber", 0);
                List<PCNPhotoTable> photos  = DBHelper.PhotosForPCN(observation);
                Long logTime                = obsObject.optLong("logTime", 0l);
                long issuedTime             = obsObject.optLong("issuedTime", 0l);
             //   java.lang.String atTime     = prettyTime.format(new Date(logTime));

          //      if (issuedTime > 0)
            //        atTime = prettyTime.format(new Date(issuedTime));
                java.lang.String[] res  = item.split("/");

                item = res[res.length - 1];

                for (PCNPhotoTable photoInfo : photos)
                {
                    java.lang.String fileName = photoInfo.getFileName();
                    if (fileName.startsWith("/storage"))
                    {
                        java.lang.String[] filenameBits = fileName.split("/");
                        fileName = filenameBits[filenameBits.length - 1];

                    }
                    if (item.equals(fileName))
                    {
                        Integer obsCheck = photoInfo.getObservation();
                        if (obsCheck.equals(observation))
                        {
                            if (!photoInfo.getFileName().contains("+"))
                            {
                                res = item.split("-");
                                header = res[0];
                                //header += " issued " + atTime;
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (!found)
                {
                    header = "Observation ";// + atTime;

                    /**
                     * Exiting here is relevant and would speed up the app
                     * but would only work if there are no extra tickets in the database from
                     * another session
                     */


                    //break;
                }
                else
                {
                    break;
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                return "";
            }
        }
        return header;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mHeaderResId, parent, false);
            holder = new HeaderViewHolder();
            holder.textView = (TextView)convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder)convertView.getTag();
        }

        holder.textView.setText(getHeaderId(position));

        return convertView;
    }

    @Override
    public java.lang.String getItem(int position) {
        return _filePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(_context);
        } else {
            imageView = (ImageView) convertView;
        }


        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(imageWidth,
                imageWidth));

        mImageFetcher.loadImage(_filePaths.get(position) , imageView);


        return imageView;
    }



    private void init(Context context, int headerResId,
                      ArrayList<java.lang.String> filePaths, int imageWidth, Bitmap empty) {

        //prettyTime = new PrettyTime();
        this.mHeaderResId = headerResId;
        this._context = context;
        mInflater = LayoutInflater.from(context);
        this._filePaths = filePaths;
        this.imageWidth = imageWidth;
        ArrayList<TimerObj> mTimers = TimerObj.getTimersFromDatabase();

        for (TimerObj timerObj: mTimers)
        {
            try
            {
                observations.put(new JSONObject(timerObj.pcnJSON));

            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        mImageThumbSize = CeoApplication.PHOTO_THUMB_SIZE;

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(context, IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageResizer(context, mImageThumbSize);
        mImageFetcher.setLoadingImage(empty);
        mImageFetcher.addImageCache(((FragmentActivity)context).getSupportFragmentManager(), cacheParams);
    }

    protected class HeaderViewHolder {
        public TextView textView;
    }


}
