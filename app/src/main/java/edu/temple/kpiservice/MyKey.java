package edu.temple.kpiservice;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.security.KeyPair;

public class MyKey implements Parcelable, Serializable {
    private String strUser;
    private String strKey;

    public MyKey(String user, String key)
    {
        this.strUser = user;
        this.strKey = key;
    }

    protected MyKey(Parcel in) {
        strUser = in.readString();
        strKey = in.readString();
    }

    public static final Creator<MyKey> CREATOR = new Creator<MyKey>() {
        @Override
        public MyKey createFromParcel(Parcel in) {
            return new MyKey(in);
        }

        @Override
        public MyKey[] newArray(int size) {
            return new MyKey[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(strUser);
        parcel.writeString(strKey);
    }

    public String getUser() {
        return strUser;
    }

    public void setTitle(String user) {
        this.strUser = user;
    }

    public String getKey() {
        return strKey;
    }

    public void setKey(String key) {
        this.strKey = key;
    }
}
