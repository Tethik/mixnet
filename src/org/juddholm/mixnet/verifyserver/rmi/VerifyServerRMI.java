package org.juddholm.mixnet.verifyserver.rmi;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.crypto.KeyCollection;
import org.juddholm.crypto.VerificationResult;
import org.juddholm.mixnet.enums.VerificationType;
import org.juddholm.mixnet.mixserver.rmi.MixServerInterface;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.mixnet.verifyserver.InfoHandler;
import org.juddholm.mixnet.verifyserver.OutputHandler;
import org.juddholm.mixnet.verifyserver.VerifyServer;
import org.juddholm.mixnet.verifyserver.VerifyServerSettings;
import org.juddholm.rmi.RMIServer;
import org.juddholm.voteserver.VoteInserter;
import org.juddholm.voteserver.VoteServer;

public class VerifyServerRMI extends RMIServer implements VerifyServerInterface, OutputHandler, InfoHandler {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4970979911064348264L;
	
	private HashMap<VerificationType, VerificationResult> verifications = new HashMap<>();
	private List<VerificationListener> listeners = new ArrayList<VerificationListener>();
	private VerifyServer server;
	private VerifyServerSettings settings;
	
	public VerifyServerRMI(String filename) throws RemoteException, FileNotFoundException, JSONException, MalformedURLException
	{
		super();
		settings = new VerifyServerSettings(filename);
		bind(settings.getName());
		server = new VerifyServer(this, this, settings.getRepetitions());
		new Thread(server).start();
	}
	
	@Override
	public void releaseVerification(VerificationType type,
			VerificationResult result) {
		verifications.put(type, result);		
		for(VerificationListener listener : listeners)
		{
			System.out.println("<-- Sent verification.");
			try {
				listener.getVerification(type,result);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public KeyCollection getPublicKeys() {
		KeyCollection publicKeys = new KeyCollection();
		int i = 1;
		for(String mix : settings.getMixnodes())
		{		
			KeyCollection keys;
			while(true) {				
				try {
					keys = ((MixServerInterface) Naming.lookup(mix)).getPublicKeys();
					break;
				} catch (RemoteException | MalformedURLException | NotBoundException e) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//throw new IllegalStateException("Failed to get public keys!", e);
				}
			}
			System.out.println("# Connected to mixnode #" + i++);
			System.out.println("# Adding "+keys.size()+" public keys");
			publicKeys.union(keys);
		}
		
		return publicKeys;
	}

	@Override
	public KeyCollection getPrivateKeys() {
		
		KeyCollection privateKeys = new KeyCollection();
		int i = 1;
		for(String mix : settings.getMixnodes())
		{	
			KeyCollection keys;
			boolean first = true;
			try {
				do {
					if(!first)
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					first = false;
					keys = ((MixServerInterface) Naming.lookup(mix)).getPrivateKeys();
				} while(keys.size() == 0);				
			} catch (RemoteException | MalformedURLException | NotBoundException e) {
				throw new IllegalStateException("Failed to get private keys!", e);
			}
			System.out.println("# Connected to mixnode #" + i++);
			System.out.println("# Adding "+keys.size()+" private keys");
			privateKeys.union(keys);
		}
		
		return privateKeys;
	}

	@Override
	public synchronized void putCryptoCollection(CryptoMessageCollection collection) {
		try {
			System.out.println("--> Got collection from " + RemoteServer.getClientHost());
		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}		

		server.putCryptoCollection(collection);
	}

	@Override
	public void addVerificationListener(VerificationListener listener)
			throws RemoteException {
		try {
			System.out.println("--> Listener added: " + RemoteServer.getClientHost());
		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listeners.add(listener);		
	}
	

	public static void main(String[] args)
	{
		if(args.length < 1) {
			System.out.println("USAGE: VerifyServerRMI [settings file]");
			return;
		}
		
		try {
			VerifyServerRMI s = new VerifyServerRMI(args[0]);
		} catch (RemoteException | FileNotFoundException
				| MalformedURLException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void sendDummy(CryptoMessage message) throws RemoteException, MalformedURLException, NotBoundException {		
		VoteInserter inserter = (VoteInserter) Naming.lookup(settings.getVoteServer());
		inserter.addVote(message);

	}
	
}
