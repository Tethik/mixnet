package org.juddholm.mixnet.mixserver.rmi.tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.json.JSONException;
import org.juddholm.mixnet.mixserver.rmi.MixServerRMI;
import org.juddholm.voteclient.VoteClient;
import org.juddholm.voteserver.VoteServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MixServerRMITest {

	VoteServer server;
	MixServerRMI[] ms = new MixServerRMI[3];
	
	@Before
	public void setUp()
	{
		try {
			server = new VoteServer("tests/mixserver/VoteServer.json");
			for(int i = 1; i < 4; i++)
				ms[i-1] = new MixServerRMI("tests/mixserver/m"+i+".json");
		} catch (IOException | NotBoundException | JSONException e) {
			fail(e.toString());
		}		
	}
	
	@Test
	public void test() {
		String vote = "test";
		VoteClient client;
		try {
			client = new VoteClient("tests/mixserver/VoteClient.json");
			client.sendVote(vote);
		} catch (RemoteException | MalformedURLException
				| FileNotFoundException | NotBoundException
				| InterruptedException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		server.closeVote();
		try {
			server.sendVotes();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//ms[0].getMixServer().
		
		
	}
	
	
	@After
	public void tearDown() throws RemoteException, MalformedURLException, NotBoundException
	{
		server.unbind();
		for(int i = 0; i < 3; i++)
			ms[i].unbind();
	}

}
