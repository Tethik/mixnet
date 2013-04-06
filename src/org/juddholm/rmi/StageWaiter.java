package org.juddholm.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.juddholm.mixnet.enums.VotingStage;
import org.juddholm.mixnet.interfaces.VotingStageHandler;

public class StageWaiter {
	
	private VotingStageHandler stageHandler;
	
	public StageWaiter(String hostname) throws MalformedURLException, RemoteException, NotBoundException
	{
		stageHandler = (VotingStageHandler) Naming.lookup(hostname);
	}
	
	public void waitForStage(VotingStage stage) throws RemoteException
	{
		System.out.println("Checking if stage is "+stage+"...");
		while(stageHandler.getCurrentStage() != stage)
		{
			System.out.println("Stage is not "+stage+". Waiting...");
			try
			{
				Thread.sleep(10000);
			}
			catch(InterruptedException ex) {				
			}			
		}
		
	}

}
