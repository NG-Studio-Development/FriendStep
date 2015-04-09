package com.alexutils.helpers;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;


import com.google.gson.Gson;


import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Alexander on 14.11.13.
 */
public class EncryptionHelper {

    private static final String TAG = EncryptionHelper.class.getSimpleName();
    private static final String hexDefaultSecret = "051ce9abc26c4b1e86fc08df53c50e903657257663865536552911978ce63753";
    private static final String hexPublicKeyHeader = "3059301306072a8648ce3d020106082a8648ce3d030107034200";

    public static SparseArray<KeyPair> GeneratedKeys = new SparseArray<KeyPair>();
    public static SparseArray<byte[]> NotApprovedSecrets = new SparseArray<byte[]>();

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public static String decrypt(String hexPublicKey, String hexEncryptedData) {
        synchronized (EncryptionHelper.class) {
            if (TextUtils.isEmpty(hexEncryptedData)) {
                return null;
            }
            byte[] encryptedData = Hex.decode(hexEncryptedData);
            if (TextUtils.isEmpty(hexPublicKey)) {
                return decryptDefault(encryptedData);
            }

            byte[] keyBytes = prepareKey(hexPublicKey);
            if (keyBytes == null) {
                Log.w(TAG, "Can't get instance of SHA-256");
                return decryptDefault(encryptedData);
            }

            byte[] decrypted = null;
            try {
                decrypted = decrypt(keyBytes, encryptedData);
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

            if (decrypted == null) {
                Log.w(TAG, "Can't decrypt with shared key, trying default one");
                return decryptDefault(encryptedData);
            }
            return new String(decrypted);
        }
    }

    public static String encrypt(String hexPublicKey, String clearString) {
        synchronized (EncryptionHelper.class) {
            if (TextUtils.isEmpty(clearString)) {
                return null;
            }
            if (TextUtils.isEmpty(hexPublicKey)) {
                return encryptDefault(clearString);
            }

            byte[] keyBytes = prepareKey(hexPublicKey);
            if (keyBytes == null) {
                Log.w(TAG, "Can't get instance of SHA-256");
                return encryptDefault(clearString);
            }

            byte[] encrypted = null;
            byte[] data = clearString.getBytes();
            try {
                encrypted = encrypt(keyBytes, data);
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

            if (encrypted == null) {
                Log.w(TAG, "Can't encrypt with shared key, trying default one");
                return encryptDefault(clearString);
            }
            return new String(Hex.encode(encrypted));
        }
    }

    private static byte[] prepareKey(String publicKey) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (digest == null) {
            return null;
        }

        digest.update(Hex.decode(publicKey));
        for (int i = 0; i < 4; i++) {
            digest.update(digest.digest());
        }
        return digest.digest();
    }

    public static String encryptDefault(String clearString) {
        byte[] data = clearString.getBytes();
        byte[] encrypted = encryptDefault(data);
        if (encrypted == null) return null;
        return new String(Hex.encode(encrypted));
    }

    public static byte[] encryptDefault(byte[] data) {
        byte[] encrypted = null;
        try {
            encrypted = encrypt(Hex.decode(hexDefaultSecret), data);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        if (encrypted == null) return null;
        return encrypted;
    }

    public static String decryptDefault(byte[] encryptedData) {
        byte[] decrypted = decryptBytesDefault(encryptedData);
        if (decrypted == null) return null;
        return new String(decrypted);
    }

    public static byte[] decryptBytesDefault(byte[] encryptedData) {
        byte[] decrypted = null;
        try {
            decrypted = decrypt(Hex.decode(hexDefaultSecret), encryptedData);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public static KeyPair generateEcKeys(int hash) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        // generate EC keys
        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp256r1");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "SC");
        kpg.initialize(ecParamSpec);
        KeyPair kp = kpg.generateKeyPair();
        GeneratedKeys.put(hash, kp);
        return kp;
    }

    public static byte[] getSharedSecret(byte[] pubKeyStr, byte[] privKeyStr) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        KeyFactory kf = KeyFactory.getInstance("ECDH", "SC");

        X509EncodedKeySpec x509ks = new X509EncodedKeySpec(pubKeyStr);
        PublicKey pubKeyA = kf.generatePublic(x509ks);

        PKCS8EncodedKeySpec p8ks = new PKCS8EncodedKeySpec(privKeyStr);
        PrivateKey privKeyA = kf.generatePrivate(p8ks);
        return getSharedSecret(privKeyA, pubKeyA);
    }


    private static byte[] getSharedSecret(PrivateKey privKeyA, PublicKey pubKeyB) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
        KeyAgreement aKeyAgreement = KeyAgreement.getInstance("ECDH", "SC");
        aKeyAgreement.init(privKeyA);
        aKeyAgreement.doPhase(pubKeyB, true);
        return aKeyAgreement.generateSecret();
    }


    private static byte[] encrypt(byte[] raw, byte[] clear) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES", "SC");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        if (cipher == null) throw new IllegalBlockSizeException();
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(clear);
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES", "SC");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        if (cipher == null) throw new IllegalBlockSizeException();
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }


    public static String getNormalizedKey(String publicKey) {
        if (!TextUtils.isEmpty(publicKey) && publicKey.length() == 130) {
            return hexPublicKeyHeader + publicKey;
        }
        return publicKey;
    }

    public static String getiOsKey(String publicKey) {
        if (!TextUtils.isEmpty(publicKey) && publicKey.length() == 182) {
            return publicKey.substring(52);
        }
        return publicKey;
    }

    public static void encryptStream(String secret, InputStream is, OutputStream os) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException {
        byte[] keyBytes;
        if (TextUtils.isEmpty(secret)) {
            keyBytes = Hex.decode(hexDefaultSecret);
        } else {
            keyBytes = prepareKey(secret);
            if (keyBytes == null) {
                keyBytes = Hex.decode(hexDefaultSecret);
            }
        }
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES", "SC");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        if (cipher == null) throw new IllegalBlockSizeException();
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        CipherOutputStream cos = new CipherOutputStream(os, cipher);
        // Write bytes
        int b;
        byte[] d = new byte[1024];
        try {
            while ((b = is.read(d)) != -1) {
                cos.write(d, 0, b);
            }
            // Flush and close streams.
            cos.flush();
            cos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decryptStream(String secret, InputStream is, OutputStream os) throws IllegalBlockSizeException {
        byte[] keyBytes;
        if (TextUtils.isEmpty(secret)) {
            keyBytes = Hex.decode(hexDefaultSecret);
        } else {
            keyBytes = prepareKey(secret);
            if (keyBytes == null) {
                keyBytes = Hex.decode(hexDefaultSecret);
            }
        }
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES", "SC");
            if (cipher == null) throw new IllegalBlockSizeException();
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        if (cipher == null) return;
        CipherInputStream cis = new CipherInputStream(is, cipher);
        byte[] buffer = new byte[1024];
        try {
            while (is.read(buffer) != -1) {
                os.write(cipher.update(buffer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                cis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Static call to load SC provider
     */
    public static void init() {

    }


}
