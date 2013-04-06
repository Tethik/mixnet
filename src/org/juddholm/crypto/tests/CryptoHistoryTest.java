package org.juddholm.crypto.tests;

import static org.junit.Assert.*;

import org.juddholm.crypto.CryptoHistory;
import org.juddholm.crypto.CryptoHistoryItem;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;
import java.util.List;

public class CryptoHistoryTest {

	private int COLLECTION_SIZE = 10;
	private int CHAIN_SIZE = 10;
	private CryptoHistory history;
	private List<KeyPair> pairs;
	
	@Before
	public void setUp() throws Exception {
		history = new CryptoHistory();
		pairs = TestHelper.generateKeypairs(CHAIN_SIZE);
	}

	@Test
	public void testAddItem() {
		CryptoMessageCollection coll = new CryptoMessageCollection();	
		for(int i = 0; i < COLLECTION_SIZE; i++) 
			coll.add(new CryptoMessage(TestHelper.randomString()));
		
		CryptoHistoryItem item = new CryptoHistoryItem(coll);
		history.addItem(item);
		assertTrue(history.size() == 1);		
		item = new CryptoHistoryItem(coll);
		history.addItem(item);
		assertTrue(history.size() == 2);
		assertTrue(history.get(0).getPrev() == null);
		assertTrue(history.get(1).getPrev() == history.get(0));
	}

	@Test
	public void testRemoveItem() {
		testAddItem();
		history.removeItem(history.get(0));
		assertTrue(history.get(0).getPrev() == null);		
	}

	@Test
	public void testVerify() {
		CryptoMessageCollection coll = new CryptoMessageCollection();	
		for(int i = 0; i < COLLECTION_SIZE; i++) 
			coll.add(new CryptoMessage(TestHelper.randomString()));
		
		for(int i = 0; i < CHAIN_SIZE; i++) 
			coll.encrypt(pairs.get(i).getPublic());
		
		CryptoHistoryItem item = new CryptoHistoryItem(coll);
		item.setDecryptionKey(pairs.get(CHAIN_SIZE-1).getPrivate());
		history.addItem(item);
		
		for(int i = CHAIN_SIZE - 1; i > -1; i--)
		{
			CryptoMessageCollection clone = coll.clone();		
			clone.decrypt(pairs.get(i).getPrivate());
			clone.sign(pairs.get(i).getPrivate());
	
			// Create initial item
			item = new CryptoHistoryItem(clone);
			if(i > 0) {
				item.setDecryptionKey(pairs.get(i-1).getPrivate());				
			}			
			item.setVerifyKey(pairs.get(i).getPublic());
			history.addItem(item);
			coll = clone;
		}				

		assertTrue(history.verify()); 

	}

}
