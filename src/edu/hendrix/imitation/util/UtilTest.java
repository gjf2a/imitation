package edu.hendrix.imitation.util;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class UtilTest {

	@Test
	public void debraceTest1() {
		testDebrace("one", "two", "three");
	}
	
	@Test
	public void debraceTest2() {
		testDebrace("one", "", "three");
	}

	public void testDebrace(String... parts) {
		String braced = "";
		for (String part: parts) {
			braced += "{" + part + "}";
		}
		
		ArrayList<String> parted = Util.debrace(braced);
		assertEquals(parts.length, parted.size());
		for (int i = 0; i < parted.size(); i++) {
			assertEquals(parts[i], parted.get(i));
		}
	}
	
	@Test
	public void testMod() {
		int target = 0;
		for (int i = -30; i < 30; i++) {
			assertEquals(target, Util.trueMod(i, 5));
			target = (target + 1) % 5;
		}
	}
}
