package edu.temple.kpiservice;

import android.content.Context;
import android.util.Base64;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import java.io.*;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class LocalService extends Service {

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocalService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocalService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //=============================================================
    //boolean resetMyKeyPair = false;
    //Map< String , PublicKey > partnerKeyMap;
    private final String KEY_LIST_FILE = "myKeyList";
    KeyList keyList = null;//new KeyList();

    KeyPairGenerator kpg;
    KeyPair keyPair;
    Cipher cipher;
    RSAPrivateKey privateKey;
    RSAPublicKey publicKey;


    //Generate and/or retrieve a user’s RSA KeyPair.
    public KeyPair getMyKeyPair() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, ClassNotFoundException {
        readKeysFromFile();/*
        if(resetMyKeyPair) {
            resetMyKeyPair = false;
            kpg = KeyPairGenerator.getInstance("RSA");
            keyPair = kpg.generateKeyPair();

            String strKeyPair = toString(keyPair);
            keyList.storePublicKey("myKeyPair",strKeyPair);

            saveKeyToFile();
        }
        else
        {*/
            String strKeyPair = keyList.getPublicKey("myKeyPair");
            if(strKeyPair == null) {
                System.out.printf("====== Can not find myKeyPair in key list.\n");
                kpg = KeyPairGenerator.getInstance("RSA");
                keyPair = kpg.generateKeyPair();
                strKeyPair = toString(keyPair);
                keyList.storePublicKey("myKeyPair",strKeyPair);
                saveKeyToFile();
                return null;
            }
            keyPair = (KeyPair)fromString(strKeyPair);
        //}
        return keyPair;
    }

    //Store a key for a provided partner name
    void storePublicKey (String partnerName, String publicKey)
            throws IllegalBlockSizeException,
            InvalidKeyException,
            BadPaddingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException, IOException {

        readKeysFromFile();
        keyList.storePublicKey(partnerName, publicKey);

        saveKeyToFile();
    }

    //Returns the public key associated with the provided partner name
    RSAPublicKey getPublicKey(String partnerName)
            throws IllegalBlockSizeException,
            InvalidKeyException,
            BadPaddingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeySpecException, IOException {


        String publicKeyString = "";
        readKeysFromFile();
        publicKeyString = keyList.getPublicKey(partnerName);
        if(publicKeyString == null) {
            System.out.printf("========= getPublicKey %s, by partner name: %s \n", publicKeyString, "partner-1");
            return null;
        }

        System.out.printf("========= getPublicKey by partner name: %s \n", "partner-1");
        RSAPublicKey rsaPublicKey = (RSAPublicKey)stringToPublicKey( publicKeyString);

        return rsaPublicKey;

        //if(publicKey == null)
        //    System.out.printf("========= getPublicKey: converting fail.(%s)\n",publicKeyString);
        //KeyFactory fact = KeyFactory.getInstance("RSA");
        //RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(publicKey.getModulus(), new BigInteger(publicKeyString));
        //rsaPublicKey = (RSAPublicKey) fact.generatePublic(pubKeySpec);

        //return rsaPublicKey;
    }

    //erase the respective stored keys.
    void resetMyKeyPair() throws NoSuchAlgorithmException, IOException {
        kpg = KeyPairGenerator.getInstance("RSA");
        keyPair = kpg.generateKeyPair();

        String strKeyPair = toString(keyPair);
        readKeysFromFile();
        keyList.storePublicKey("myKeyPair",strKeyPair);

        saveKeyToFile();
    }

    private PublicKey stringToPublicKey(String publicKeyString)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        try {
            byte[] keyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
            return publicKey;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();

            return null;
        }
    }

    private void saveKeyToFile() throws IOException {
        //saving book list to file
        Context context = getApplicationContext();
        FileOutputStream fos = context.openFileOutput(KEY_LIST_FILE, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(keyList);
        os.close();
        fos.close();
        System.out.printf("====== saved key list to file.\n");
    }

    private void readKeysFromFile(){
        if(keyList != null)
            return;
        Context context = getApplicationContext();
        try {
            FileInputStream fis = context.openFileInput(KEY_LIST_FILE);
            ObjectInputStream is = new ObjectInputStream(fis);
            keyList = (KeyList)is.readObject();
            is.close();
            fis.close();
            System.out.printf("====== Loaded local key list.\n");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(keyList == null)
            keyList = new KeyList();
    }

    private static Object fromString(String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    private static String toString(Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
