package org.juddholm.mixnet.verifyserver.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.juddholm.crypto.CryptoMessageCollection;

public interface VerifyServerInterface extends Remote {
	public void putCryptoCollection(CryptoMessageCollection collection) throws RemoteException;
	public void addVerificationListener(VerificationListener listener) throws RemoteException;
}
