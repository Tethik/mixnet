package org.juddholm.voteclient.tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.json.JSONException;
import org.juddholm.voteclient.VoteClientSettings;
import org.junit.Before;
import org.junit.Test;

public class VoteClientSettingsTest {

	/**
		{
			"voteServer": "TestBallot",
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
		VoteClientSettings settings = null;
		try {
			settings = new VoteClientSettings("tests/VoteClient.json");
		} catch (FileNotFoundException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Failed parsing: " + e.toString());
		}
		assertEquals("TestBallot", settings.getVoteServer());
		String[] ms = { "m1", "m2", "m3" };
		assertArrayEquals(ms, settings.getMixnodes());
		assertEquals(3, settings.getRepetitions());
	}

}
