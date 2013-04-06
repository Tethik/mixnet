package org.juddholm.crypto;

import java.io.Serializable;
import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.juddholm.mixnet.enums.EncryptionLayer;

public class KeyCollection implements Serializable {
	private Map<EncryptionLayer, List<Key> > keys = new HashMap<>();
	private int size = 0;
	
	public KeyCollection()
	{
		
	}
	
	public void addKey(EncryptionLayer stage, Key key)
	{
		if(!keys.containsKey(stage))
			keys.put(stage, new ArrayList<Key>());
		
		keys.get(stage).add(key);
		size++;
	}
	
	public boolean removeKey(EncryptionLayer stage, Key key)
	{
		if(!keys.containsKey(stage))
			return false;
		
		if(!keys.get(stage).remove(key))
			return false;
		
		size--;
		return true;		
	}
	
	public List<Key> getKeys(EncryptionLayer stage)
	{
		return keys.get(stage);
	}
	
	public Set<EncryptionLayer> getEncryptionStages()
	{
		return keys.keySet();
	}
	
	public void addEncryptionLayer(CryptoMessage msg, EncryptionLayer layer) {
		List<Key> stageKeys = keys.get(layer);
		Collections.reverse(stageKeys);
		for(Key key : stageKeys) {
			System.out.println("Encrypting with key: " + key.hashCode());
			msg.encrypt((PublicKey) key);
		}
		Collections.reverse(stageKeys); // stupid references :(
		System.out.println("# Encrypted layer " + layer.toString());
	}
	
	public void union(KeyCollection collection)
	{
		for(EncryptionLayer stage : collection.getEncryptionStages())
		{
			for(Key key : collection.getKeys(stage))
				addKey(stage, key);
		}
	}
	
	public int size()
	{
		return size;
	}
	
}

