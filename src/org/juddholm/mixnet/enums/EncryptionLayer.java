package org.juddholm.mixnet.enums;

public enum EncryptionLayer {
	OUTER,
	Mix1,
	Mix2,
	REPETITION,
	FINAL;
	
	public EncryptionLayer next()
	{
		for(EncryptionLayer stage : EncryptionLayer.values())
			if(stage.ordinal() > this.ordinal())
				return stage;
		
		return EncryptionLayer.values()[EncryptionLayer.values().length-1];
	}
}
