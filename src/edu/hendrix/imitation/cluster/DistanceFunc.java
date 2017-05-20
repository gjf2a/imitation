package edu.hendrix.imitation.cluster;

public interface DistanceFunc<T> {
	public double distance(T img1, T img2);
	
	default public long square(long value) {
		return value * value;
	}
}
