package Encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyAgreement;

public class ECDHKeyExchange {
    private KeyPairGenerator kpg;
    private KeyPair keyPair;
    private PublicKey theirKey;
    private KeyAgreement keyAgree;
    private int KEYSIZE;
    private AlgorithmParameterSpec dhecParamSpec;
    
    public ECDHKeyExchange(){
        this.initializeDHECParameters();
    }
    
    private AlgorithmParameterSpec getECDHParameterSpec() {
        AlgorithmParameterSpec retValue = new ECGenParameterSpec("secp224r1");
        return retValue;
    }
    
    private void initializeDHECParameters(){
        this.dhecParamSpec = this.getECDHParameterSpec();
        try {
            this.kpg = KeyPairGenerator.getInstance("EC");
            this.kpg.initialize(this.dhecParamSpec);
            this.keyPair = kpg.generateKeyPair();
            this.keyAgree = KeyAgreement.getInstance("ECDH");
            this.keyAgree.init(this.returnMyPrivateKey());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException ex) {
            Logger.getLogger(ECDHKeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public PublicKey returnMyPublicKey(){
        return keyPair.getPublic();
        
    }
    public PrivateKey returnMyPrivateKey(){
        return keyPair.getPrivate();
    }
    
    public void setTheirPublicKey(byte[] theirs){
        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            this.theirKey = kf.generatePublic(new X509EncodedKeySpec(theirs));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(ECDHKeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public PublicKey returnTheirPublicKey(){
        return this.theirKey;
    }

    public byte[] computeSharedSecret(){
        byte[] retVal = new byte[16];
        try {
            this.keyAgree.doPhase(returnTheirPublicKey(), true);
            byte[] total = this.keyAgree.generateSecret();
            
            // Use the last 16 bytes of shared secret.
            int start = total.length - 16;
            for (int c = start; c < total.length;c++){
                retVal[c - start] = total[c];
            }
        } catch (InvalidKeyException | IllegalStateException ex) {
            Logger.getLogger(ECDHKeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retVal;
    }
}