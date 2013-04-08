package org.juddholm.crypto;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/***
 * Encapsulates a message with an AES encryption with a public key encrypted AES-key. It can only be unlocked 
 * by using the private key. Symmetric encryption is generally faster to perform and works much better for larger texts.
 * @author Tethik
 */
public class CryptoMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7793342071795618597L;
	protected static String SYMMETRIC_ALGO = "AES";
	protected static String ASYMMETRIC_ALGO = "RSA";
	protected static String SIGNING_ALGO = "SHA1withRSA";
	private static int SEED_SIZE = 16;
	
	private byte[] data;
	protected Stack<byte[]> layerKeys = new Stack<>();
	protected Stack<byte[]> layerSeeds = new Stack<byte[]>();
	
	//protected Stack<PublicKey> rsaKeys = new Stack<PublicKey>();
	
	protected Stack<byte[]> removedLayerKeys = new Stack<>();
	protected Stack<byte[]> removedLayerSeeds = new Stack<>();
	
	
	
	// Signature to verify that this message was indeed decrypted by the correct key 
	protected byte[] signature;

	public CryptoMessage() {}
	
	public CryptoMessage(String message)
	{
		try {
			setMessage(message);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	public byte[] getData()
	{
		return data;
	}
	
	public void setData(byte[] data) throws IllegalAccessException
	{
		if(layerKeys.size() != 0)
			throw new IllegalAccessException("CryptoMessage already contains an encrypted message!");
		
		this.data = data;
	}

	public String getMessage() {
		return new String(data);
	}
	
	public int getLayers()
	{
		return layerKeys.size();
	}

	public void setMessage(String message) throws IllegalAccessException {
		setData(message.getBytes());
	}
	
	protected SecretKey generateKey(byte[] seed) throws NoSuchAlgorithmException 
	{
		KeyGenerator generator = KeyGenerator.getInstance(SYMMETRIC_ALGO); 
		generator.init(new SecureRandom(seed));
		return generator.generateKey();
	}
	
	
	private byte[] createSeed()
	{
		byte[] seed = new byte[SEED_SIZE];
		SecureRandom random = new SecureRandom();
		random.nextBytes(seed);
		return seed;
	}
	
	
	public void reencrypt(PublicKey publicKey)
	{	
		if(removedLayerKeys.size() == 0)
			throw new IllegalArgumentException("No layer to reencrypt");
		
		SecretKey key = new SecretKeySpec(removedLayerKeys.pop(), SYMMETRIC_ALGO);
		encrypt(publicKey, key, removedLayerSeeds.pop());		
	}
	
	public void encrypt(PublicKey publicKey)
	{
		try {
			byte[] seed = createSeed();
			SecretKey key = generateKey(seed);	
			seed = createSeed();
			encrypt(publicKey, key, seed);			
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Encryption of cryptomessage failed.", e);
		}
		
	}
	
	public void encrypt(PublicKey publicKey, SecretKey key, byte[] seed) {		 
		try {
			System.out.println(new String(key.getEncoded()) + ": "+ new String(seed));
	
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGO);		
			cipher.init(Cipher.ENCRYPT_MODE, key, new SecureRandom(seed));
			data = cipher.doFinal(data); // Encrypt using symmetric key
			
			// Add encrypted AES key to list of keys.
			Cipher rsaCipher = Cipher.getInstance(ASYMMETRIC_ALGO);
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			layerKeys.push(rsaCipher.doFinal(key.getEncoded()));
						
			byte[] encrypted_seed = cipher.doFinal(seed);
			layerSeeds.push(encrypted_seed);			
			//rsaKeys.push(publicKey);
			
			assert(layerSeeds.size() == layerKeys.size());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
			throw new RuntimeException("Encryption of cryptomessage failed.", e);		
		}
	}
	
	public void decrypt(PrivateKey privateKey) throws IllegalAccessException
	{
		if(layerKeys.size() <= 0)
			throw new IllegalAccessException("No encryption layer to decrypt");
	
		
		byte[] dataCopy = data;		
		byte[] encrypted_aes_key = layerKeys.peek();
		byte[] encrypted_seed = layerSeeds.peek();
		//PublicKey pubkey = rsaKeys.peek();
		
		try {
			Cipher rsaCipher = Cipher.getInstance(ASYMMETRIC_ALGO);
			rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decrypted_aes_key = rsaCipher.doFinal(encrypted_aes_key);
			
			SecretKey key = new SecretKeySpec(decrypted_aes_key, SYMMETRIC_ALGO);
			Cipher aesCipher = Cipher.getInstance(SYMMETRIC_ALGO);
			aesCipher.init(Cipher.DECRYPT_MODE, key);
			dataCopy = aesCipher.doFinal(data);
		
			//byte[] encrypted_seed = seedMap.get(pubkey);
			byte[] decrypted_seed = aesCipher.doFinal(encrypted_seed);
			
			removedLayerKeys.push(key.getEncoded());
			removedLayerSeeds.push(decrypted_seed);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IllegalAccessException("Decryption of cryptomessage failed: " + e.toString());
		}
		
		data = dataCopy;
		//rsaKeys.pop();
		layerSeeds.pop();
		layerKeys.pop(); // Everything went ok. Pop the layer.
	}
	
	public void sign(PrivateKey privateKey)
	{
		try {
			Signature sign = Signature.getInstance(SIGNING_ALGO);
			sign.initSign(privateKey);
			sign.update(data);
			signature = sign.sign();
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
			throw new RuntimeException("CryptoMessage signing failed.", e);			
		}
	}
	
	public boolean verifySignature(PublicKey publicKey)
	{		
		if(signature == null)
			return false;
		
		try {			
			Signature sign = Signature.getInstance(SIGNING_ALGO);
			sign.initVerify(publicKey);
			sign.update(data);
			return sign.verify(signature);
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
			return false;
		}
	}
	
	@Override
	public CryptoMessage clone()
	{
		CryptoMessage clone = new CryptoMessage();
		clone.data = this.data.clone();
		if(this.signature != null)
			clone.signature = this.signature.clone();
		
		clone.layerKeys = new Stack<byte[]>();
		clone.layerSeeds = new Stack<byte[]>();
		//clone.rsaKeys = new Stack<PublicKey>();
		clone.removedLayerKeys = new Stack<byte[]>();
		clone.removedLayerSeeds = new Stack<byte[]>();
		
		int i = 0;
		for(byte[] layerKey : layerKeys)		
			clone.layerKeys.add(i++, layerKey);
		
		i = 0;
		for(byte[] layerSeed : layerSeeds)		
			clone.layerSeeds.add(i++, layerSeed);
		
		/*
		i=0;		
		for(PublicKey publicKey : rsaKeys)
			clone.rsaKeys.add(i++, publicKey);
		*/
		i=0;
		for(byte[] layerKey : removedLayerKeys)
			clone.removedLayerKeys.add(i++, layerKey);
		
		i=0;
		for(byte[] layerSeeds : removedLayerSeeds)
			clone.removedLayerSeeds.add(i++, layerSeeds);
		
		return clone;				
	}
	
	public byte[] getSignature()
	{
		return signature;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof CryptoMessage))
			return false;
		
		CryptoMessage msg = (CryptoMessage) object;
		/*
		// Signature equal only happens if cloned
		if(!Arrays.equals(signature, msg.signature))
			return false;
		*/
		
		// Data equal		
		if(!Arrays.equals(data, msg.data))
			return false;
		
		// Layers equal
		if(layerKeys.size() != msg.layerKeys.size()|| layerSeeds.size() != msg.layerSeeds.size())
			return false;	
		
		/*
		 * Causes reencryptions to not be equal
		for(int i = 0; i < layerKeys.size(); i++)
			if(!Arrays.equals(layerKeys.get(i),msg.layerKeys.get(i)))
				return false;
		*/
		
		return true;
	}

}
