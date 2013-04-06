package org.juddholm.crypto;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CryptoMessageCollection implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2335644102959232680L;
	
	private List<CryptoMessage> messages = new ArrayList<CryptoMessage>();
	
	public CryptoMessageCollection()
	{
		
	}
	
	public void add(CryptoMessage msg)
	{
		messages.add(msg);
	}
	
	public void remove(CryptoMessage msg)
	{
		messages.remove(msg);
	}
	
	public CryptoMessage get(int index)
	{
		return messages.get(index);
	}
	
	public List<CryptoMessage> getList()
	{
		return messages;
	}	
	
	public boolean checkSignatures(PublicKey publicKey)
	{
		for(CryptoMessage msg : messages)
		{
			if(!msg.verifySignature(publicKey))
				return false;
		}
		return true;
	}
	
	/***
	 * Decrypts the collection using the given private key.
	 * Any CryptoMessage which could not be decrypted is removed from the collection.
	 * Returns the number of removed CryptoMessages.
	 * 
	 * @param privateKey The private key to decrypt with
	 * @return The number of removed CryptoMessages.
	 */
	public int decrypt(PrivateKey privateKey) 
	{
		List<CryptoMessage> toRemove = new ArrayList<CryptoMessage>();
		int i = 0;
		for(CryptoMessage msg : messages)
		{
			try {
				msg.decrypt(privateKey);
			}
			catch(IllegalAccessException ex) {
				System.out.println("Failed to decrypt msg #"+i);
				System.out.println(ex.toString());
				toRemove.add(msg);
			}
			i++;
		}		
		messages.removeAll(toRemove);
		return toRemove.size();
	}
	
	/***
	 * Shuffles the collection by using SecureRandom. Does not use in-place memory.
	 * Should run in O(n) time.
	 */
	public void shuffle()
	{
		SecureRandom random = new SecureRandom();
		ArrayList<CryptoMessage> newPermutation = new ArrayList<CryptoMessage>();
		while(messages.size() > 0)
		{
			int index = random.nextInt(messages.size());
			newPermutation.add(messages.remove(index));			
		}
		messages = newPermutation;
	}
	
	/**
	 * Sorts the collection by comparing as strings. 
	 */
	public void sort()
	{
		Collections.sort(messages, new Comparator<CryptoMessage>() {
			@Override
			public int compare(CryptoMessage o1, CryptoMessage o2) {			
				return o1.getMessage().compareTo(o2.getMessage());
			}
		});
	}
	
	public void sign(PrivateKey privateKey)
	{
		for(CryptoMessage msg : messages)
		{
			msg.sign(privateKey);
		}
	}
	
	public void encrypt(PublicKey publicKey)
	{
		for(CryptoMessage msg : messages)
		{
			msg.encrypt(publicKey);
		}
	}
	
	public int size()
	{
		return messages.size();
	}
	
	public int layers()
	{
		if(messages.size() > 0)			
			return messages.get(0).getLayers();
		return 0;
	}
	
	
	public CryptoMessageCollection clone()
	{
		CryptoMessageCollection clone = new CryptoMessageCollection();
		for(CryptoMessage msg : messages)
			clone.add(msg.clone());
		return clone;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof CryptoMessageCollection))
			return false;
		
		// Cast
		CryptoMessageCollection coll = (CryptoMessageCollection) object;
		
		// Check each message
		if(coll.size() != coll.size())
			return false;
		
		// Because of the shuffle the order is indeterminate. We need to make
		// sure each message is there though!
		// This can take a very long time (O(n^2)) depending on the amount of messages.
		// Prefereably this should be replaced by a secure hash table, where the hash has
		// to be computed by a secure algorithm such as SHA256
		return messages.containsAll(coll.messages);
	}
	
	
}
