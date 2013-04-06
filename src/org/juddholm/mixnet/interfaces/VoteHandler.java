package org.juddholm.mixnet.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.juddholm.crypto.CryptoMessageCollection;

public interface VoteHandler extends Remote {
	public void AddVoteCollection(CryptoMessageCollection coll) throws RemoteException;
}
