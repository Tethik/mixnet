package org.juddholm.voteserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

import org.juddholm.crypto.CryptoMessage;
import org.juddholm.mixnet.interfaces.VoteHandler;

public interface VoteInserter extends Remote {
	public boolean addVote(CryptoMessage msg) throws RemoteException;
}
