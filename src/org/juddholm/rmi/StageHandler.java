package org.juddholm.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;

import org.juddholm.mixnet.enums.VotingStage;
import org.juddholm.mixnet.interfaces.VotingStageHandler;

public class StageHandler extends RMIServer implements VotingStageHandler {

	protected StageHandler(String name) throws RemoteException {
		super(name);
	}

	private VotingStage currentStage = VotingStage.Registration;
	
	public void setStage(VotingStage stage)
	{
		this.currentStage = stage;
		System.out.println("VotingStage changed to " + stage.toString());
			
	}
	
	@Override
	public VotingStage getCurrentStage() throws RemoteException {
		return currentStage;
	}
	
	public static void main(String[] args)
	{
		
	}

}
