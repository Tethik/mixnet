package org.juddholm.mixnet.verifyserver;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.KeyCollection;
import org.juddholm.mixnet.enums.EncryptionLayer;

public interface OutputHandler {
	public KeyCollection getPublicKeys();
	public KeyCollection getPrivateKeys();
	public void sendDummy(CryptoMessage message);
}
