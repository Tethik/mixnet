package org.juddholm.mixnet.verifyserver;

import java.rmi.RemoteException;

import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.VerificationResult;
import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.enums.VerificationStatus;
import org.juddholm.mixnet.enums.VerificationType;

public interface InfoHandler {
	public void releaseVerification(VerificationType type, VerificationResult result);
}
