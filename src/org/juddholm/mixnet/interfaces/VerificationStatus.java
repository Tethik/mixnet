package org.juddholm.mixnet.interfaces;

public enum VerificationStatus {
	PASS, // Verification passed
	FAIL, // Verification failed
	ERROR, // Error occurred while verifying
	WAIT // Verification is still processing
}
