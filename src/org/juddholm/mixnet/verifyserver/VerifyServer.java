package org.juddholm.mixnet.verifyserver;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.juddholm.crypto.CryptoHistory;
import org.juddholm.crypto.CryptoHistoryItem;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.crypto.DummyVerification;
import org.juddholm.crypto.ExplicitVerification;
import org.juddholm.crypto.KeyCollection;
import org.juddholm.crypto.KeyPairCollection;
import org.juddholm.crypto.TracingVerification;
import org.juddholm.crypto.VerificationResult;
import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.enums.VerificationType;
import org.juddholm.mixnet.interfaces.Verification;

public class VerifyServer implements Runnable {	
	//private Map<EncryptionLayer, VerificationResult> results = new HashMap<EncryptionLayer, VerificationResult>();
	
	private KeyPairCollection keys = new KeyPairCollection();
	private EncryptionLayer currentEncryptionStage = EncryptionLayer.OUTER;
	
	private CryptoHistory history = new CryptoHistory();
	//private Map<EncryptionLayer, List<CryptoMessageCollection> > decryptionResults = new HashMap<EncryptionLayer, List<CryptoMessageCollection>>(); 
	private Map<VerificationType, Verification> verifications = new HashMap<VerificationType, Verification>();
	private InfoHandler info;
	private OutputHandler output;
	private int repetitions;
	
	public int getRepetitions()
	{
		return repetitions;
	}
	
	public void setRepetitions(int repetitions)
	{
		this.repetitions = repetitions;
	}
	
	public VerifyServer(InfoHandler info, OutputHandler output, int repetitions) {
		this.info = info;
		this.output = output;
		this.repetitions = repetitions;	
		keys.getPublicKeys().union(output.getPublicKeys());
		
		ExplicitVerification everi = new ExplicitVerification(EncryptionLayer.Mix1, EncryptionLayer.Mix1);
		DummyVerification dveri = new DummyVerification(keys.getPublicKeys(), repetitions);
		TracingVerification tveri = new TracingVerification(repetitions);
		verifications.put(VerificationType.Explicit, everi);
		verifications.put(VerificationType.Dummies, dveri);
		verifications.put(VerificationType.Tracing, tveri);
		CryptoMessage msg = dveri.generateDummy(repetitions);	
		output.sendDummy(msg);

	}
	
	private int getExpectedNumberOfEncryptions(EncryptionLayer layer)
	{		
		return keys.getPublicKeys().getKeys(layer).size();
	}
	
	
	
	public synchronized EncryptionLayer getCurrentEncryptionStage() {
		return currentEncryptionStage;
	}

	public synchronized void setCurrentEncryptionStage(EncryptionLayer currentEncryptionStage) {
		this.currentEncryptionStage = currentEncryptionStage;
	}

	public synchronized void waitFor(EncryptionLayer layer) {
		//numberAdds.get(layer) == null || !numberAdds.get(layer).equals(getExpectedNumberOfEncryptions(layer)))
		while(currentEncryptionStage != layer)
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void verify(VerificationType type) {
		// Fetch private keys
		KeyCollection privateKeys = keys.getPrivateKeys();
		privateKeys.union(output.getPrivateKeys());
		
		int expected_index = 0;
		EncryptionLayer layer = EncryptionLayer.OUTER;
		for(CryptoHistoryItem item : history.getItems())
		{			
			if(item.getLayer() != layer) {
				layer = item.getLayer();
				expected_index = 0;
			}
			System.out.println(layer + ","+expected_index+": " +  item.getVerifyKey().hashCode());
			
			List<Key> layerKeys = privateKeys.getKeys(item.getLayer());
			if(layerKeys == null || layerKeys.size() <= expected_index)
				continue;
			PrivateKey key = (PrivateKey) layerKeys.get(expected_index);
			System.out.println(layer + ","+expected_index+": Adding key : " + key.hashCode() + " to item with key " + item.getVerifyKey().hashCode());
			item.setDecryptionKey(key);			
			expected_index++;
		}
		
		
		VerificationResult result = new VerificationResult();
		Verification v = verifications.get(type);
		
		if(v != null)
		{
			result = v.verify(history);	
		}		
		
		info.releaseVerification(type, result);		
		
		System.out.println(result.toString());
	}
	
	

	
	private static final long serialVersionUID = -5976055436215156206L;

	private int expected_index = 0; // Offset -1 to make up for initial collection
	private boolean initialCryptoReceived = false;
	public synchronized void putCryptoCollection(CryptoMessageCollection collection) {
		PublicKey key = (PublicKey) keys.getPublicKeys().getKeys(currentEncryptionStage).get(
				expected_index % keys.getPublicKeys().getKeys(currentEncryptionStage).size());
		/*
		if(!collection.checkSignatures(key)) {			
			System.out.println("Incorrect signature! Ignoring.");
			//return; // TODO: Report this?
		}		
	
		System.out.println("Got signed collection");
		*/
		
		if(expected_index == 0 && initialCryptoReceived) {
			currentEncryptionStage = currentEncryptionStage.next();
			System.out.println("Encryptionlayer is now: " + currentEncryptionStage);
		}
		
		CryptoHistoryItem item = new CryptoHistoryItem(collection);
		item.setLayer(currentEncryptionStage);
		item.setVerifyKey(key);
		history.addItem(item);	
		expected_index++;
		if(expected_index >= getExpectedNumberOfEncryptions(currentEncryptionStage))
		{
			expected_index = 0;			
			
		}
		initialCryptoReceived = true;
		
		notify();
	}

	@Override
	public void run() {
		System.out.println("# Waiting for results from Mix1 and Mix2");
		waitFor(EncryptionLayer.REPETITION);
		System.out.println("# Now running explicit verification");
		verify(VerificationType.Explicit);
		System.out.println("# Waiting for results from Final");
		waitFor(EncryptionLayer.FINAL);
		System.out.println("# Now running tracing verification");
		verify(VerificationType.Tracing);
		System.out.println("# Now tracing dummies");
		verify(VerificationType.Dummies);
		
	}

	

}
