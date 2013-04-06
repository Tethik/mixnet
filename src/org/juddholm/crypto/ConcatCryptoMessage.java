package org.juddholm.crypto;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Stack;

import javax.crypto.SecretKey;

public class ConcatCryptoMessage extends CryptoMessage implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7033670712549408699L;
	protected int numberOfMessages;
	protected int concatLayers = 0;
	protected CryptoMessage[] messages;
	
	
	public ConcatCryptoMessage(CryptoMessage[] messages) 
	{
		if(messages == null || messages.length < 1)
			throw new IllegalArgumentException("Messagelist should contain at least one message");
		
		this.messages = messages;
		this.numberOfMessages = messages.length;
		
		/*
		int size = messages[0].getData().length;
		byte[] data = new byte[size*messages.length];
		int dp = 0;
		for(CryptoMessage msg : messages) {
			if(msg.getData().length != size)
				throw new IllegalArgumentException("Mismatching message lengths.");
			
			for(int i = 0; i < msg.getData().length; i++,dp++)
				data[dp] = msg.getData()[i];				
		}
		
		this.numberOfMessages = messages.length;		
		try {
			this.setData(data);
		}
		catch(IllegalAccessException ex)
		{
			// Should not happen. 
		}	
		
		this.layerKeys = new Stack<byte[]>();
		int i = 0;
		for(byte[] layerKey : messages[0].layerKeys)
			this.layerKeys.add(i++, layerKey);
		if(messages[0].signature != null)
			this.signature = messages[0].signature.clone();
		*/

	}
	
	public CryptoMessage[] split()
	{
		if(concatLayers > 0)
			throw new IllegalStateException("Object needs to be decrypted of layers first.");
		
		return messages;
		
		/*
		int subsize = getData().length / numberOfMessages;
		CryptoMessage[] messages = new CryptoMessage[numberOfMessages];
		for(int i = 0; i < numberOfMessages; i++) {
			CryptoMessage msg = new CryptoMessage();
			byte[] data = Arrays.copyOfRange(getData(), subsize*i, subsize + subsize*i);
			assert(data.length == subsize);
			try {
				msg.setData(data);
			}
			catch(IllegalAccessException ex)
			{
				// Should not happen. 
			}
			msg.layerKeys = new Stack<byte[]>();
			int x = 0;
			for(byte[] layerKey : this.layerKeys)
				msg.layerKeys.add(x++, layerKey);
			
			if(this.signature != null)
				msg.signature = this.signature.clone();
			
			
			messages[i] = msg;					
		}
		
		return messages;*/		
	}
	
	public int getNumberOfMessages()
	{
		return numberOfMessages;
	}
	
	public int getLayers()
	{
		return concatLayers;
	}
	
	@Override
	public void encrypt(PublicKey publicKey)
	{
		
		try {
			SecretKey key = generateKey();
			for(CryptoMessage msg : messages)
				msg.encrypt(publicKey, key);	
			this.concatLayers++;
		}			
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Encryption of concatcryptomessage failed: " + e.toString());
		}
	}
	
	@Override
	public void decrypt(PrivateKey privateKey) throws IllegalAccessException
	{
		for(CryptoMessage msg : messages)
			msg.decrypt(privateKey);
		this.concatLayers--;
	}
	
	@Override
	public void sign(PrivateKey privateKey) {
		for(CryptoMessage msg : messages)
			msg.sign(privateKey);
	}
	
}
