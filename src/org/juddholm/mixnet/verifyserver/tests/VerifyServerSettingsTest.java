package org.juddholm.mixnet.verifyserver.tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.json.JSONException;
import org.juddholm.mixnet.verifyserver.VerifyServerSettings;
import org.junit.Before;
import org.junit.Test;

public class VerifyServerSettingsTest {
	
	
	/**
		{
			"name": "Test",
			"mixnodes": [
				"m1",
				"m2",
				"m3"	
			],
			"repetitions": 3
		}
	 */
	
	@Test
	public void test() {
		VerifyServerSettings settings = null;
		try {
			settings = new VerifyServerSettings("tests/VerifyServer.json");
		} catch (FileNotFoundException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Failed parsing: " + e.toString());
		}
		assertEquals("Test", settings.getName());
		String[] ms = new String[] {"m1", "m2", "m3" };
		assertArrayEquals(ms, settings.getMixnodes());
		assertEquals(3, settings.getRepetitions());
		assertEquals("testvoteserver", settings.getVoteServer());
	}

}
