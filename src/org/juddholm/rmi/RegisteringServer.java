package org.juddholm.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.juddholm.mixnet.enums.ServerType;


public class RegisteringServer extends RMIServer {

	private String hostname_to_registry;
	protected ServerType type;
	protected int myId;
	protected ServerRegistry serverRegistry;
	
	
	public RegisteringServer(ServerRegistry serverRegistry, String name, ServerType type) throws RemoteException, MalformedURLException, NotBoundException {
		super(name);
		this.serverRegistry = serverRegistry;
		this.type = type;
		this.hostname_to_registry = hostname_to_registry;
		bind();
		register();
	}
	
	protected void register() throws MalformedURLException, RemoteException, NotBoundException
	{	
		myId = serverRegistry.register(type, name);
		if(myId == -1)
			throw new IllegalStateException("Could not register!");			
	}
	
	public int getId()
	{
		return myId;
	}
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 4350677332810398097L;

}
