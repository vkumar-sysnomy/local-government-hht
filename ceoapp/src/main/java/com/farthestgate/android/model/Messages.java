package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 6/22/2016.
 */
public class Messages implements Parcelable{
    public String messagetitle;
    public String messagebody;

    public static final Creator<Messages> CREATOR = new Creator<Messages>() {
        @Override
        public Messages createFromParcel(Parcel p) {
            return new Messages(p);
        }

        @Override
        public Messages[] newArray(int size) {
            return new Messages[size];
        }
    };

    public Messages()
    {
    }

    public Messages(String messagetitle, String messagebody)
    {
        this.messagetitle = messagetitle;
        this.messagebody = messagebody;
    }

    public Messages(Parcel in)
    {
        messagetitle = in.readString();
        messagebody = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(messagetitle);
        dest.writeString(messagebody);

    }
}
