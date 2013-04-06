package org.juddholm.crypto.tests;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.juddholm.crypto.CryptoMessage;
import org.junit.Before;
import org.junit.Test;

public class CryptoMessageTest {

	private static int CHAIN_SIZE = 5;
	private String plaintextData; 
	private CryptoMessage msg;
	private List<KeyPair> pairs;
	
	@Before
	public void setUp() throws Exception {
		plaintextData = TestHelper.randomString();
		System.out.println(plaintextData);
		msg = new CryptoMessage(plaintextData);
		pairs = new ArrayList<>();
		for(int i = 0; i < CHAIN_SIZE; i++)
		{
			pairs.add(KeyPairGenerator.getInstance("RSA").genKeyPair());
		}
	}

	@Test
	public void testGetData() {
		assertArrayEquals(msg.getData(), plaintextData.getBytes());
	}

	@Test
	public void testSetData() {
		plaintextData = TestHelper.randomString();
		try {
			msg.setData(plaintextData.getBytes());
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		}
		testGetData();
	}

	@Test
	public void testGetMessage() {
		assertEquals(msg.getMessage(), plaintextData);
	}

	@Test
	public void testSetMessage() {
		plaintextData = TestHelper.randomString();
		try {
			msg.setMessage(plaintextData);
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		}
		testGetData();
	}

	@Test
	public void testEncrypt() {		
		msg.encrypt(pairs.get(0).getPublic());
		assertFalse(plaintextData.equals(msg.getMessage()));
		assertEquals(msg.getLayers(), 1);
	}
	

	@Test
	public void testDecrypt() {
		testEncrypt();
		try {
			msg.decrypt(pairs.get(0).getPrivate());
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		}
		assertEquals(plaintextData, msg.getMessage());
	}
	
	
	@Test
	public void testChainEncryptDecrypt()
	{
		// Encrypt
		for(int i = 0; i < CHAIN_SIZE; i++) {
			msg.encrypt(pairs.get(i).getPublic());
			assertTrue(msg.getLayers() == i+1);
		}
		
		assertFalse(plaintextData.equals(msg.getMessage()));
		CryptoMessage singleCryptoMsg = new CryptoMessage(plaintextData);
		singleCryptoMsg.encrypt(pairs.get(0).getPublic());
		assertFalse(singleCryptoMsg.getMessage().equals(msg.getMessage()));
		
		// Decrypt
		for(int i = CHAIN_SIZE - 1; i > -1; i--)
			try {
				msg.decrypt(pairs.get(i).getPrivate());
				assertTrue(msg.getLayers() == i);
			} catch (IllegalAccessException e) {
				fail(e.getMessage());
			}
		
		assertEquals(msg.getMessage(), plaintextData);
	}
	
	@Test
	public void testSignVerify()
	{
		try {
			msg.setMessage(plaintextData);
			assertFalse(msg.verifySignature(pairs.get(0).getPublic()));
		} catch (IllegalAccessException e) {
			fail(e.getMessage());
		}
		
		for(int i = 0; i < CHAIN_SIZE; i++)
		{		
			assertFalse(msg.verifySignature(pairs.get(i).getPublic()));
			
			msg.sign(pairs.get(i).getPrivate());
			// Check that signature verifies
			assertTrue(msg.verifySignature(pairs.get(i).getPublic()));
			// Make sure message is untouched.
			assertTrue(msg.getMessage().equals(plaintextData));
		}
		
		// Check so that a mismatching private/public key don't verify.
		msg.sign(pairs.get(0).getPrivate());
		assertFalse(msg.verifySignature(pairs.get(1).getPublic()));
		
		
	}
	
	@Test
	public void testCloneAndEquals()
	{
		for(int i = 0; i < CHAIN_SIZE; i++) {
			msg.encrypt(pairs.get(i).getPublic());
		}
		msg.sign(pairs.get(0).getPrivate());
		
		CryptoMessage clone = msg.clone();
		
		assertArrayEquals(msg.getData(),clone.getData());
		assertTrue(clone.getLayers() == msg.getLayers());
		assertArrayEquals(msg.getSignature(), clone.getSignature());
		assertTrue(clone.equals(msg));
		
		int layers = msg.getLayers();		
		byte[] data = msg.getData().clone();
		byte[] signature = msg.getSignature().clone();
		
		for(int i = CHAIN_SIZE - 1; i > -1; i--) {
			PrivateKey decKey = pairs.get(i).getPrivate();
			try {
				msg.decrypt(decKey);
			} catch (IllegalAccessException e) {
				fail("Decrypt failed in clone test");
			}			
			msg.sign(decKey);
		}
		
		assertTrue(clone.getLayers() == layers);
		assertTrue(clone.getLayers() != msg.getLayers());
		
		assertArrayEquals(signature, clone.getSignature());
		assertArrayEquals(data, clone.getData());
		assertFalse(Arrays.equals(msg.getData(), clone.getData()));		
		assertFalse(clone.equals(msg));
		
		for(int i = CHAIN_SIZE - 1; i > -1; i--) {
			PrivateKey decKey = pairs.get(i).getPrivate();
			try {
				clone.decrypt(decKey);
			} catch (IllegalAccessException e) {
				fail("Decrypt failed in clone test");
			}			
			clone.sign(decKey);
		}
		
		/*
		System.out.println(clone.getData().length + " " + msg.getData().length);
		for(int i = 0; i < clone.getData().length; i++)
			System.out.println(clone.getData()[i] + " : " + msg.getData()[i]);
		*/
		
		assertArrayEquals(clone.getData(), msg.getData());
		assertTrue(clone.getLayers() == msg.getLayers());
		assertArrayEquals(clone.getSignature(), msg.getSignature());
		assertTrue(clone.equals(msg));
		
	}

}
