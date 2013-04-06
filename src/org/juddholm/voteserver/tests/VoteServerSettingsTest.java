package org.juddholm.voteserver.tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.json.JSONException;
import org.juddholm.voteserver.VoteServerSettings;
import org.junit.Test;

public class VoteServerSettingsTest {

	/**
		{
			firstMixnode: "TestMix",
			"verifyServers": [
				"v1",
				"v2",
				"v3"	
			]
		}
	 */
	@Test
	public void testVoteServerSettings() {
		VoteServerSettings settings = null;
		try {
			settings = new VoteServerSettings("tests/VoteServer.json");
		} catch (FileNotFoundException | JSONException e) {			
			e.printStackTrace();
			fail("Parsing failed " + e.toString());
		}	
		
		assertEquals("TestMix", settings.getFirstMixnode());
		String[] vs = { "v1", "v2", "v3" };
		assertArrayEquals(vs, settings.getVerifyServers());		
		assertEquals("Test", settings.getName());
	}

}
