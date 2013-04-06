package org.juddholm.mixnet.verifyserver.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.VerificationResult;
import org.juddholm.mixnet.enums.VerificationType;

public interface VerificationListener extends Remote, Serializable {
	public void getVerification(VerificationType type, VerificationResult result) throws RemoteException;
	public void getDummyMessage(CryptoMessage message) throws RemoteException;
}
