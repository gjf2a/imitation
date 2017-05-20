package edu.hendrix.imitation.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CycleTimerTest {

	@Test
	public void testLast() {
		CycleTimer timer = new CycleTimer();
		timer.start();
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() < start + 1000) {}
		timer.bumpCycle();
		double time = timer.getLastDuration() / 1000.0;
		System.out.println("time: " + time);
		assertEquals(1.0, time, 0.01);
	}
	
	@Test
	public void testHz() {
		CycleTimer timer = new CycleTimer();
		timer.start();
		for (int i = 0; i < 10; i++) {
			long start = System.currentTimeMillis();
			while (System.currentTimeMillis() < start + 100) {}
			timer.bumpCycle();
		}
		System.out.println(timer.cyclesPerSecond() + " hz");
		assertEquals(10.0, timer.cyclesPerSecond(), 0.01);
	}
}
