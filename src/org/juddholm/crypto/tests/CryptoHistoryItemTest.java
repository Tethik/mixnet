package org.juddholm.crypto.tests;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import org.juddholm.crypto.CryptoHistoryItem;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.junit.Before;
import org.junit.Test;

public class CryptoHistoryItemTest {
	
	private static int COLLECTION_SIZE = 10;

	private List<KeyPair> pairs;
	
	@Before
	public void setUp() throws Exception {
		pairs = TestHelper.generateKeypairs(2);
	}

	@Test
	public void testVerifySignature() {
		CryptoMessageCollection coll = new CryptoMessageCollection();	
		for(int i = 0; i < COLLECTION_SIZE; i++) 
			coll.add(new CryptoMessage(TestHelper.randomString()));
		coll.encrypt(pairs.get(0).getPublic());
		coll.encrypt(pairs.get(1).getPublic());
				
		// Create initial item
		CryptoHistoryItem item = new CryptoHistoryItem(coll);		
		item.setDecryptionKey(pairs.get(1).getPrivate());
		
		// 0 case
		assertTrue(item.verifySignature());
		
		// Clone, decrypt and sign
		CryptoMessageCollection next = coll.clone();
		next.decrypt(pairs.get(1).getPrivate());		
		next.sign(pairs.get(1).getPrivate());
		
		// Create next item
		CryptoHistoryItem nextItem = new CryptoHistoryItem(next);
		nextItem.setDecryptionKey(pairs.get(0).getPrivate());
		nextItem.setVerifyKey(pairs.get(1).getPublic());
		nextItem.setPrev(item);
		item.setNext(nextItem);
		
		assertTrue(item.verifySignature());
		assertTrue(nextItem.verifySignature());
		
		CryptoMessageCollection last = next.clone();
		last.decrypt(pairs.get(0).getPrivate());		
		last.sign(pairs.get(0).getPrivate());
		
		CryptoHistoryItem lastItem = new CryptoHistoryItem(last);
		lastItem.setVerifyKey(pairs.get(0).getPublic());
		lastItem.setPrev(nextItem);
		nextItem.setNext(lastItem);
		
		assertTrue(item.verifySignature());
		assertTrue(nextItem.verifySignature());
		assertTrue(lastItem.verifySignature());		
	}
	
	
	@Test
	public void testVerifyDecryption() {
		CryptoMessageCollection coll = new CryptoMessageCollection();	
		for(int i = 0; i < COLLECTION_SIZE; i++) 
			coll.add(new CryptoMessage(TestHelper.randomString()));
		coll.encrypt(pairs.get(0).getPublic());
		coll.encrypt(pairs.get(1).getPublic());
				
		// Create initial item
		CryptoHistoryItem item = new CryptoHistoryItem(coll);		
		item.setDecryptionKey(pairs.get(1).getPrivate());
		
		// 0 case
		assertTrue(item.verifyDecryption());
		
		// Clone, decrypt and sign
		CryptoMessageCollection next = coll.clone();
	
		next.decrypt(pairs.get(1).getPrivate());
		next.sign(pairs.get(1).getPrivate());
		
		// Create next item
		CryptoHistoryItem nextItem = new CryptoHistoryItem(next);
		nextItem.setDecryptionKey(pairs.get(0).getPrivate());
		nextItem.setVerifyKey(pairs.get(1).getPublic());
		nextItem.setPrev(item);
		item.setNext(nextItem);
		
		assertTrue(item.verifyDecryption());
		assertTrue(nextItem.verifyDecryption());
		
		CryptoMessageCollection last = next.clone();		
		last.decrypt(pairs.get(0).getPrivate());
		last.sign(pairs.get(0).getPrivate());
		
		CryptoHistoryItem lastItem = new CryptoHistoryItem(last);
		lastItem.setVerifyKey(pairs.get(0).getPublic());
		lastItem.setPrev(nextItem);
		nextItem.setNext(lastItem);
		
		assertTrue(item.verifyDecryption());
		assertTrue(nextItem.verifyDecryption());
		assertTrue(lastItem.verifyDecryption());
		
		
	}

}
