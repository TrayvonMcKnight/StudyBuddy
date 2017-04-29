package Encryption;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES128CBC {

    private final String ENCRYPTION_ALGO = "AES/CBC/PKCS5PADDING";
    private final int KEYSIZE = 16;
    private byte[] key;
    private SecretKeySpec secretKey;
    private Cipher cipher;

    // Class Constructor
    public AES128CBC(byte[] sharedkey) {
        if (sharedkey.length == KEYSIZE) {
            this.key = sharedkey;
        } else {
            this.key = new byte[KEYSIZE];
        }
        this.initCipher();
    }

    private void initCipher() {
        this.secretKey = new SecretKeySpec(this.key, "AES");
        try {
            cipher = Cipher.getInstance(ENCRYPTION_ALGO);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(AES128CBC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte[] getKey() {
        return this.key;
    }

    public boolean setKey(byte[] input) {
        if (input.length == KEYSIZE) {
            this.key = input;
            return true;
        }
        return false;
    }

    public String encrypt(String plaintext) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);    // Initialize the cipher for encrypt.
            byte[] messageArray = plaintext.getBytes("UTF-8");   // byte array for message.
            byte[] iv = cipher.getIV(); // Byte array for initialization vector.
            byte[] cipherText = cipher.doFinal(messageArray);   // Encrypted message.
            byte[] appendedMessage = new byte[iv.length + cipherText.length];   // Byte Array for total message sent.
            System.arraycopy(iv, 0, appendedMessage, 0, iv.length); // Copy iv as first 16 bytes.
            System.arraycopy(cipherText, 0, appendedMessage, iv.length, cipherText.length); // Copy the encrypted message as remaining bytes.
            return new String(Base64.getMimeEncoder().encode(appendedMessage));  // Encrypted string as Base64 encoder.
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException ex) {
            Logger.getLogger(AES128CBC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String decrypt(String ciphertext) {
        try {
            byte[] incoming = Base64.getMimeDecoder().decode(ciphertext);  // Encrypted string as Base64 decoder.
            byte[] ivArray = new byte[KEYSIZE];  // Byte array for initialization vector.
            byte[] messageArray = new byte[incoming.length - KEYSIZE];   // Byte array to hold the actual message.
            System.arraycopy(incoming, 0, ivArray, 0, KEYSIZE);  // Read first 16 bytes into ivArray.
            System.arraycopy(incoming, KEYSIZE, messageArray, 0, incoming.length - KEYSIZE);  // Read the additional bytes as the message.
            IvParameterSpec iv = new IvParameterSpec(ivArray);  // create iv from first 16 bytes.
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);     // Initialize the cipher for decrypt with key and iv extracted from input.
            return new String(cipher.doFinal(messageArray));  // Decrypt the original message.
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(AES128CBC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
