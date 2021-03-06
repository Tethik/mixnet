package org.juddholm.crypto;

import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.interfaces.Verification;

public class ExplicitVerification implements Verification {
	
	
	private VerificationResult result = new VerificationResult();
	
	private EncryptionLayer from;
	private EncryptionLayer to;
	
	public ExplicitVerification(EncryptionLayer from, EncryptionLayer to)
	{
		this.from = from;
		this.to = to;
	}

	@Override
	public VerificationResult verify(CryptoHistory history) {	
		int i = 0;
		result = new VerificationResult();
		for(CryptoHistoryItem item : history.getItems())
		{
			if(item.getLayer().ordinal() < from.ordinal())
				continue;
			
			if(item.getLayer().ordinal() > to.ordinal())
				break;
			
			if(!item.verifyDecryption())
				result.addErrorMessage(item.getLayer() + " #" + i + " Decryption of previous collection did not match this one.");
			
			/*
			if(!item.verifySignature()) 
				result.addErrorMessage(item.getLayer() + " #" + i + " Signature of previous collection did not match this one.");
			*/
			
			i++;
		}
		
		return result;
		
		
	}

}
