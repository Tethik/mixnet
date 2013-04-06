package org.juddholm.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.juddholm.mixnet.enums.EncryptionLayer;

public class CryptoHistoryItem {

	private CryptoHistoryItem prev = null;	
	private CryptoHistoryItem next = null;
	
	private EncryptionLayer layer;
	
	/**
	 * The key that will decrypt the collection. It will also sign the next items collection.
	 */
	private PrivateKey decryptionKey = null;
	
	/**
	 * The key used to sign the decryption of this collection.
	 */
	private PublicKey verifyKey = null;
	
	
	/**
	 * The current collection of CryptoMessages. It should have been signed by the given verifyKey and will be decrypted by the decryption key.
	 */
	private CryptoMessageCollection coll;

	
	public CryptoHistoryItem(CryptoMessageCollection coll)
	{
		this.coll = coll;
	}
	
	/**
	 * @return the layer
	 */
	public EncryptionLayer getLayer() {
		return layer;
	}

	/**
	 * @param layer the layer to set
	 */
	public void setLayer(EncryptionLayer layer) {
		this.layer = layer;
	}

	/**
	 * @return the decryptionKey
	 */
	public PrivateKey getDecryptionKey() {
		return decryptionKey;
	}

	/**
	 * @param decryptionKey the decryptionKey to set
	 */
	public void setDecryptionKey(PrivateKey decryptionKey) {
		this.decryptionKey = decryptionKey;
	}

	/**
	 * @return the verifyKey
	 */
	public PublicKey getVerifyKey() {
		return verifyKey;
	}

	/**
	 * @param verifyKey the verifyKey to set
	 */
	public void setVerifyKey(PublicKey verifyKey) {
		this.verifyKey = verifyKey;
	}

	/**
	 * @return the prev
	 */
	public CryptoHistoryItem getPrev() {
		return prev;
	}

	/**
	 * @param prev the prev to set
	 */
	public void setPrev(CryptoHistoryItem prev) {
		this.prev = prev;
	}

	/**
	 * @return the next
	 */
	public CryptoHistoryItem getNext() {
		return next;
	}

	/**
	 * @param next the next to set
	 */
	public void setNext(CryptoHistoryItem next) {
		this.next = next;
	}
	
	@Override
	public String toString()
	{
		return "";
	}

	/**
	 * @return the coll
	 */
	public CryptoMessageCollection getColl() {
		return coll;
	}

	/**
	 * @param coll the coll to set
	 */
	public void setColl(CryptoMessageCollection coll) {
		this.coll = coll;
	}
	
	/**
	 * Verifies the signature based on the last decryption in the chain
	 * @return
	 */
	public boolean verifySignature()
	{
		if(prev == null)
			return true; // This was the first item, thus no signature should exist.		
		
		return coll.checkSignatures(prev.getVerifyKey());
	}
	
	public boolean verifyDecryption()
	{
		if(next == null)
			return true; // This was the last item, no more decryptions registered in this chain.
		
		CryptoMessageCollection clone = coll.clone();
	
		if(decryptionKey == null)
		{
			throw new IllegalArgumentException("DecryptionKey is null! " + getLayer() + " " + verifyKey.hashCode());
		}
		
		clone.decrypt(decryptionKey);
		//clone.sign(decryptionKey);

		return clone.equals(next.coll); 
	}
	
	
}
