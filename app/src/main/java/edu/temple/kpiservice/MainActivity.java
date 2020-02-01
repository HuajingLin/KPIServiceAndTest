package edu.temple.kpiservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    LocalService mService;
    boolean mBound = false;
    boolean first = true;

    Button btnDecrypt, btnEncrypt;
    Cipher cipher;
    KeyPair keys;
    RSAPrivateKey privateKey;
    RSAPublicKey publicKey;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDecrypt = (Button) findViewById(R.id.buttonDecrypt);
        btnEncrypt = (Button) findViewById(R.id.buttonEncrypt);
        final EditText username = (EditText) findViewById(R.id.editInfo);


        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!mBound) {
                    Toast.makeText(getApplicationContext(), "the service didn't bind.", Toast.LENGTH_LONG).show();
                    return;
                }
                //======

                byte[] encryptedText = Base64.decode(username.getText().toString(), Base64.DEFAULT);

                try {
                    cipher.init(Cipher.DECRYPT_MODE, publicKey);
                    String text = new String(cipher.doFinal(encryptedText));
                    username.setText(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(first)
                {
                    first = false;
                    try {
                        keys = mService.getMyKeyPair();
                        KeyFactory fact = KeyFactory.getInstance("RSA");
                        cipher = Cipher.getInstance("RSA");
                        privateKey = (RSAPrivateKey) keys.getPrivate();
                        publicKey = (RSAPublicKey) keys.getPublic();

                        //privateKeyString = privateKey.getPrivateExponent().toString();
                        //publicKeyString = publicKey.getPublicExponent().toString();

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (!mBound) {
                    Toast.makeText(getApplicationContext(), "the service didn't bind.", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Encrypt...", Toast.LENGTH_LONG).show();
                String text = username.getText().toString();
                System.out.printf("======Encrypt: %s\n",text);

                try {
                    cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                    byte[] encryptedText = cipher.doFinal(text.getBytes());
                    username.setText(Base64.encodeToString(encryptedText, Base64.DEFAULT));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public KeyPair Request_a_keypair() throws InterruptedException {


        try {
            keys = mService.getMyKeyPair();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return keys;
    }
}
