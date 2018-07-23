package de.filipzocktan.util.crypto;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypto {

    private PublicKey pubKey;
    private PrivateKey privKey;

    public Crypto() throws NoSuchAlgorithmException {
        KeyPair keyPair_tmp = buildKeyPair();
        pubKey = keyPair_tmp.getPublic();
        privKey = keyPair_tmp.getPrivate();
    }

    public static String keyToString(PublicKey key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = factory.getKeySpec(key, X509EncodedKeySpec.class);
        return new String(Base64.getEncoder().encode(spec.getEncoded()));
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public byte[] encrypt(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privKey);

        return cipher.doFinal(message.getBytes());
    }

    public byte[] decrypt(byte[] encrypted, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(encrypted);
    }


}




