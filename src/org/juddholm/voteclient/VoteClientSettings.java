package org.juddholm.voteclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.json.JSONException;
import org.json.JSONMappedObject;
import org.json.JSONObj;
import org.json.JSONObject;
import org.json.JSONTokener;

public class VoteClientSettings extends JSONMappedObject {
	
	@JSONObj
	protected String[] mixnodes;
	
	@JSONObj
	protected String voteServer;
	
	@JSONObj
	protected int repetitions;

	public VoteClientSettings(String filename) throws JSONException, FileNotFoundException {
		super(new JSONObject(new JSONTokener(new FileInputStream(filename))));	
	}

	public String[] getMixnodes() {
		return mixnodes;
	}

	public String getVoteServer() {
		return voteServer;
	}	
	
	public int getRepetitions()	{
		return repetitions;
	}
}
