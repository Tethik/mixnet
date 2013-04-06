package org.juddholm.mixnet.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MixNodeRegistry extends Remote {
	public void registerAsNode(String name) throws RemoteException;
	public List<MixNode> getMixNodes() throws RemoteException;
}
