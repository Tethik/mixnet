package org.juddholm.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.juddholm.mixnet.enums.ServerType;

public interface ServerRegistry extends Remote {
	public int register(ServerType type, String name) throws RemoteException;
	public List<ServerInfo> getList(ServerType type) throws RemoteException;
}
