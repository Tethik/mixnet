package org.juddholm.mixnet.mixserver.rmi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import org.json.JSONException;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.crypto.DummyVerification;
import org.juddholm.crypto.DummyVerificationResult;
import org.juddholm.crypto.KeyCollection;
import org.juddholm.crypto.VerificationResult;
import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.enums.VerificationStatus;
import org.juddholm.mixnet.enums.VerificationType;
import org.juddholm.mixnet.mixserver.InfoHandler;
import org.juddholm.mixnet.mixserver.MixServer;
import org.juddholm.mixnet.mixserver.MixServerSettings;
import org.juddholm.mixnet.mixserver.OutputHandler;
import org.juddholm.mixnet.verifyserver.rmi.VerificationListener;
import org.juddholm.mixnet.verifyserver.rmi.VerifyServerInterface;
import org.juddholm.rmi.RMIServer;

public class MixServerRMI extends RMIServer implements MixServerInterface, OutputHandler, InfoHandler, VerificationListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3737878049530894857L;

	private MixServer mix = null;
	private MixServerSettings settings;
	
	private List<CryptoMessage> dummies = new ArrayList<>();
	private KeyCollection publicKeys = null;
	private KeyCollection privateKeys = new KeyCollection();
	private CryptoMessageCollection collection = null;
	private HashMap<VerificationType, List<VerificationStatus>> verifications = new HashMap<>();
	
	public MixServerRMI(String filename) throws InvalidPropertiesFormatException, FileNotFoundException, IOException, JSONException, NotBoundException
	{
		super();
		settings = new MixServerSettings(filename);

		//output = new OutputHandlerRMI(server.getId(), serverRegistry, hostname_to_registry);			
		mix = new MixServer(this, this);	
		mix.setRepetitions(settings.getRepetitions());
	
		new Thread(mix).start();
		bind(settings.getName());
	
		for(String v : settings.getVerifyServers())
		{
			while(true)
			{
				try {
					VerifyServerInterface veri = (VerifyServerInterface) Naming.lookup(v);
					veri.addVerificationListener(this);
					break;
				} catch(Exception ex) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public MixServer getMixServer()
	{
		return mix;
	}
	
	@Override
	public boolean isLast() {
		return settings.isLast();
	}
	

	@Override
	public void putCryptoCollection(CryptoMessageCollection collection) throws RemoteException {
		try {
			System.out.println("--> Got collection from " + RemoteServer.getClientHost() + " with " + collection.size() + " votes and " + collection.layers() + " layers!");
		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mix.putCryptoCollection(collection);			
	}
	
	@Override
	public boolean putCryptoCollection(CryptoMessageCollection collection, EncryptionLayer layer) {		
		if(isLast())
		{
			System.out.println("# Result after " + layer + " encryption stage");
			int i = 0; 
			for(CryptoMessage msg : collection.getList())
				System.out.println("["+i+++"] "+msg.getMessage());		
		}				
	
		try {
			System.out.println("<-- Sending collection to " + settings.getNext());	
			MixServerInterface node = (MixServerInterface) Naming.lookup(settings.getNext());		
			
			for(String v : settings.getVerifyServers())
			{
				System.out.println("<-- Sending to verifier " + v);
				VerifyServerInterface veri = (VerifyServerInterface) Naming.lookup(v);
				veri.putCryptoCollection(collection);
				/*
				RemoteHandler veri = info.getRemote();
				((org.juddholm.mixnet.verifyserver.rmi.RMIInput) veri.getInputHandler()).putCryptoCollection(collection);
				*/
			}
			
			if(!(isLast() && layer == EncryptionLayer.FINAL))
			{		
				node.putCryptoCollection(collection);
			}
			
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			return false;
		}
		
		return true;	
	}

	@Override
	public synchronized List<VerificationStatus> getVerifications(VerificationType type) {
		while(verifications.get(type) == null || verifications.get(type).size() < settings.getVerifyServers().length)
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return verifications.get(type);
	}

	@Override
	public synchronized void getVerification(VerificationType type, VerificationResult result) {
		if(verifications.get(type) == null)
			verifications.put(type, new ArrayList<VerificationStatus>());
		
		verifications.get(type).add(result.getStatus());
		
		if(type == VerificationType.Dummies) {
			DummyVerificationResult dresult = (DummyVerificationResult) result;
			dummies.add(dresult.getBeforeFinalDummy());
		}
		
		notify();
	}
	
	@Override
	public KeyCollection getPublicKeys() throws RemoteException {
		try {
			System.out.println("--> PublicKey request from " + RemoteServer.getClientHost());
		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return publicKeys;
	}

	@Override
	public synchronized KeyCollection getPrivateKeys() throws RemoteException {
		return privateKeys;
	}	

	@Override
	public void releasePublicKeys(KeyCollection keys) {		
		publicKeys = keys;
	}

	@Override
	public synchronized void releasePrivateKey(EncryptionLayer layer, PrivateKey key) {		
		assert(key != null);
		privateKeys.addKey(layer, key);
	}

	@Override
	public CryptoMessageCollection getCollection() throws RemoteException {
		return collection;
	}

	@Override
	public void releaseCollection(CryptoMessageCollection collection) {
		this.collection = collection;		
	}
	
	public static void main(String[] args)
	{
		if(args.length < 1)
		{
			System.out.println("Usage: MixServerRMI [settings file]");
			return;
		}
		
		try {
			MixServerRMI rmiserv = new MixServerRMI(args[0]);
		} catch (IOException | JSONException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void getDummyMessage(CryptoMessage message) throws RemoteException {
		dummies.add(message);
		notify();
	}

	@Override
	public synchronized List<CryptoMessage> getDummies() {
		while(dummies.size() < settings.getVerifyServers().length)
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return dummies;
	}

	@Override
	public boolean isFirst() {
		return settings.isFirst();
	}
}
