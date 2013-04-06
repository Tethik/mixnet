package org.juddholm.mixnet.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface VerifierRegistry extends Remote {
	public void registerAsVerifier(String name) throws RemoteException;
	public List<Verifier> getVerifiers() throws RemoteException;
}
