package edu.hendrix.imitation.cluster;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.function.Function;

import edu.hendrix.imitation.util.DeepCopyable;
import edu.hendrix.imitation.util.EnumHistogram;
import edu.hendrix.imitation.util.Logger;
import edu.hendrix.imitation.util.Util;

import java.util.TreeMap;

public class LabeledBSOC<C extends Clusterable<C> & DeepCopyable<C>, I, E extends Enum<E>> {
	private BoundedSelfOrgCluster<C,I> bsoc;
	private TreeMap<Integer,EnumHistogram<E>> node2counts;
	private Class<E> enumClass;
	private E previous;
	
	private LabeledBSOC(Class<E> enumClass) {
		this.enumClass = enumClass;
		node2counts = new TreeMap<>();
	}
	
	public LabeledBSOC(Class<E> enumClass, DistanceFunc<C> dist, Function<I,C> transformer, int maxNumNodes, E startLabel) {
		this(enumClass);
		bsoc = new BoundedSelfOrgCluster<>(maxNumNodes, dist, transformer);
		previous = startLabel;
		bsoc.addListener(new BSOCListener(){
			@Override
			public void addingNode(int node) {
				addHistogram(node);
			}
			@Override
			public void replacingNode(int target, int replacement) {
				for (Entry<E, Integer> label: node2counts.get(target)) {
					EnumHistogram<E> toUpdate = node2counts.get(replacement);
					toUpdate.setCountFor(label.getKey(), label.getValue() + toUpdate.getCountFor(label.getKey()));
				}
				node2counts.remove(target);
			}
			@Override
			public void removingNode(int node) {
				node2counts.remove(node);
			}});
	}
	
	private void addHistogram(int node) {
		node2counts.put(node, new EnumHistogram<>(enumClass));
	}
	
	public void train(I example, E label) {
		int node = bsoc.train(example);
		// This shouldn't happen, but it did happen.
		if (!node2counts.containsKey(node)) {
			Logger.EV3Log.format("PATCHING BUG: Couldn't find node %d", node);
			addHistogram(node);
		}
		node2counts.get(node).bump(label);
	}
	
	public boolean isTrained() {
		return bsoc.size() > 0;
	}
	
	public int size() {return bsoc.size();}
	
	public E bestMatchFor(I example) {
		EnumHistogram<E> counts = getCountsFor(example);
		if (counts.getNumKeys() == 1) {
			previous = counts.getHighestCounted();
		}
		return previous;
	}
	
	public EnumHistogram<E> getCountsFor(I example) {
		Util.assertArgument(example != null, "No example!");
		int node = bsoc.getClosestMatchFor(example);
		return node2counts.get(node).deepCopy();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{");
		result.append(bsoc.toString());
		result.append("}");
		result.append("{");
		result.append(previous.toString());
		result.append("}");
		for (Entry<Integer, EnumHistogram<E>> entry: node2counts.entrySet()) {
			result.append("{{");
			result.append(entry.getKey().toString());
			result.append("}{");
			result.append(entry.getValue().toString());
			result.append("}}");
		}
		return result.toString();
	}
	
	public LabeledBSOC(Class<E> enumClass, String src, Function<String,C> extractor, DistanceFunc<C> dist, Function<I,C> transformer) {
		this(enumClass);
		ArrayList<String> parts = Util.debrace(src);
		bsoc = new BoundedSelfOrgCluster<C,I>(parts.get(0), extractor, dist, transformer);
		previous = Util.toEnum(enumClass, parts.get(1));
		for (int i = 2; i < parts.size(); i++) {
			ArrayList<String> treeParts = Util.debrace(parts.get(i));
			Integer key = Integer.parseInt(treeParts.get(0));
			node2counts.put(key, histogramFrom(enumClass, treeParts));
		}
	}
	
	private static <E extends Enum<E>> EnumHistogram<E> histogramFrom(Class<E> enumClass, ArrayList<String> parts) {
		if (parts.size() < 2) {
			return new EnumHistogram<>(enumClass);
		} else {
			return EnumHistogram.from(enumClass, parts.get(1));
		}
	}
}
