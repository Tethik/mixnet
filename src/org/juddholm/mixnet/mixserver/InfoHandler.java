package org.juddholm.mixnet.mixserver;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.crypto.KeyCollection;
import org.juddholm.mixnet.enums.EncryptionLayer;

/**
 * Interface for releasing information.
 * @author tethik
 */
public interface InfoHandler {

	public void releasePublicKeys(KeyCollection keys);
	public void releasePrivateKey(EncryptionLayer layer, PrivateKey key);
	public void releaseCollection(CryptoMessageCollection collection);
	
}
