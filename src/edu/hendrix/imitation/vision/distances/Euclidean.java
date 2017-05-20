package edu.hendrix.imitation.vision.distances;

import edu.hendrix.imitation.vision.ImageDistanceFunc;

public class Euclidean implements ImageDistanceFunc {
	@Override
	public double calculationAt(int a, int b) {
		return Math.pow(b - a, 2);
	}
}
