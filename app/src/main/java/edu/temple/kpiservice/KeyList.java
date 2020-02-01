package edu.temple.kpiservice;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.security.KeyPair;
import java.util.ArrayList;

public class KeyList implements Parcelable, Serializable {
    private ArrayList<MyKey> keyList;

    public KeyList () {
        keyList = new ArrayList<>();
    }

    protected KeyList(Parcel in) {
        keyList = in.createTypedArrayList(MyKey.CREATOR);
    }

    public static final Creator<KeyList> CREATOR = new Creator<KeyList>() {
        @Override
        public KeyList createFromParcel(Parcel in) {
            return new KeyList(in);
        }

        @Override
        public KeyList[] newArray(int size) {
            return new KeyList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(keyList);
    }

    public void storePublicKey (String partnerName, String publicKey){
        MyKey myKey = null;
        for(int i=0;i<keyList.size(); i++){
            myKey = keyList.get(i);
            if(partnerName.equals(myKey.getUser())) {
                myKey.setKey(publicKey);
                //System.out.printf("KeyList========= modify public key.\n");
                return;
            }
        }
        //System.out.printf("KeyList========= Add new key.\n");
        myKey = new MyKey(partnerName, publicKey);
        keyList.add(myKey);
    }

    public String getPublicKey(String partnerName){
        MyKey myKey = null;
        String temp = "";
        for(int i=0;i<keyList.size(); i++){
            myKey = keyList.get(i);/*
            if(temp.length()>1){
                if(temp.equals(myKey.getUser())){
                    keyList.remove(i);System.out.printf("KeyList========= remove(%s).\n",temp);
                    temp = "";
                    i=0;
                    continue;
                }
            }
            else{
                temp = myKey.getUser();
                System.out.printf("KeyList========= temp(%s)%d.\n",temp,temp.length());
            }*/
            //System.out.printf("KeyList=========(%d) one key: %s, %s.\n",i, partnerName, myKey.getUser());
            if(partnerName.equals(myKey.getUser())) {
                //System.out.printf("KeyList========= found!\n");
                return myKey.getKey();
            }
        }
        //System.out.printf("KeyList========= not found.\n");
        return null;
    }
}
