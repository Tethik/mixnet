package org.juddholm.crypto.tests;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.junit.Before;
import org.junit.Test;

public class CryptoMessageCollectionTest {
	
	private static int MESSAGE_COUNT = 20;
	private static int CHAIN_SIZE = 5;
	
	private List<KeyPair> pairs;
	private List<String> plaintexts;
	private CryptoMessageCollection collection;
	
	
	@Before
	public void setUp() throws Exception {		
		pairs = new ArrayList<>();
		plaintexts = new ArrayList<String>();
		
		for(int i = 0; i < CHAIN_SIZE; i++)
		{
			pairs.add(KeyPairGenerator.getInstance("RSA").genKeyPair());
		}
		
		collection = new CryptoMessageCollection();
		
		for(int i = 0; i < MESSAGE_COUNT; i++)
		{
			String plaintextData = TestHelper.randomString(); 
			plaintexts.add(plaintextData);
			CryptoMessage msg = new CryptoMessage(plaintextData);
			collection.add(msg);		
		}
		
		for(int i = 0; i < CHAIN_SIZE; i++)
		{
			List<CryptoMessage> msgs = collection.getList();
			for(CryptoMessage msg : msgs)
			{
				msg.encrypt(pairs.get(i).getPublic());
			}
		}

	}

	@Test
	public void testCheckSignatures() {
		for(int i = CHAIN_SIZE - 1; i > -1; i--)
		{
			PrivateKey privateKey = pairs.get(i).getPrivate();
			PublicKey publicKey = pairs.get(i).getPublic();
			List<CryptoMessage> msgs = collection.getList();
			for(CryptoMessage msg : msgs)
			{					
				try {
					msg.decrypt(privateKey);
				} catch (IllegalAccessException e) {
					fail("Decryption failed. Check CryptoMessageTest");
				}				
				msg.sign(privateKey);
			}				
			assertTrue(collection.checkSignatures(publicKey));
			if(i > 0) {
				collection.get(0).sign(pairs.get(0).getPrivate());
				assertFalse(collection.checkSignatures(publicKey));
			}
		}
	}
	
	@Test
	public void testCloneAndEquals()
	{
		CryptoMessageCollection clone = collection.clone();
		
		// Check so that collections are equal.
		assertTrue(clone.equals(collection));
		assertTrue(collection.equals(clone));		
		collection.decrypt(pairs.get(CHAIN_SIZE - 1).getPrivate());
		assertFalse(clone.equals(collection));
		
	}

}
