package edu.temple.kpiservice;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Base64;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.net.sip.SipErrorCode.TIME_OUT;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

/*
    @Test
    public void useAppContext() throws Throwable {
        // Context of the app under test.
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Context appContext = mInstrumentation.getTargetContext();

        assertEquals("edu.temple.kpiservice", appContext.getPackageName());
    }

*/

    @Test
    public void testService_Request_a_keypair() throws InterruptedException {
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Context appContext = mInstrumentation.getTargetContext();

        Intent intent = new Intent(appContext, LocalService.class);

        // Defines callbacks for service binding, passed to bindService()
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
                LocalService mService = binder.getService();
                //mBound = true;

                KeyPair keys = null;
                System.out.printf("1========= test: Service_Request_a_keypair\n");
                try {
                    keys = mService.getMyKeyPair();
                    if(keys == null)
                        return;
                    RSAPrivateKey privateKey = (RSAPrivateKey) keys.getPrivate();
                    RSAPublicKey publicKey = (RSAPublicKey) keys.getPublic();

                    //String privateKeyString = privateKey.getPrivateExponent().toString();
                    //String publicKeyString = publicKey.getPublicExponent().toString();

                    byte[] privateKeyBytes = Base64.encode(keys.getPrivate().getEncoded(),0);
                    String priKey = new String(privateKeyBytes);

                    byte[] publicKeyBytes = Base64.encode(keys.getPublic().getEncoded(),0);
                    String pubKey = new String(publicKeyBytes);

                    System.out.printf("1========= private Key:\n\t %s \n",priKey);
                    System.out.printf("1========= public Key:\n\t %s \n",pubKey);

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                assertNotNull(keys);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                //mBound = false;
                System.out.printf("========= onServiceDisconnected\n");
            }
        };

        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Test
    public void encrypted_And_Decrypted_Text(){
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Context appContext = mInstrumentation.getTargetContext();

        Intent intent = new Intent(appContext, LocalService.class);

        /// Defines callbacks for service binding, passed to bindService()
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
                LocalService mService = binder.getService();

                //generate a random String
                byte[] array = new byte[15];
                new Random().nextBytes(array);
                String text = new String(array, Charset.forName("UTF-8"));

                String textEncrypted = "";
                String textDecrypted = "";

                KeyPair keys = null;
                Cipher cipher = null;
                RSAPrivateKey privateKey;
                RSAPublicKey publicKey;
                try {
                    keys = mService.getMyKeyPair();
                    KeyFactory fact = KeyFactory.getInstance("RSA");
                    cipher = Cipher.getInstance("RSA");
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

                if(keys == null)
                     return;
                privateKey = (RSAPrivateKey) keys.getPrivate();
                publicKey = (RSAPublicKey) keys.getPublic();

                System.out.printf("2========= encrypted And Decrypted Text\n");
                //String privateKeyString = privateKey.getPrivateExponent().toString();
                //String publicKeyString = publicKey.getPublicExponent().toString();
                //System.out.printf("2========= private Key:\n\t %s \n",privateKeyString);
                //System.out.printf("2========= public Key:\n\t %s \n",publicKeyString);

                try {
                    cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                    byte[] encryptedText = cipher.doFinal(text.getBytes());
                    textEncrypted = Base64.encodeToString(encryptedText, Base64.DEFAULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //decrypted
                byte[] encryptedText = Base64.decode(textEncrypted, Base64.DEFAULT);

                try {
                    cipher.init(Cipher.DECRYPT_MODE, publicKey);
                    textDecrypted = new String(cipher.doFinal(encryptedText));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.printf("2=====text: %s\n",text);
                System.out.printf("2=====textDecrypted: %s\n",textDecrypted);
                assertEquals(text, textDecrypted);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Test
    public void test_retrieved_keypair(){
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Context appContext = mInstrumentation.getTargetContext();

        Intent intent = new Intent(appContext, LocalService.class);

        // Defines callbacks for service binding, passed to bindService()
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
                LocalService mService = binder.getService();
                //mBound = true;

                KeyPair keys = null;
                System.out.printf("3========= test: test_retrieved_keypair\n");
                try {
                    keys = mService.getMyKeyPair();
                    if(keys == null)
                        return;
                    RSAPrivateKey privateKey = (RSAPrivateKey) keys.getPrivate();
                    RSAPublicKey publicKey = (RSAPublicKey) keys.getPublic();

                    byte[] privateKeyBytes = Base64.encode(keys.getPrivate().getEncoded(),0);
                    String priKey = new String(privateKeyBytes);

                    byte[] publicKeyBytes = Base64.encode(keys.getPublic().getEncoded(),0);
                    String pubKey = new String(publicKeyBytes);

                    System.out.printf("3========= private Key:\n\t %s \n",priKey);
                    System.out.printf("3========= public Key:\n\t %s \n",pubKey);

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                assertNotNull(keys);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
            }
        };

        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Test
    public void test_store_key_by_partnerName(){
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Context appContext = mInstrumentation.getTargetContext();

        Intent intent = new Intent(appContext, LocalService.class);

        // Defines callbacks for service binding, passed to bindService()
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
                LocalService mService = binder.getService();

                System.out.printf("4========= test: test_store_key_by_partnerName\n");
                String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzi1P18ldTgeRs3TJQqfNNrPpt08li3Ke" +
                        "yyMRcttd3+FKRcIYp0Hcyy+c8L8KxVNBxIRidAcILLeBDU6QPArDjW7RYhk5N09q6w2YhqAkm7S8" +
                        "KSVL6yarT0v/IOTCZZt8Z5NpUPAwK8Rw0+A/aSNZdwDfsXlJcFP8jxicHjCYXRHEWLlMKix7w425" +
                        "6vEH4NvfT227zoFBheFLyL5nVmhPEcYhmsO5ApXRoV6YLmeYqdWAZ9Rc5473vAxBWOebQ9cWHg+t" +
                        "lmdYO2RT/Kf4lyAUQ/4wb2x8usEUwaS6yAp6Foi9jxwYaQCeNVcR9E2X86UdFjysYN6LXW1qt+Ep" +
                        "VzFtJwIDAQAB";
                try {
                    mService.storePublicKey("partner-1", publicKey);
                    System.out.printf("4========= partner name:\n\t %s \n","partner-1");
                    System.out.printf("4========= publice key:\n\t %s \n",publicKey);

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
                //assertNotNull(keys);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                //mBound = false;
                System.out.printf("========= onServiceDisconnected\n");
            }
        };

        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Test
    public void test_retrieved_key_by_partnerName(){
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Context appContext = mInstrumentation.getTargetContext();

        Intent intent = new Intent(appContext, LocalService.class);

        /// Defines callbacks for service binding, passed to bindService()
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
                LocalService mService = binder.getService();
                RSAPublicKey publicKey = null;
                System.out.printf("5========= test: test_retrieved_key_by_partnerName 333\n");

                try {
                    publicKey = mService.getPublicKey("partner-1");
                    byte[] publicKeyBytes = Base64.encode(publicKey.getEncoded(),0);
                    String pubKey = new String(publicKeyBytes);

                    //String strPublicKey = publicKey.getPublicExponent().toString();
                    System.out.printf("5========= partner name:\n\t %s \n","partner-1");
                    System.out.printf("5========= publice key:\n\t %s \n",pubKey);

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assertNotNull(publicKey);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                //mBound = false;
                System.out.printf("========= onServiceDisconnected\n");
            }
        };

        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

/*
    @Test
    public void test_Request_New_keypair() throws InterruptedException {
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        Context appContext = mInstrumentation.getTargetContext();

        Intent intent = new Intent(appContext, LocalService.class);

        // Defines callbacks for service binding, passed to bindService()
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
                LocalService mService = binder.getService();
                //mBound = true;

                KeyPair keys = null;
                System.out.printf("6========= test: Service_Request_a_keypair\n");
                try {
                    mService.resetMyKeyPair();
                    keys = mService.getMyKeyPair();
                    if(keys == null)
                        return;
                    RSAPrivateKey privateKey = (RSAPrivateKey) keys.getPrivate();
                    RSAPublicKey publicKey = (RSAPublicKey) keys.getPublic();

                    String privateKeyString = privateKey.getPrivateExponent().toString();
                    String publicKeyString = publicKey.getPublicExponent().toString();
                    System.out.printf("6========= private Key:\n\t %s \n",privateKeyString);
                    System.out.printf("6========= public Key:\n\t %s \n",publicKeyString);

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                assertNotNull(keys);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                //mBound = false;
                System.out.printf("========= onServiceDisconnected\n");
            }
        };
        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
*/
}
