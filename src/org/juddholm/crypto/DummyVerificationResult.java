package org.juddholm.crypto;

public class DummyVerificationResult extends VerificationResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1303837697127617776L;
	
	private CryptoMessage fullyEncryptedDummy;
	private CryptoMessage beforeFinalDummy;
	
	public CryptoMessage getFullyEncryptedDummy() {
		return fullyEncryptedDummy;
	}
	public void setFullyEncryptedDummy(CryptoMessage fullyEncryptedDummy) {
		this.fullyEncryptedDummy = fullyEncryptedDummy;
	}
	public CryptoMessage getBeforeFinalDummy() {
		return beforeFinalDummy;
	}
	public void setBeforeFinalDummy(CryptoMessage beforeFinalDummy) {
		this.beforeFinalDummy = beforeFinalDummy;
	}
	
}
