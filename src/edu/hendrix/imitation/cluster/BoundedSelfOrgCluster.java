package edu.hendrix.imitation.cluster;

import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;

import edu.hendrix.imitation.util.DeepCopyable;
import edu.hendrix.imitation.util.Duple;
import edu.hendrix.imitation.util.FixedSizeArray;
import edu.hendrix.imitation.util.Util;

// This data structure is an adaptation of the idea of Agglomerative Clustering.
// 
// Traditional agglomerative clustering is concerned with finding a hierarchical relationship
// among the data elements.
//
// Our goal here is to create an online learning algorithm with fast and predictable 
// runtime performance, suitable for both supervised and unsupervised learning.

public class BoundedSelfOrgCluster<C extends Clusterable<C> & DeepCopyable<C>, I> implements Clusterer<C,I>, DeepCopyable<BoundedSelfOrgCluster<C,I>> {
	// Object state
	private FixedSizeArray<Node<C>> nodes;
	private ArrayList<TreeSet<Edge<C>>> nodes2edges;
	private TreeSet<Edge<C>> edges;
	private NodeTransitions transitions;
	
	// Alternative distance function
	private DistanceFunc<C> dist;
	
	// Input transformation function
	private Function<I,C> transformer;
	
	// Tracking for transitions
	private Optional<Integer> lastMatchingNode;
	
	public int maxNumNodes() {return nodes.capacity() - 1;}
	
	// Notification
	private ArrayList<BSOCListener> listeners = new ArrayList<>();

	@Override
	public BoundedSelfOrgCluster<C,I> deepCopy() {
		BoundedSelfOrgCluster<C,I> result = new BoundedSelfOrgCluster<>(size(), dist, transformer);
		deepCopyHelp(result);
		return result;
	}
	
	protected void deepCopyHelp(BoundedSelfOrgCluster<C,I> result) {
		for (Edge<C> edge: this.edges) {
			result.edges.add(edge.deepCopy());
		}
		result.nodes = this.nodes.deepCopy();
		result.setupTransitions(this.transitions.deepCopy());
		result.lastMatchingNode = this.lastMatchingNode;
	}

	public BoundedSelfOrgCluster(int maxNumNodes, DistanceFunc<C> dist, Function<I,C> transformer) {
		setupBasic(dist, transformer);
		setupAvailable(maxNumNodes);
	}
	
	private void setupBasic(DistanceFunc<C> dist, Function<I,C> transformer) {
		this.dist = dist;
		this.transformer = transformer;
		this.edges = new TreeSet<>();		
		this.nodes2edges = new ArrayList<>();
		this.lastMatchingNode = Optional.empty();
	}
	
	private void setupTransitions(NodeTransitions transitions) {
		this.transitions = transitions;
		this.addListener(transitions);
	}
	
	private void setupAvailable(int maxNumNodes) {
		this.nodes = FixedSizeArray.make(maxNumNodes + 1);
		setupTransitions(new NodeTransitions(this.nodes.capacity()));
		Util.assertState(size() == 0, "size() should be zero, but is " + size());
	}
	
	public BoundedSelfOrgCluster(String src, Function<String,C> extractor, DistanceFunc<C> dist, Function<I,C> transformer) {
		setupBasic(dist, transformer);
		ArrayList<String> topLevel = Util.debrace(src);
		fromStringHelp(topLevel, extractor);
	}
	
	protected void fromStringHelp(ArrayList<String> topLevel, Function<String,C> extractor) {
		rebuildAvailable(topLevel.get(0));
		if (topLevel.size() > 1) {
			rebuildNodes(topLevel.get(1), extractor);
		}
		if (topLevel.size() > 2) {
			rebuildEdges(topLevel.get(2));
		}
		setupTransitions(topLevel.size() > 3 
				? new NodeTransitions(topLevel.get(3)) 
				: new NodeTransitions(maxNumNodes()));
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{");
		result.append(maxNumNodes());
		result.append("}\n{");
		nodes.doAll((i, v) -> {
			result.append('{');
			result.append(v);
			result.append('}');
		});
		result.append("}\n{");
		for (Edge<C> edge: edges) {
			result.append('{');
			result.append(edge.toString());
			result.append('}');
		}
		result.append("}\n{");
		result.append(transitions.toString());
		result.append("}");
		return result.toString();
	}
	
	public boolean nodeExists(int node) {
		return nodes.containsKey(node);
	}
	
	public void addListener(BSOCListener listener) {
		listeners.add(listener);
	}
	
	private void rebuildAvailable(String availStr) {
		ArrayList<String> availability = Util.debrace(availStr);
		int maxNumNodes = Integer.parseInt(availability.get(0));
		setupAvailable(maxNumNodes);
	}
	
	private void rebuildNodes(String nodeStr, Function<String,C> extractor) {
		for (String node: Util.debrace(nodeStr)) {
			Node<C> newNode = new Node<>(node, extractor);
			nodes.put(newNode.getID(), newNode);
			nodes2edges.add(new TreeSet<>());
		}
	}
	
	private void rebuildEdges(String edgeStr) {
		for (String edge: Util.debrace(edgeStr)) {
			Edge<C> newEdge = new Edge<>(edge);
			edges.add(newEdge);
			nodes2edges.get(newEdge.getNode1()).add(newEdge);
			nodes2edges.get(newEdge.getNode2()).add(newEdge);
		}
	}
	
	public int size() {return nodes.size();}
	
	public int getStartingLabel() {return 0;}
	
	@Override
	public double distance(C one, C two) {
		return dist.distance(one, two);
	}
	
	private double distance(Node<C> n1, Node<C> n2) {
		return Math.max(n1.getNumInputs(), n2.getNumInputs()) * distance(n1.getCluster(), n2.getCluster());
	}
	
	private void removeAllEdgesFor(int node) {
		for (Edge<C> edge: nodes2edges.get(node)) {
			edges.remove(edge);
			nodes2edges.get(edge.getOtherNode(node)).remove(edge);
		}
		nodes2edges.get(node).clear();
	}
	
	private void createEdgesFor(int node) {
		for (int i = nodes.getLowestInUse(); i < nodes.capacity(); i = nodes.nextInUse(i)) {
			if (i != node) {
				double distance = distance(nodes.get(i), nodes.get(node));
				Edge<C> edge = new Edge<>(Math.min(i, node), Math.max(i, node), distance);
				edges.add(edge);
				nodes2edges.get(node).add(edge);
				nodes2edges.get(i).add(edge);
			}
		}
	}
	
	@Override
	public int train(I example) {
		C transformed = transformer.apply(example);
		int where = nodes.getLowestAvailable();
		insert(new Node<>(where, transformed));
		notifyAdd(where);
		if (nodes.size() > maxNumNodes()) {
			where = removeAndMerge();
		}
		Util.assertState(nodes.getHighestInUse() == nodes.size() - 1, "Not compact");
		int match = getClosestMatchFor(example);
		lastMatchingNode.ifPresent(lastMatch -> {
			transitions.transition(lastMatch, match);
		});
		lastMatchingNode = Optional.of(match);
		return match;
	}
	
	private void insert(Node<C> example) {
		nodes.put(example.getID(), example);
		Util.assertState(example == nodes.get(example.getID()), "Went to the wrong place");
		Util.assertState(nodes2edges.size() >= example.getID(), String.format("nodes2edges mismatch! exampleID: %d nodes2edges.size: %d", example.getID(), nodes2edges.size()));
		if (example.getID() == nodes2edges.size()) {
			nodes2edges.add(new TreeSet<>());
		}
		createEdgesFor(example.getID());
	}

	private int removeAndMerge() {
		Edge<C> smallest = edges.first();
		Node<C> removedNode = removeNode(smallest.getNode2());
		Node<C> absorberNode = removeNode(smallest.getNode1());
		
		Node<C> merged = absorberNode.mergedWith(removedNode);
		insert(merged);
		notifyReplace(removedNode.getID(), absorberNode.getID());
		return placeMergedNode(removedNode, absorberNode);
	}
	
	private int placeMergedNode(Node<C> removedNode, Node<C> absorberNode) {
		int unused = removedNode.getID();
		if (unused > nodes.getHighestInUse()) {
			return absorberNode.getID();
		} else {
			Node<C> tooHighNode = removeNode(nodes.getHighestInUse());
			tooHighNode.renumber(unused);
			insert(tooHighNode);
			return tooHighNode.getID();
		}		
	}
	
	private Node<C> removeNode(int target) {
		removeAllEdgesFor(target);
		return nodes.remove(target);
	}
	
	public void delete(int node) {
		removeNode(node);
		notifyDelete(node);
	}

	private void notifyDelete(int node) {
		for (BSOCListener listener: listeners) {
			listener.removingNode(node);
		}
	}

	private void notifyAdd(int added) {
		for (BSOCListener listener: listeners) {
			listener.addingNode(added);
		}
	}
	
	private void notifyReplace(int original, int replacement) {
		for (BSOCListener listener: listeners) {
			listener.replacingNode(original, replacement);
		}
	}
	
	public boolean edgeRepresentationConsistent() {
		for (int i = 0; i < nodes2edges.size(); i++) {
			if (nodeExists(i)) {
				for (Edge<C> edge: nodes2edges.get(i)) {
					if (!edges.contains(edge)) {
						return false;
					}
				}
			} else {
				if (!nodes2edges.get(i).isEmpty()) {
					return false;
				}
			}
		}
		
		for (Edge<C> edge: edges) {
			if (!nodeExists(edge.getNode1())) {return false;}
			if (!nodes2edges.get(edge.getNode1()).contains(edge)) {return false;}
			if (!nodeExists(edge.getNode2())) {return false;}
			if (!nodes2edges.get(edge.getNode2()).contains(edge)) {return false;}
		}
		
		return true;
	}
	
	@Override
	public C getIdealInputFor(int node) {
		Util.assertArgument(nodes.containsKey(node), "Node " + node + " not present");
		return nodes.get(node).getCluster();
	}
	
	public int getNumMergesFor(int node) {
		Util.assertArgument(nodes.containsKey(node), "Node " + node + " not present");
		return nodes.get(node).getNumInputs();
	}
	
	public int getTotalSourceInputs() {
		int total = 0;
		for (Node<C> node: nodes.values()) {
			total += node.getNumInputs();
		}
		return total;
	}

	@Override
	public ArrayList<Integer> getClusterIds() {
		return nodes.indices();
	}
	
	public ArrayList<C> getIdealInputs() {
		ArrayList<C> result = new ArrayList<C>();
		for (Node<C> n: nodes.values()) {
			result.add(n.getCluster());
		}
		return result;
	}
	
	public ArrayList<Duple<Integer,Integer>> transitionCountsFor(int node) {
		return transitions.countsFor(node);
	}
	
	@Override
	public boolean equals(Object other) {
		return toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public C transform(I input) {
		return transformer.apply(input);
	}
}
