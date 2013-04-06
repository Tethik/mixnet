package org.juddholm.mixnet.interfaces;

import org.juddholm.crypto.CryptoHistory;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.crypto.KeyPairCollection;
import org.juddholm.crypto.VerificationResult;
import org.juddholm.mixnet.enums.*;
import java.util.List;
import java.util.Map;

public interface Verification {		
	public VerificationResult verify(CryptoHistory history);
}
