package org.juddholm.mixnet.mixserver;

import java.util.List;

import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.enums.VerificationStatus;
import org.juddholm.mixnet.enums.VerificationType;

public interface OutputHandler {

	public boolean putCryptoCollection(CryptoMessageCollection coll, EncryptionLayer layer);
	public List<VerificationStatus> getVerifications(VerificationType type);
	public boolean isLast();
	public boolean isFirst();
	public List<CryptoMessage> getDummies();
}

