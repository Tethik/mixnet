package org.juddholm.mixnet.mixserver;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.juddholm.crypto.ConcatCryptoMessage;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.crypto.KeyCollectionFactory;
import org.juddholm.crypto.KeyPairCollection;
import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.enums.VerificationStatus;
import org.juddholm.mixnet.enums.VerificationType;

public class MixServer implements Runnable {
	
	private static EncryptionLayer[] layers = {EncryptionLayer.OUTER, EncryptionLayer.Mix1, EncryptionLayer.Mix2, EncryptionLayer.REPETITION, EncryptionLayer.FINAL};
	private CryptoMessageCollection collection;
	private KeyPairCollection keys;	
	private OutputHandler output = null;
	private InfoHandler info = null;
	
	private int repetitions;
	
	public MixServer(OutputHandler outputHandler, InfoHandler infoHandler)  {			
		this.output = outputHandler;
		this.info = infoHandler;
		
		try {
			keys = KeyCollectionFactory.generateKeyPairs();
			
			for(EncryptionLayer layer : layers)
				System.out.println(layer + " key: " + keys.getPublicKeys().getKeys(layer).get(0).hashCode());

			
		} catch (NoSuchAlgorithmException e) {
			// TODO Fix exception?
			e.printStackTrace();
		}
		info.releasePublicKeys(keys.getPublicKeys());
	}	
	
	public KeyPairCollection getKeys()
	{
		return keys;
	}
	
	private synchronized void waitForList() 
	{
		while(collection == null)
		{
			try {
				wait();
			} catch(InterruptedException ex) {  }
		}
	}

	public boolean decrypt(EncryptionLayer layer) 
	{
		System.out.println("# Waiting for my turn to decrypt for stage " + layer);
		waitForList();
		
		if(layer == EncryptionLayer.FINAL && output.isFirst())
		{
			cleanupDuplicates();
			List<CryptoMessage> dummies =  output.getDummies();
			System.out.println("# Removing " + dummies.size() + " dummies");
			for(CryptoMessage msg : dummies)
				collection.remove(msg);
		}
		
		/*
		if(layer == EncryptionLayer.Mix2 && output.isLast())
			collection.remove(collection.get(0)); // Simulated evilness
		*/
		
		PrivateKey key = (PrivateKey) keys.getPrivateKeys().getKeys(layer).get(0);
		PublicKey pkey = (PublicKey) keys.getPublicKeys().getKeys(layer).get(0);	
		System.out.println("# Decrypting with key: " + key.hashCode() + " public: " + pkey.hashCode());
		/*
		for(CryptoMessage msg : collection.getList()) {
			System.out.println(msg.getData().length + " " + msg.getLayers());
		}
		*/
		collection.decrypt(key);				
	
		
		if(output.isLast() && layer == EncryptionLayer.OUTER)
		{
			// Each CryptoMessage should be a concatcryptomessage now.
			// Split into repetitions.
			CryptoMessageCollection newColl = new CryptoMessageCollection();
			for(CryptoMessage _msg : collection.getList()) {
				ConcatCryptoMessage concmsg = (ConcatCryptoMessage) _msg;
				
				// Ignore any msgs sent with a different count of messages than the agreed amount
				if(concmsg.getNumberOfMessages() != repetitions)
					continue;
				
				CryptoMessage[] msgs = concmsg.split();				
				System.out.println("# Split ConcatMessage into " + msgs.length);
				
				for(CryptoMessage msg : msgs) {
					newColl.add(msg);
					System.out.println(msg.getData().length + " " + msg.getLayers());
				}
			}
			collection = newColl;
		} else if(output.isLast() && layer == EncryptionLayer.REPETITION) {
			
			System.out.println("# Sorting collection");
			collection.sort();			
		}		
			
		if(layer == EncryptionLayer.Mix1 || layer == EncryptionLayer.Mix2)
			collection.shuffle();		
		
		return true;		
	}
	
	public void cleanupDuplicates() {
		CryptoMessageCollection newColl = new CryptoMessageCollection();
		int copies = 0;
		CryptoMessage prev = collection.get(0);
		for(int i = 0; i < collection.size(); i++)
		{
			CryptoMessage curr = collection.get(i);
			
			if(prev.equals(curr)) {
				copies++;
			} else {	
				if(copies == repetitions)
				{
					newColl.add(prev);
				}
				copies = 1;
			}		
			
			prev = curr;				
		}
		
		if(copies == repetitions)
		{
			newColl.add(prev);
		}	
		collection = newColl;
	}
	
	public synchronized void sendCollection(EncryptionLayer layer) {
		PrivateKey key = (PrivateKey) keys.getPrivateKeys().getKeys(layer).get(0);
		//collection.sign(key);
		output.putCryptoCollection(collection, layer);
		info.releaseCollection(collection);
		collection = null;
	}
	
	public CryptoMessageCollection getCollection()
	{
		return collection;
	}

	private boolean verify(VerificationType type) {	
		List<VerificationStatus> statuses = output.getVerifications(type);
		
		for(VerificationStatus status : statuses)
			if(status != VerificationStatus.PASS)
				return false;
				
		return true;
	}

	public synchronized void putCryptoCollection(CryptoMessageCollection coll) {
		this.collection = coll;		
		notifyAll();
	}

	@Override
	public void run() {	
		for(EncryptionLayer layer : layers)
		{				
			if(!decrypt(layer))
			{
				System.out.println("# Decryption failed!");
				break;
			}
			
			sendCollection(layer);			
			System.out.println("# Finished sending");
			System.out.println();
			
			if(layer == EncryptionLayer.Mix2) {			
				PrivateKey mix1Key = (PrivateKey) keys.getPrivateKeys().getKeys(EncryptionLayer.Mix1).get(0);
				PrivateKey mix2Key = (PrivateKey) keys.getPrivateKeys().getKeys(EncryptionLayer.Mix2).get(0);
				PrivateKey outerKey = (PrivateKey) keys.getPrivateKeys().getKeys(EncryptionLayer.OUTER).get(0);
				info.releasePrivateKey(EncryptionLayer.OUTER, outerKey);
				info.releasePrivateKey(EncryptionLayer.Mix1, mix1Key);
				info.releasePrivateKey(EncryptionLayer.Mix2, mix2Key);
				System.out.println("# Waiting for explicit verification");
				
				if(!verify(VerificationType.Explicit))
				{
					System.out.println("# Explicit Verification failed!");
					break;
				} else {
					System.out.println("# Explicit Verification passed!");
				}			
		
			}			
			else if(layer == EncryptionLayer.REPETITION) {
				
				System.out.println("# Waiting for tracing verification");
				if(!verify(VerificationType.Tracing))
				{
					System.out.println("# Tracing Verification failed!");
					break;
				} else {
					System.out.println("# Tracing Verification passed!");
				}
				
				System.out.println("# Waiting for dummy verification");
				if(!verify(VerificationType.Dummies))
				{
					System.out.println("# Dummy Verification failed!");
					break;
				} else {
					System.out.println("# Dummy Verification passed!");
				}
			}
			
			
		}
		
		
		
		
		
	}

	/**
	 * @return the repetitions
	 */
	public int getRepetitions() {
		return repetitions;
	}

	/**
	 * @param repetitions the repetitions to set
	 */
	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}

}
