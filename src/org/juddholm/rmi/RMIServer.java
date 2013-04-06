package org.juddholm.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public abstract class RMIServer extends UnicastRemoteObject {
	
	protected RMIServer() throws RemoteException {
		super();
	}

	private String hostname;
	
	public void bind(String hostname) throws RemoteException, MalformedURLException
	{
		this.hostname = hostname;
		if (System.getSecurityManager() == null) {			
            System.setSecurityManager(new RMISecurityManager());            
            System.out.println("# Security manager installed.");
        } else {
            System.out.println("# Security manager already exists.");
        }
 
        try { 
        	//special exception handler for registry creation
            LocateRegistry.createRegistry(1099); 
            System.out.println("# java RMI registry created.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.out.println("# java RMI registry already exists.");
        }
        
		Naming.rebind("//localhost/"+hostname, this);	
		System.out.println("# Server bound to registry as //localhost/"+hostname);
	}
	
	public void unbind() throws RemoteException, MalformedURLException, NotBoundException
	{
		Naming.unbind("//localhost/"+hostname);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2665304033429986090L;
	
	
}
