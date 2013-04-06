package org.juddholm.voteserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.json.JSONException;
import org.json.JSONMappedObject;
import org.json.JSONObj;
import org.json.JSONObject;
import org.json.JSONTokener;

public class VoteServerSettings extends JSONMappedObject {	

	@JSONObj
	protected String firstMixnode;
	
	@JSONObj
	protected String[] verifyServers;	
	
	@JSONObj
	protected String name;


	public VoteServerSettings(String filename) throws JSONException, FileNotFoundException {
		super(new JSONObject(new JSONTokener(new FileInputStream(filename))));
	}	
	
	public String getFirstMixnode()
	{
		return firstMixnode;
	}
	
	/**
	 * @return the verifyServers
	 */
	public String[] getVerifyServers() {
		return verifyServers;
	}	
	
	public String getName() {
		return name;
	}

}
