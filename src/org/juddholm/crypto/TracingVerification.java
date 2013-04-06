package org.juddholm.crypto;

import org.juddholm.mixnet.interfaces.Verification;

public class TracingVerification implements Verification {

	private int repetitions;
	private VerificationResult result = new VerificationResult();
	public TracingVerification(int repetitions)
	{
		this.repetitions = repetitions;
	}
	
	private void addBadPositionMessage(int pos, int copies)
	{
		StringBuilder builder = new StringBuilder("Bad message at positions [ ");
		for(int x = 0; x < copies; x++)
			builder.append(pos - x + " ");
		builder.append("] ");
		result.addErrorMessage(builder.toString());		
	}
	
	@Override
	public VerificationResult verify(CryptoHistory history) {	
		CryptoMessageCollection collection = history.get(history.size() - 1).getColl();
		collection.sort();
		
		int copies = 0;
		CryptoMessage prev = collection.get(0);
		for(int i = 0; i < collection.size(); i++)
		{
			CryptoMessage curr = collection.get(i);
			
			if(prev.equals(curr)) {
				copies++;
			} else {	
				if(copies != repetitions)
				{
					addBadPositionMessage(i-1, copies);	
				}
				copies = 1;
			}		
			
			prev = curr;				
		}
		
		if(copies != repetitions)
		{
			addBadPositionMessage(collection.size()-1, copies);	
		}		
		return result;
	}

}
