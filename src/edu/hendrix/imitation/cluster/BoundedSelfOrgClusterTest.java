package edu.hendrix.imitation.cluster;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BoundedSelfOrgClusterTest {
	BoundedSelfOrgCluster<BSOCTestee,BSOCTestee> bsoc1;
	final int MAX_NODES = 3;
	
	@Before
	public void setup() {
		bsoc1 = new BoundedSelfOrgCluster<>(MAX_NODES, BSOCTestee::distanceTo, t -> t);
		stringTest("{3}\n{}\n{}\n{{4}{{{0}{{4}{}}}{{1}{{4}{}}}{{2}{{4}{}}}{{3}{{4}{}}}}}");
		for (long value = 0; value < MAX_NODES; value++) {
			bsoc1.train(new BSOCTestee(value*value));
		}
	}

	@Test
	public void stringTest1() {
		stringTest("{3}\n{{{0}{1}{0}}{{1}{1}{1}}{{2}{1}{4}}}\n{{0;1;1.0}{1;2;3.0}{0;2;4.0}}\n{{4}{{{0}{{4}{{{1}{1}}}}}{{1}{{4}{{{2}{1}}}}}{{2}{{4}{}}}{{3}{{4}{}}}}}");
	}

	@Test
	public void stringTest2() {
		bsoc1.train(new BSOCTestee(MAX_NODES * MAX_NODES));
		stringTest("{3}\n{{{0}{2}{0}}{{1}{1}{9}}{{2}{1}{4}}}\n{{1;2;5.0}{0;2;8.0}{0;1;18.0}}\n{{4}{{{0}{{4}{{{0}{1}}{{1}{0}}{{2}{1}}{{3}{0}}}}}{{1}{{4}{}}}{{2}{{4}{{{0}{0}}{{1}{1}}}}}{{3}{{4}{{{0}{0}}{{1}{0}}}}}}}");
	}
	
	@Test
	public void stringTest3() {
		stringTest2();
		bsoc1.train(new BSOCTestee(3));
		stringTest("{3}\n{{{0}{2}{0}}{{1}{1}{9}}{{2}{2}{3}}}\n{{0;2;6.0}{1;2;12.0}{0;1;18.0}}\n{{4}{{{0}{{4}{{{0}{1}}{{1}{0}}{{2}{1}}{{3}{0}}}}}{{1}{{4}{{{2}{1}}{{3}{0}}}}}{{2}{{4}{{{0}{0}}{{1}{1}}{{2}{0}}{{3}{0}}}}}{{3}{{4}{}}}}}");
	}
	
	public void stringTest(String target) {
		assertTrue(bsoc1.edgeRepresentationConsistent());
		assertEquals(target, bsoc1.toString());
		assertEquals(bsoc1, new BoundedSelfOrgCluster<BSOCTestee,BSOCTestee>(bsoc1.toString(), BSOCTestee::new, BSOCTestee::distanceTo, t -> t));
	}
}
