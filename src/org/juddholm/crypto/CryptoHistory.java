package org.juddholm.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.juddholm.mixnet.enums.EncryptionLayer;

public class CryptoHistory {

	private List<CryptoHistoryItem> items = new ArrayList<CryptoHistoryItem>();
	
	public Iterator<CryptoHistoryItem> getIterator()
	{
		return items.iterator();
	}
	
	public List<CryptoHistoryItem> getItems()
	{
		return items;
	}
	
	public void addItem(CryptoHistoryItem item)
	{
		if(items.size() > 0)
		{
			item.setPrev(items.get(items.size() - 1));
			items.get(items.size() -1).setNext(item);
		}
			
		items.add(item);
	}
	
	public CryptoHistoryItem get(int index)
	{
		return items.get(index);
	}
	
	public boolean removeItem(CryptoHistoryItem item)
	{
		if(item.getNext() != null)		
			item.getNext().setPrev(item.getPrev());		
		
		if(item.getPrev() != null)
			item.getPrev().setNext(item.getNext());
		
		return items.remove(item);
	}
	
	public int size()
	{
		return items.size();
	}
	
	public boolean verify()
	{
		for(CryptoHistoryItem item : items)
			if(!(item.verifyDecryption() && item.verifySignature()))
				return false;
		
		return true;
	}
	
	/*
	public static void createHistory(KeyPairCollection keys,
			Map<EncryptionLayer, List<CryptoMessageCollection>> history, List<EncryptionLayer> layers)
	{
		CryptoHistory history = new CryptoHistory();

		
		List<CryptoMessageCollection> toCheck = new ArrayList<CryptoMessageCollection>();
		List<PublicKey> publicKeys = new ArrayList<PublicKey>();
		List<PrivateKey> privateKeys = new ArrayList<PrivateKey>();
		
		// Lazy
		List<EncryptionLayer> layerIndexes = new ArrayList<EncryptionLayer>();
		
		boolean first = true;
		for(EncryptionLayer layer : layers) {			
			if(!history.containsKey(layer))
				continue;
			
			List<CryptoMessageCollection> collections = history.get(layer);
			for(Key key : keys.getPrivateKeys().getKeys(layer))
				privateKeys.add((PrivateKey) key);
			
			for(Key key : keys.getPublicKeys().getKeys(layer))
				publicKeys.add((PublicKey) key);
			
			toCheck.addAll(collections);
			layerIndexes.add(layer);
		}
	}
	*/
	
}
