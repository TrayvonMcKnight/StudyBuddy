package Encryption;


import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
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
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

public class KeyExchange{
    protected KeyPairGenerator kpg;
    protected KeyPair keyPair;
    protected PublicKey theirKey;
    protected KeyAgreement keyAgree;
    protected int KEYSIZE;
    
    public PublicKey returnMyPublicKey(){
        return keyPair.getPublic();
        
    }
    public PrivateKey returnMyPrivateKey(){
        return keyPair.getPrivate();
    }
}

class DHKeyExchange extends KeyExchange {
    protected DHParameterSpec dhparamSpec;
    public DHKeyExchange(int keysize){
        super.KEYSIZE = keysize;
    }
    
    public void setTheirPublicKey(byte[] theirs){
        try {
            KeyFactory kf = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(theirs);
            super.theirKey = kf.generatePublic(x509Spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(KeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public PublicKey returnTheirPublicKey(){
        return super.theirKey;
    }
    public void generateParameters(){
        try {
            
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(super.KEYSIZE);
            AlgorithmParameters params = paramGen.generateParameters();
            this.dhparamSpec = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);
            super.kpg = KeyPairGenerator.getInstance("DiffieHellman");
            super.kpg.initialize(dhparamSpec);
            super.keyPair = super.kpg.genKeyPair();
            super.keyAgree = KeyAgreement.getInstance("DiffieHellman");
            super.keyAgree.init(keyPair.getPrivate());
            
        } catch (NoSuchAlgorithmException | InvalidParameterSpecException ex) {
            Logger.getLogger(KeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException ex) {
            Logger.getLogger(DHKeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setDHParameterSpec(){
        try {
            PublicKey pubKey = returnTheirPublicKey();
            this.dhparamSpec = ((DHPublicKey) pubKey).getParams();
            super.kpg = KeyPairGenerator.getInstance("DiffieHellman");
            super.kpg.initialize(this.dhparamSpec);
            super.keyPair = super.kpg.genKeyPair();
            super.keyAgree = KeyAgreement.getInstance("DiffieHellman");
            super.keyAgree.init(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException ex) {
            Logger.getLogger(DHKeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public byte[] computeSharedSecret(){
        try {
            super.keyAgree.doPhase(returnTheirPublicKey(), true);
        } catch (InvalidKeyException | IllegalStateException ex) {
            Logger.getLogger(DHKeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
        return super.keyAgree.generateSecret();
    }
}

class ECDHKeyExchange extends KeyExchange {
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
            super.kpg = KeyPairGenerator.getInstance("EC");
            super.kpg.initialize(this.dhecParamSpec);
            super.keyPair = kpg.generateKeyPair();
            super.keyAgree = KeyAgreement.getInstance("ECDH");
            super.keyAgree.init(super.returnMyPrivateKey());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException ex) {
            Logger.getLogger(ECDHKeyExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            super.keyAgree.doPhase(returnTheirPublicKey(), true);
            byte[] total = super.keyAgree.generateSecret();
            
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