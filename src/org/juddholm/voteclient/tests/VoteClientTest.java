package org.juddholm.voteclient.tests;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.PrivateKey;

import org.json.JSONException;
import org.juddholm.crypto.ConcatCryptoMessage;
import org.juddholm.crypto.CryptoMessage;
import org.juddholm.mixnet.enums.EncryptionLayer;
import org.juddholm.mixnet.mixserver.rmi.MixServerRMI;
import org.juddholm.voteclient.VoteClient;
import org.juddholm.voteserver.VoteServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VoteClientTest {
	
	VoteServer server;
	MixServerRMI[] ms = new MixServerRMI[3];
	
	@Before
	public void setUp()
	{
		try {
			server = new VoteServer("tests/voteclient/VoteServer.json");
			for(int i = 1; i < 4; i++)
				ms[i-1] = new MixServerRMI("tests/voteclient/m"+i+".json");
		} catch (IOException | NotBoundException | JSONException e) {
			fail(e.toString());
		}		
	}

	@Test
	public void testSendVote() {
		String vote = "test";		
		VoteClient client = null;
		
		try {
			client = new VoteClient("tests/voteclient/VoteClient.json");
		} catch (RemoteException | MalformedURLException
				| FileNotFoundException | NotBoundException
				| InterruptedException | JSONException e) {			
			e.printStackTrace();
			fail(e.toString());
		}
		
		try {
			client.sendVote(vote);
		} catch (RemoteException e) {
			e.printStackTrace();
			fail(e.toString());
		}		
		
		
		assertEquals(1, server.getVotes().size());
		
		try {
			client.sendVote(vote);
		} catch (RemoteException e) {
			e.printStackTrace();
			fail(e.toString());
		}	
		
		assertEquals(2, server.getVotes().size());
		
	}
	
	@Test
	public void testEncryptVote()
	{
		String vote = "test";		
		VoteClient client = null;		
		
		try {
			client = new VoteClient("tests/voteclient/VoteClient.json");
		} catch (RemoteException | MalformedURLException
				| FileNotFoundException | NotBoundException
				| InterruptedException | JSONException e) {			
			e.printStackTrace();
			fail(e.toString());
		}
		
		ConcatCryptoMessage concmsg = (ConcatCryptoMessage) client.encryptVote(vote);
		assertEquals(3, concmsg.getLayers());
		for(int i = 0; i < 3; i++)
			try {
				concmsg.decrypt((PrivateKey) ms[i].getMixServer().getKeys().getPrivateKeys().getKeys(EncryptionLayer.OUTER).get(0));
			} catch (IllegalAccessException e) {				
				e.printStackTrace();
				fail("Decryption failed.");
			}
		
		CryptoMessage[] msgs = concmsg.split();
		
		for(CryptoMessage msg : msgs) {			
			for(int i = 0; i < 3; i++)
				try {
					msg.decrypt((PrivateKey) ms[i].getMixServer().getKeys().getPrivateKeys().getKeys(EncryptionLayer.REPETITION).get(0));
				} catch (IllegalAccessException e) {				
					e.printStackTrace();
					fail("Decryption failed.");
				}			
		}
		
		for(CryptoMessage msg : msgs) {
			for(int i = 0; i < 3; i++)
				try {
					msg.decrypt((PrivateKey) ms[i].getMixServer().getKeys().getPrivateKeys().getKeys(EncryptionLayer.FINAL).get(0));
				} catch (IllegalAccessException e) {				
					e.printStackTrace();
					fail("Decryption failed.");
				}
			assertEquals(vote, msg.getMessage());
		}
		
		
		
	}
	
	@After
	public void tearDown() throws RemoteException, MalformedURLException, NotBoundException
	{
		server.unbind();
		for(int i = 0; i < 3; i++)
			ms[i].unbind();
	}

}
