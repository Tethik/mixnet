package org.juddholm.mixnet.mixserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONMappedObject;
import org.json.JSONObj;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MixServerSettings extends JSONMappedObject {	

	@JSONObj
	protected String name;

	@JSONObj
	protected String previous;
	
	@JSONObj
	protected String next;	
	
	@JSONObj
	protected String[] verifyServers;	
	
	@JSONObj
	protected boolean last;	
	
	@JSONObj
	protected boolean first;
	
	@JSONObj
	protected int repetitions;
	
	
	public MixServerSettings(String filename) throws InvalidPropertiesFormatException, FileNotFoundException, IOException, JSONException, NotBoundException
	{		
		super(new JSONObject(new JSONTokener(new FileInputStream(filename))));
	}
	
	
	public String getName() {
		return name;
	}

	public String getPrevious() {
		return previous;
	}

	public String getNext() {
		return next;
	}

	/**
	 * @return the verifyServers
	 */
	public String[] getVerifyServers() {
		return verifyServers;
	}	
	
	public int getRepetitions()	{
		return repetitions;
	}
	
	public boolean isLast() {
		return last;
	}
	
	public boolean isFirst() {
		return first;
	}

}
