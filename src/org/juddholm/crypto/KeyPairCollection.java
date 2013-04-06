package org.juddholm.crypto;

import java.security.KeyPair;

import org.juddholm.mixnet.enums.EncryptionLayer;

public class KeyPairCollection {
	private KeyCollection privateKeys = new KeyCollection();
	private KeyCollection publicKeys = new KeyCollection();
	

	public KeyPairCollection()
	{
		
	}
	
	public void addKeyPair(EncryptionLayer stage, KeyPair pair)
	{
		privateKeys.addKey(stage, pair.getPrivate());
		publicKeys.addKey(stage, pair.getPublic());
	}
	
	public KeyCollection getPublicKeys()
	{
		return publicKeys;
	}
	
	public KeyCollection getPrivateKeys()
	{
		return privateKeys;
	}
}
