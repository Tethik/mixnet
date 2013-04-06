package org.juddholm.voteserver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.json.JSONException;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.mixnet.mixserver.rmi.MixServerInterface;
import org.juddholm.mixnet.verifyserver.rmi.VerifyServerInterface;
import org.juddholm.rmi.RMIServer;

public class VoteServer extends RMIServer implements VoteInserter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1224333633138710221L;
	private CryptoMessageCollection collection = new CryptoMessageCollection();
	
	private VoteServerSettings settings;
	private VoteServer server;
	private boolean closed = false;
	
	public VoteServer(String filename) throws FileNotFoundException, JSONException, RemoteException, MalformedURLException
	{
		super();	
		settings = new VoteServerSettings(filename);	
		bind(settings.getName());
		
	}

	@Override
	public synchronized boolean addVote(CryptoMessage vote) throws RemoteException {
		if(closed)
			return false;
		
		collection.add(vote);
		System.out.println("--> Received vote. Total votes: " + collection.size());		
		
		return true;
	}
	
	public synchronized void closeVote() {
		closed = true;
	}

	public void sendVotes() throws MalformedURLException, RemoteException, NotBoundException {	
		MixServerInterface mix = (MixServerInterface) Naming.lookup(settings.getFirstMixnode());
		mix.putCryptoCollection(collection);
		for(String v : settings.getVerifyServers())
		{
			VerifyServerInterface veri = (VerifyServerInterface) Naming.lookup(v);
			veri.putCryptoCollection(collection);
		}
	}
	
	public CryptoMessageCollection getVotes()
	{
		return collection;
	}
	
	public static void main(String[] args) {
		if(args.length < 1)
		{
			System.out.println("USAGE: VoteServer [settings file]");
			return;
		}
		
		VoteServer server = null;
		try {
			server = new VoteServer(args[0]);
		} catch (FileNotFoundException | RemoteException
				| MalformedURLException | JSONException e) {
			System.out.println("Could not start server");
			e.printStackTrace();
			return;
		}
		
		System.out.println("PRESS ENTER to close vote");
		System.out.println();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("# Closing vote.");
		server.closeVote();
		System.out.println("# Sending votes to mixnet...");
		try {
			server.sendVotes();			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {		
			System.out.println("# Failed to send votes to mixnet!");
			e.printStackTrace();
			return;			
		}
		System.out.println("# Votes sent to mixnet!");
	}

}
