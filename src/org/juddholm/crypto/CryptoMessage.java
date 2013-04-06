package org.juddholm.crypto;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import javax.management.RuntimeErrorException;


/***
 * Encapsulates a message with an AES encryption with a public key encrypted AES-key. It can only be unlocked 
 * by using the private key. Symmetric encryption is generally faster to perform and works much better for larger texts.
 * @author Tethik
 */
public class CryptoMessage implements Serializable {
	
	protected static String SYMMETRIC_ALGO = "AES";
	protected static String ASYMMETRIC_ALGO = "RSA";
	protected static String SIGNING_ALGO = "SHA1withRSA";
	
	private byte[] data;
	protected Stack<byte[]> layerKeys = new Stack<>();

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
	
	protected SecretKey generateKey() throws NoSuchAlgorithmException 
	{
		return KeyGenerator.getInstance(SYMMETRIC_ALGO).generateKey();		
	}
	
	public void encrypt(PublicKey publicKey)
	{
		try {
			SecretKey key = generateKey();
			encrypt(publicKey, key);			
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Encryption of cryptomessage failed.", e);
		}
		
	}
	
	public void encrypt(PublicKey publicKey, SecretKey key) {		 
		try {
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGO);		
			cipher.init(Cipher.ENCRYPT_MODE, key);
			data = cipher.doFinal(data); // Encrypt using symmetric key
			
			// Add encrypted AES key to list of keys.
			Cipher rsaCipher = Cipher.getInstance(ASYMMETRIC_ALGO);
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			layerKeys.push(rsaCipher.doFinal(key.getEncoded()));
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
		
		try {
			Cipher rsaCipher = Cipher.getInstance(ASYMMETRIC_ALGO);
			rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decrypted_aes_key = rsaCipher.doFinal(encrypted_aes_key);
			
			SecretKey key = new SecretKeySpec(decrypted_aes_key, SYMMETRIC_ALGO);
			Cipher aesCipher = Cipher.getInstance(SYMMETRIC_ALGO);
			aesCipher.init(Cipher.DECRYPT_MODE, key);
			dataCopy = aesCipher.doFinal(data);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IllegalAccessException("Decryption of cryptomessage failed: " + e.toString());
		}
		
		data = dataCopy;
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
		int i = 0;
		for(byte[] layerKey : layerKeys)
			clone.layerKeys.add(i++, layerKey);
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
		// Signature equal
		if(!Arrays.equals(signature, msg.signature))
			return false;
		*/
		
		// Data equal		
		if(!Arrays.equals(data, msg.data))
			return false;
		
		// Layers equal
		if(layerKeys.size() != msg.getLayers())
			return false;	
		
		for(int i = 0; i < layerKeys.size(); i++)
			if(!Arrays.equals(layerKeys.get(i),msg.layerKeys.get(i)))
				return false;
		
		
		return true;
	}

}
