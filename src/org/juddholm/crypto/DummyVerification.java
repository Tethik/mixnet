package org.juddholm.crypto;

import java.util.List;
import java.util.Random;

import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.interfaces.Verification;

public class DummyVerification implements Verification {
	
	private CryptoMessage beforeFinal;
	private CryptoMessage fullyEncrypted;
	private KeyCollection publicKeys;
	private int repetitions;
	
	public DummyVerification(KeyCollection publicKeys, int repetitions)
	{
		this.repetitions = repetitions;
		this.publicKeys = publicKeys;		
	}

	public static String randomString()
	{
		StringBuilder builder = new StringBuilder();
		
		Random random = new Random();
		int size = 15 + random.nextInt(10);
		for(int i = 0; i < size; i++)
		{
			builder.append((char) (48 +  random.nextInt(74)));
		}
		
		return builder.toString();		
	}
	
	public CryptoMessage generateDummy(int repetitions) {
		CryptoMessage msg = new CryptoMessage(randomString());
		
		publicKeys.addEncryptionLayer(msg, EncryptionLayer.FINAL);
		beforeFinal = msg.clone();
		
		// Repetitions
		CryptoMessage[] msgs = new CryptoMessage[repetitions];
		for(int i = 0; i < repetitions; i++)
		{			
			msgs[i] = msg.clone();
			System.out.println("# Repetition " + i);
			publicKeys.addEncryptionLayer(msgs[i], EncryptionLayer.REPETITION);
			publicKeys.addEncryptionLayer(msgs[i], EncryptionLayer.Mix2);
			publicKeys.addEncryptionLayer(msgs[i], EncryptionLayer.Mix1);
		}
		
		// Outer
		fullyEncrypted = new ConcatCryptoMessage(msgs);
		publicKeys.addEncryptionLayer(fullyEncrypted, EncryptionLayer.OUTER);	
		
		
		return fullyEncrypted;
	}
	
	public CryptoMessage getFullyEncryptedDummy()
	{
		return fullyEncrypted;
	}
	
	public CryptoMessage getBeforeFinalDummy()
	{
		return beforeFinal;
	}

	@Override
	public VerificationResult verify(CryptoHistory history) {
		DummyVerificationResult result = new DummyVerificationResult();		
		CryptoMessageCollection collection = history.getItems().get(history.getItems().size() - 1).getColl();
		
		int found = 0;
		for(CryptoMessage msg : collection.getList())
		{
			if(msg.equals(beforeFinal))
			{
				found++;				
			}
		}
		
		if(found != repetitions)
		{
			result.addErrorMessage("Dummy message is missing or altered. Expected " + repetitions + " but found " + found);
		}		
		
		result.setBeforeFinalDummy(beforeFinal);
		result.setFullyEncryptedDummy(fullyEncrypted);
		
		return result;
	}

}
