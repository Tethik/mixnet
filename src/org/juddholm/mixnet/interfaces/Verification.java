package org.juddholm.mixnet.interfaces;

import org.juddholm.crypto.CryptoHistory;
import org.juddholm.crypto.VerificationResult;

public interface Verification {		
	public VerificationResult verify(CryptoHistory history);
}
