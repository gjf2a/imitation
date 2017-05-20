package edu.hendrix.imitation.vision.distances;

import edu.hendrix.imitation.vision.ImageDistanceFunc;

// My implementation of the metric from the following paper:
//
// Hassanat  B.  A. Dimensionality  Invariant  Similarity  Measure. J  Am  Sci 2014;10(8):221-226].  (ISSN:  1545-1003).
//
// https://www.researchgate.net/publication/264995324_Dimensionality_Invariant_Similarity_Measure

public class DimInvSimMeasure implements ImageDistanceFunc {
	public double calculationAt(int a, int b) {
		return 1 - ((1 + Math.min((double)a, (double)b)) / (1 + Math.max((double)a, (double)b)));
	}
}
