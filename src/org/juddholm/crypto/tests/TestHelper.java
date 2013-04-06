package org.juddholm.crypto.tests;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TestHelper {

	public static String randomString()
	{
		StringBuilder builder = new StringBuilder();
		
		Random random = new Random();
		
		for(int i = 0; i < 20; i++)
		{
			builder.append((char) (48 +  random.nextInt(74)));
		}
		
		return builder.toString();		
	}
	
	public static List<KeyPair> generateKeypairs(int CHAIN_SIZE) throws NoSuchAlgorithmException
	{
		List<KeyPair> pairs = new ArrayList<>();
		for(int i = 0; i < CHAIN_SIZE; i++)
		{
			pairs.add(KeyPairGenerator.getInstance("RSA").genKeyPair());
		}
		return pairs;
	}
}
