package org.juddholm.mixnet.mixserver.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.crypto.KeyCollection;

public interface MixServerInterface extends Remote {
	
	public KeyCollection getPublicKeys() throws RemoteException;
	
	public KeyCollection getPrivateKeys() throws RemoteException;
	
	public CryptoMessageCollection getCollection() throws RemoteException;
	
	public void putCryptoCollection(CryptoMessageCollection collection) throws RemoteException;
}
