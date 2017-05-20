package edu.hendrix.imitation.cluster;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import edu.hendrix.imitation.util.Duple;
import edu.hendrix.imitation.util.Util;

public class NodeTransitionsTest {

	@Test
	public void testBump() {
		NodeTransitions target = new NodeTransitions(16);
		target.transition(2, 3);
		target.transition(3, 4);
		assertEquals(new Duple<>(3,1), target.countsFor(2).get(0));
		assertEquals(new Duple<>(4,1), target.countsFor(3).get(0));
		target.replacingNode(3, 4);
		assertEquals(new Duple<>(4,1), target.countsFor(2).get(0));
		assertEquals(0, target.countsFor(3).size());
		assertEquals(new Duple<>(4,1), target.countsFor(4).get(0));
	}

	@Test
	public void testSerialization1a() throws FileNotFoundException {
		testNTSerial("plan1oldtransitions.txt");
	}
	
	@Test
	public void testSerialization1b() throws FileNotFoundException {
		testNTSerial("plan1transitions.txt");
	}
	
	public void testNTSerial(String filename) throws FileNotFoundException {
		String original = Util.fileToString(new File("src/" + filename));
		NodeTransitions rebuilt = new NodeTransitions(original);
		assertEquals(original.trim(), rebuilt.toString());		
	}
	
	@Test
	public void testSerialization2a() throws FileNotFoundException {
		testBSOCSerial("plan1old.txt");
	}
	
	@Test
	public void testSerialization2b() throws FileNotFoundException {
		testBSOCSerial("plan1.txt");
	}
	
	public void testBSOCSerial(String filename) throws FileNotFoundException {
		String original = Util.fileToString(new File("src/" + filename));
		ShrinkingImageBSOC rebuilt = ShrinkingImageBSOC.fromString(original);
		assertEquals(original.trim(), rebuilt.toString());
	}
}
