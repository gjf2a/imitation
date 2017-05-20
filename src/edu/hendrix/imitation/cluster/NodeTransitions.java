package edu.hendrix.imitation.cluster;

import java.util.ArrayList;

import edu.hendrix.imitation.util.DeepCopyable;
import edu.hendrix.imitation.util.Duple;
import edu.hendrix.imitation.util.FixedSizeArray;
import edu.hendrix.imitation.util.Util;

public class NodeTransitions implements DeepCopyable<NodeTransitions>, BSOCListener {
	private FixedSizeArray<FixedSizeArray<Integer>> transitions;
	private int size;
	
	public NodeTransitions(int numNodes) {
		transitions = FixedSizeArray.make(numNodes);
		size = numNodes;
		for (int i = 0; i < numNodes; i++) {
			transitions.put(i, makeBlank(numNodes));
		}
	}
	
	public static FixedSizeArray<Integer> makeBlank(int size) {
		FixedSizeArray<Integer> row = FixedSizeArray.makeImmutableType(size);
		row.setAll(i -> 0);
		return row;
	}
	
	public int size() {return size;}
	
	private NodeTransitions(FixedSizeArray<FixedSizeArray<Integer>> other) {
		transitions = other;
		size = transitions.capacity();
	}
	
	public NodeTransitions(String src) {
		this(FixedSizeArray.parse(src, s -> FixedSizeArray.parseImmutableType(s, intStr -> Integer.parseInt(intStr))));
	}
	
	public void transition(int start, int end) {
		FixedSizeArray<Integer> counts = transitions.get(start);
		counts.put(end, counts.get(end) + 1);
	}
	
	public ArrayList<Duple<Integer,Integer>> countsFor(int node) {
		ArrayList<Duple<Integer,Integer>> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			if (transitions.get(node).get(i) > 0) {
				result.add(new Duple<>(i, transitions.get(node).get(i)));
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object other) {
		return toString().equals(other.toString());
	}
	
	public String toString() {
		return transitions.toString();
	}

	@Override
	public NodeTransitions deepCopy() {
		return new NodeTransitions(transitions.deepCopy());
	}

	@Override
	public void addingNode(int node) {}

	@Override
	public void removingNode(int node) {
		transitions.remove(node);
	}

	@Override
	public void replacingNode(int target, int replacement) {
		replaceToTarget(target, replacement);
		replaceFromTarget(target, replacement);
	}
	
	private void replaceToTarget(int target, int replacement) {
		for (int i = 0; i < size(); i++) {
			FixedSizeArray<Integer> row = transitions.get(i);
			Util.assertState(row != null, "Row " + i + " is null");
			Util.assertState(row.get(replacement) != null, "Replacement (" + replacement + ") is null");
			Util.assertState(row.get(target) != null, "Target (" + target + ") is null");
			row.put(replacement, row.get(replacement) + row.get(target));
			row.put(target, 0);
		}
	}
	
	private void replaceFromTarget(int target, int replacement) {
		FixedSizeArray<Integer> replaceRow = transitions.get(replacement);
		for (int i = 0; i < size(); i++) {
			replaceRow.put(i, replaceRow.get(i) + transitions.get(target).get(i));
		}
		transitions.put(target, makeBlank(size()));
	}
}
