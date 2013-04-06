package org.juddholm.crypto.tests;

import static org.junit.Assert.*;

import org.juddholm.crypto.ConcatCryptoMessage;
import org.juddholm.crypto.CryptoMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.security.KeyPair;

public class ConcatCryptoMessageTest {
	
	private static int CHAIN_SIZE = 3;
	private static int MESSAGE_COUNT = 10;
	
	private List<KeyPair> pairs;

	@Before
	public void setUp() throws Exception {
		this.pairs = TestHelper.generateKeypairs(CHAIN_SIZE);
	}

	@Test
	public void testConcatCryptoMessage() {
		String str = TestHelper.randomString();
		CryptoMessage msg = new CryptoMessage(str);		
		
		for(int i = 1; i <= MESSAGE_COUNT; i++)
		{
			System.out.println("Testing for "+i+" messages.");
			CryptoMessage[] msgs = new CryptoMessage[i];
			for(int x = 0; x < i; x++) {
				msgs[x] = msg.clone();
				msgs[x].encrypt(pairs.get(0).getPublic());
			}
		
			ConcatCryptoMessage concmsg = new ConcatCryptoMessage(msgs);
			assertEquals(i, concmsg.getNumberOfMessages());
			//assertEquals((i)*msg.getData().length, concmsg.getData().length);
			
			concmsg.encrypt(pairs.get(1).getPublic());			
			assertEquals(1, concmsg.getLayers());
			concmsg.encrypt(pairs.get(2).getPublic());			
			assertEquals(2, concmsg.getLayers());
			boolean caught = false;
			try {
				CryptoMessage[] msgs_split = concmsg.split();
			} catch(IllegalStateException ex) {
				caught = true;
			}
			assertTrue(caught);
			
			try {
				concmsg.decrypt(pairs.get(2).getPrivate());
				concmsg.decrypt(pairs.get(1).getPrivate());
			} catch (IllegalAccessException e) {
				fail("Decryption failed");
			}			
			
			CryptoMessage[] split = concmsg.split();
			assertArrayEquals(msgs, split);
			assertTrue(split.length == i);
			
			for(CryptoMessage m : split)
			{
				System.out.println(m.getMessage() + " "+  m.getLayers());
				try {
					m.decrypt(pairs.get(0).getPrivate());
				} catch (IllegalAccessException e) {
					fail("Decryption failed:" + e.toString());
				}
				System.out.println(m.getMessage());
				assertEquals(str, m.getMessage());
			}
		}
	}

	@Test
	public void testSplit() {		
		CryptoMessage[] msgs = new CryptoMessage[3];
		for(int x = 0; x < 3; x++)
			msgs[x] = new CryptoMessage(TestHelper.randomString());
		
		ConcatCryptoMessage concmsg = new ConcatCryptoMessage(msgs);
		assertArrayEquals(msgs,concmsg.split());	
		
		concmsg.encrypt(pairs.get(0).getPublic());
		try {
			concmsg.decrypt(pairs.get(0).getPrivate());
		} catch (IllegalAccessException e) {
			fail("Decryption failed");
		}
		
		assertArrayEquals(msgs,concmsg.split());
		
	}
	
	@Test
	public void testEncryptDecrypt()
	{
		CryptoMessage msg = new CryptoMessage(TestHelper.randomString());
		CryptoMessage[] msgs = new CryptoMessage[3];
		for(int x = 0; x < 3; x++)
			msgs[x] = msg.clone();
		
		ConcatCryptoMessage concmsg = new ConcatCryptoMessage(msgs);
		concmsg.encrypt(pairs.get(0).getPublic());
		try {
			concmsg.decrypt(pairs.get(0).getPrivate());
		} catch (IllegalAccessException e) {
			fail("Decryption failed");
		}
	
	}

}
