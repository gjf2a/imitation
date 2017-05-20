package edu.hendrix.imitation.cluster;

import java.util.ArrayList;

import edu.hendrix.imitation.util.Util;
import edu.hendrix.imitation.vision.AdaptedYUYVImage;
import edu.hendrix.imitation.vision.distances.Euclidean;

public class ShrinkingImageBSOC extends BoundedSelfOrgCluster<AdaptedYUYVImage,AdaptedYUYVImage> {
	private int shrink;
	
	public ShrinkingImageBSOC(int nodes, int shrink) {
		super(nodes, new Euclidean(), img -> img.shrunken(shrink));
		this.shrink = shrink;
	}
	
	private ShrinkingImageBSOC(String src, int shrink) {
		super(src, AdaptedYUYVImage::fromString, new Euclidean(), img -> img.shrunken(shrink));
		this.shrink = shrink;
	}
	
	public static ShrinkingImageBSOC fromString(String src) {
		ArrayList<String> parts = Util.debrace(src);
		return new ShrinkingImageBSOC(parts.get(0), Integer.parseInt(parts.get(1)));
	}
	
	@Override
	public String toString() {
		return "{" + super.toString() + "}{" + shrink + "}";
	}
}
