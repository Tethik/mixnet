package org.juddholm.rmi;

import java.io.Serializable;
import java.rmi.Remote;

public class ServerInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2921669462499171336L;
	
	private RemoteHandler remote;
	private int id;
	private String name;
	
	public ServerInfo(RemoteHandler remote, int id, String name)
	{
		this.remote = remote;
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public RemoteHandler getRemote() {
		return remote;
	}
	
	public String getName() {
		return name;
	}
	
}
