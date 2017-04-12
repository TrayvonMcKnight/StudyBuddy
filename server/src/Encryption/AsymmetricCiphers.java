package Encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

public class AsymmetricCiphers{
    protected byte[] key;
    protected SecretKeySpec secretKey;
    protected Cipher cipher;
}

class DES extends AsymmetricCiphers{
    // private class fields
    private final String ENCRYPTION_ALGO = "DES/ECB/PKCS5Padding";
    private final int KEYSIZE = 8;

    // class constructor
    public DES(byte[] sharedkey) {
        if (sharedkey.length == KEYSIZE){
            this.key = sharedkey;
        } else {
            this.key = new byte[KEYSIZE];
        }
        this.initCipher();
    }
    
    public byte[] getKey(){
        return this.key;
    }
    
    public boolean setKey(byte[] input){
        if (input.length == KEYSIZE) {
            this.key = input;
            return true;
        }
        return false;
    }
    
    private void initCipher(){
        try {
            this.secretKey = new SecretKeySpec(this.key, 0, this.key.length, "ENCRYPTION_ALGO");
            cipher = Cipher.getInstance(ENCRYPTION_ALGO);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(DES.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String encryptDES(String plaintext){
        String ciphertext = "";
        try {
            SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, sf.generateSecret(secretKey));
            byte[] plainBytes = plaintext.getBytes();
            byte[] cipherBytes = cipher.doFinal(plainBytes);
            ciphertext = new String(Base64.getEncoder().encode(cipherBytes));
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(DES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ciphertext;
    }
    
    public String decryptDES(String ciphertext){
        String plaintext = "";
        try { 
            SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
            byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);  // Encrypted string as Base64 decoder.
            cipher.init(Cipher.DECRYPT_MODE, sf.generateSecret(secretKey));
            plaintext = new String(cipher.doFinal(cipherBytes));  // Decrypt the original message.
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(DES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return plaintext;
    }
}

