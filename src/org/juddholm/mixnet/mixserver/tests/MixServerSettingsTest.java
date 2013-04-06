package org.juddholm.mixnet.mixserver.tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.InvalidPropertiesFormatException;

import org.json.JSONException;
import org.juddholm.mixnet.mixserver.MixServerSettings;
import org.junit.Before;
import org.junit.Test;

public class MixServerSettingsTest {
	
	/**
		{
			name: "Test",
			prev: "1",
			next: "3",
			verifyServers: [
				"v1",
				"v2",
				"v3"	
			]
		}}
	 */

	@Test
	public void test() {
		MixServerSettings settings = null;
		try {
			settings = new MixServerSettings("tests/MixServer.json");
		} catch (IOException | JSONException | NotBoundException e) {		
			e.printStackTrace();
			fail("Failed parsing json file: " + e.toString());
		}
		
		assertEquals("Test", settings.getName());
		assertEquals("1", settings.getPrevious());
		assertEquals("3", settings.getNext());
		
		String[] vs = { "v1", "v2", "v3" };
		assertArrayEquals(vs, settings.getVerifyServers());
		assertEquals(3, settings.getRepetitions());
		assertTrue(settings.isFirst());
	}

}
