package org.juddholm.crypto.tests;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.juddholm.crypto.CryptoHistory;
import org.juddholm.crypto.CryptoHistoryItem;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.crypto.ExplicitVerification;
import org.juddholm.crypto.VerificationResult;
import org.juddholm.mixnet.enums.VerificationStatus;
import org.junit.Before;
import org.junit.Test;

public class ExplicitVerificationTest {
	
	private int COLLECTION_SIZE = 10;
	private int CHAIN_SIZE = 10;
	
	private ExplicitVerification veri;
	@Before
	public void setUp() throws Exception {
		veri = new ExplicitVerification(null, null);
	}

	
	@Test
	public void testVerify() throws NoSuchAlgorithmException {
		CryptoHistory history = new CryptoHistory();
		
		List<String> messages = new ArrayList<String>();
		for(int i = 0; i < COLLECTION_SIZE; i++)		
			messages.add(TestHelper.randomString());
		
		List<KeyPair> pairs = new ArrayList<KeyPair>();
		for(int i = 0; i < CHAIN_SIZE; i++)
			pairs.add(KeyPairGenerator.getInstance("RSA").generateKeyPair());
		
		Stack<CryptoHistoryItem> items = new Stack<>(); 
		for(int i = 0; i < CHAIN_SIZE; i++)
		{
			CryptoMessageCollection coll = new CryptoMessageCollection();
			for(String msg : messages) {
				CryptoMessage m = new CryptoMessage(msg);
				for(int x = 0; x < i; x++)
				{
					m.encrypt(pairs.get(x).getPublic());					
				}		
				m.sign(pairs.get(i).getPrivate());
				coll.add(m);
			}
			
			CryptoHistoryItem item = new CryptoHistoryItem(coll);
			if(i > 0) {
				item.setDecryptionKey(pairs.get(i).getPrivate());
				item.setVerifyKey(pairs.get(i).getPublic());
				items.push(item);
			}
		}
		
		CryptoHistoryItem prev = null;
		while(items.size() > 0)
		{
			CryptoHistoryItem item = items.pop();
			item.setPrev(prev);
			if(prev != null)
				prev.setNext(item);
			
			history.addItem(item);
			prev = item;
		}
		
		assertFalse(history.verify());
		VerificationResult result = veri.verify(history);		
		if(result.messageCount() == 0)
		{
			fail(result.toString());
		}
		System.out.println(result);
		assertFalse(result.getStatus() == VerificationStatus.PASS);
	
		System.out.println("Now testing correct");
		
		history = new CryptoHistory();
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
		result = veri.verify(history);
		System.out.println(result);
		assertEquals(VerificationStatus.PASS,result.getStatus());
		
	}
	

}
