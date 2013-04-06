package org.juddholm.rmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.juddholm.crypto.CryptoMessage;
import org.juddholm.crypto.CryptoMessageCollection;
import org.juddholm.mixnet.enums.ServerType;
import org.juddholm.mixnet.enums.VotingStage;
import org.juddholm.mixnet.interfaces.VoteHandler;
import org.juddholm.mixnet.mixserver.rmi.RMIInput;
import org.juddholm.voteserver.VoteInserter;

public class RegistryServer extends RMIServer implements ServerRegistry, VoteInserter, VoteHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4608941052518452055L;
	
	private CryptoMessageCollection votes = new CryptoMessageCollection();
	private Map<ServerType, List<ServerInfo> > serverRegistry = new HashMap<ServerType, List<ServerInfo>>();
	private Map<String, List<ServerType> > hostnameMap = new HashMap<String, List<ServerType>>();
	private StageHandler stageHandler = new StageHandler("stagehandler");

	protected RegistryServer() throws RemoteException, MalformedURLException {
		super("registry");
		stageHandler.bind();
		this.bind();
		for(ServerType type : new ServerType[]{ServerType.Mix, ServerType.Register, ServerType.Verify})
			serverRegistry.put(type, new ArrayList<ServerInfo>());
	}
	
	public void sendVotesToFirstNode() throws RemoteException
	{
		ServerInfo info = serverRegistry.get(ServerType.Mix).get(0);
		RemoteHandler node = info.getRemote();
		RMIInput input = (RMIInput) node.getInputHandler();
		System.out.println("Sending votes to first mixnode: " + info.getName() + " " + info.getId());
		input.putCryptoCollection(votes);
	}
	
	public synchronized void waitForFinish() throws InterruptedException
	{
		wait();
	}
	
	

	@Override
	public boolean addVote(CryptoMessage msg) {
		try {
			if(stageHandler.getCurrentStage() != VotingStage.Voting)
				return false;
		} catch (RemoteException e1) {
			return false;
		}
		
		votes.add(msg);
		try {
			System.out.println("Added vote from " + RemoteServer.getClientHost() + ". Total votes: " + votes.size());
		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public synchronized void AddVoteCollection(CryptoMessageCollection coll)
			throws RemoteException {
		this.votes = coll;
		notifyAll();
	}
	
	@Override
	public synchronized int register(ServerType type, String name) throws RemoteException {		
		if(stageHandler.getCurrentStage() != VotingStage.Registration)
			return -1;
		
		String hostname;
		try {
			hostname = "//" + RemoteServer.getClientHost() + "/" + name;
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
			throw new RemoteException("Could not add to registry",e);
		}
		
		if(hostnameMap.containsKey(hostname) && hostnameMap.get(hostname).contains(type)) {
			System.out.println(hostname + " already exists in " + type.toString() + " registry!");
			return -1; // TODO: fixa till att vara samma id
		}
		
		RemoteHandler server;
		try {
			server = (RemoteHandler) Naming.lookup(hostname);
		} catch (MalformedURLException | NotBoundException e) {
			String msg = "Lookup to server failed";
			System.out.println(msg);
			throw new RemoteException(msg, e);			
		}
		
		if(!hostnameMap.containsKey(hostname))
			hostnameMap.put(hostname, new ArrayList<ServerType>());
		
		hostnameMap.get(hostname).add(type);
		int id = serverRegistry.get(type).size();
		serverRegistry.get(type).add(new ServerInfo(server, id, name));
		
		System.out.println("Added " + hostname + " to " + type.toString() + " registry.");
		return id;		
	}

	@Override
	public List<ServerInfo> getList(ServerType type)
			throws RemoteException {		
		return serverRegistry.get(type);
	}
	
	public static void main(String[] args) {		       
		try {
			RegistryServer server = new RegistryServer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Registration for mixnet open. Type vote to open for voting. Decrypt to start decrypting");
			while(true)
			{
				String input = reader.readLine().trim().toLowerCase();
				
				if(input.equals("vote"))
				{
					server.stageHandler.setStage(VotingStage.Voting);			
				} else if(input.equals("decrypt")) {
					server.sendVotesToFirstNode();
					server.stageHandler.setStage(VotingStage.Decrypting);
					break;
				}
			}
			server.waitForFinish();
			server.stageHandler.setStage(VotingStage.Result);
			server.printResult();			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void printResult() {
		int i = 0;
		for(CryptoMessage msg :	votes.getList())
			System.out.println("["+i+++"] "+msg.getMessage());			
	}




}
