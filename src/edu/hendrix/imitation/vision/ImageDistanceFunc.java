package edu.hendrix.imitation.vision;

import edu.hendrix.imitation.cluster.DistanceFunc;

public interface ImageDistanceFunc extends DistanceFunc<AdaptedYUYVImage> {
	default public double distance(AdaptedYUYVImage img1, AdaptedYUYVImage img2) {
		double total = 0.0;
		for (int x = 0; x < img1.getWidth(); ++x) {
			for (int y = 0; y < img1.getHeight(); ++y) {
				total += calculationAt(img1.getY(x, y), img2.getY(x, y));
				total += calculationAt(img1.getU(x, y), img2.getU(x, y));
				total += calculationAt(img1.getV(x, y), img2.getV(x, y));
			}
		}
		return total;
	}
	
	public double calculationAt(int a, int b);
}
