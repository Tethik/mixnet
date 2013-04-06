package org.juddholm.voteclient;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.json.JSONException;
import org.juddholm.crypto.ConcatCryptoMessage;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.KeyCollection;
import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.mixserver.rmi.MixServerInterface;
import org.juddholm.voteserver.VoteInserter;

public class VoteClient {
	
	private KeyCollection keys = new KeyCollection();
	private VoteInserter insert;
	private VoteClientSettings settings;
	
	public VoteClient(String filename) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException, FileNotFoundException, JSONException
	{ 
		settings = new VoteClientSettings(filename);
		System.out.println("# Loaded settings file");
		
		insert = (VoteInserter) Naming.lookup(settings.getVoteServer());
		System.out.println("# Connected to ballot");
		getKeys();
		System.out.println("# Ready for your vote!");
	}
	
	private void getKeys() throws RemoteException, MalformedURLException, NotBoundException
	{
		System.out.println("# Fetching keys...");	
		
		int i = 1;
		for(String address : settings.getMixnodes())
		{			
			MixServerInterface node = (MixServerInterface) Naming.lookup(address);
			KeyCollection nodekeys = node.getPublicKeys();
			System.out.println("# Connected to mixnode " + i++ + ": " + address);			
			keys.union(nodekeys);
			System.out.println("# Added "+nodekeys.size()+" public keys");
		}
	}
	
	
	public CryptoMessage encryptVote(String vote)
	{
		System.out.println("Encrypting vote..");
		CryptoMessage msg = new CryptoMessage(vote);
		
		// Final
		keys.addEncryptionLayer(msg, EncryptionLayer.FINAL);
		
		// Repetitions
		CryptoMessage[] msgs = new CryptoMessage[settings.getRepetitions()];
		for(int i = 0; i < settings.getRepetitions(); i++)
		{			
			msgs[i] = msg.clone();
			System.out.println("# Repetition " + i);
			keys.addEncryptionLayer(msgs[i], EncryptionLayer.REPETITION);
			keys.addEncryptionLayer(msgs[i], EncryptionLayer.Mix2);
			keys.addEncryptionLayer(msgs[i], EncryptionLayer.Mix1);
		}
		
		// Outer
		ConcatCryptoMessage concmsg = new ConcatCryptoMessage(msgs);
		keys.addEncryptionLayer(concmsg, EncryptionLayer.OUTER);
		
		return concmsg;
	}
	
	public void sendVote(String vote) throws RemoteException
	{	
		System.out.println("<-- Sending vote..");
		if(insert.addVote(encryptVote(vote)))
			System.out.println("# Vote sent!");
		else
			System.out.println("# Vote failed!");
	}

	
	public static void main(String[] args)
	{
		if(args.length < 1)
		{
			System.out.println("Usage: voteclient [settings file] (vote)");
			return ;
		}
			
		VoteClient client = null;
		try {
			client = new VoteClient(args[0]);
		} catch (RemoteException | InterruptedException | MalformedURLException | NotBoundException e1) {
			System.out.println("Failed to connect to ballot and mixnodes.");
			e1.printStackTrace();
			return;
		} catch (FileNotFoundException | JSONException e1) {
			System.out.println("Failed to load settings file");
			e1.printStackTrace();
			return;
		}	
		
		try {
			System.out.print("Vote:");
			if(args.length == 2) {
				System.out.println(args[1]);
				client.sendVote(args[1]); 
			} else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String vote = reader.readLine();			
				client.sendVote(vote);
			}
		} catch (IOException e) {
			System.err.println("Voting failed due to exception.");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
}
