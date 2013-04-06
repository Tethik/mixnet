package org.juddholm.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import org.juddholm.mixnet.enums.EncryptionLayer;

public class KeyCollectionFactory {
	
	public static String ENCRYPTION_ALGO = "RSA";	
	
	public static KeyPairCollection generateKeyPairs() throws NoSuchAlgorithmException
	{
		KeyPairCollection collection = new KeyPairCollection();	
		
		for(EncryptionLayer layer : EncryptionLayer.values())
		{
			KeyPair pair = KeyPairGenerator.getInstance(ENCRYPTION_ALGO).generateKeyPair();
			collection.addKeyPair(layer, pair);
		}
		
		return collection;
	}

}
