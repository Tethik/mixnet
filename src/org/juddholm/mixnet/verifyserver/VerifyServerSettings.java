package org.juddholm.mixnet.verifyserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.json.JSONException;
import org.json.JSONMappedObject;
import org.json.JSONObj;
import org.json.JSONObject;
import org.json.JSONTokener;

public class VerifyServerSettings extends JSONMappedObject {
	
	@JSONObj
	protected String name;
	
	@JSONObj
	protected String[] mixnodes;
	
	@JSONObj
	protected int repetitions;
	
	@JSONObj
	protected String voteServer;
	

	public VerifyServerSettings(String filename) throws JSONException, FileNotFoundException {
		super(new JSONObject(new JSONTokener(new FileInputStream(filename))));		
	}

	public String getName() {
		return name;
	}


	public String[] getMixnodes() {
		return mixnodes;
	}	
	
	public int getRepetitions()
	{
		return repetitions;
	}
	
	public String getVoteServer()
	{
		return voteServer;
	}
}
