package org.juddholm.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteHandler extends Remote {
	
	public Remote getInputHandler() throws RemoteException;
	public Remote getInfoHandler() throws RemoteException;

}
