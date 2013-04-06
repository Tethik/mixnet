package org.juddholm.mixnet.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.juddholm.mixnet.enums.VotingStage;

public interface VotingStageHandler extends Remote {
	public VotingStage getCurrentStage() throws RemoteException;	
}
