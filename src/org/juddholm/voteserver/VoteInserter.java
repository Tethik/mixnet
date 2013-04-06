package org.juddholm.voteserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.juddholm.crypto.CryptoMessage;

public interface VoteInserter extends Remote {
	public boolean addVote(CryptoMessage msg) throws RemoteException;
}
