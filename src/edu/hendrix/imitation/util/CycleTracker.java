package edu.hendrix.imitation.util;

public class CycleTracker {
	private long cycles, duration, lastDuration, lastTime;
	
	public CycleTracker() {
		cycles = duration = lastDuration = 0;
		lastTime = System.currentTimeMillis();
	}
	
	public void cycle() {
		cycles += 1;
		long time = System.currentTimeMillis();
		lastDuration = duration;
		duration += time - lastTime;
		lastTime = time;
	}

	public long getCycles() {return cycles;}
	
	public long getDuration() {return duration;}
	
	public double getFPS() {return 1000.0 * cycles / duration;}
	
	public String getFPSString() {return String.format("%5.2f", getFPS());}
	
	public long getLastCycleTime() {return duration - lastDuration;}
}
