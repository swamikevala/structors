import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;

public class Tree {
	
	private int nextId;
	private Rational rate;
	private Graph<Node, DefaultEdge> tree;
	private Node root;
	
	private static final Rational ZERO = new Rational(0,1);
	private static final Rational ONE = new Rational(1,1);
	
	public Tree(Rational rate) {
		this.rate = rate;
		tree = new DefaultUndirectedGraph<Node, DefaultEdge>(DefaultEdge.class);
		root = new Node(1, ONE, ZERO);
		tree.addVertex(root);
		nextId = 2;
	}

	public void evolve(Rational amount){
		
		Set<RationalAndNode> ran = new HashSet<RationalAndNode>();
		Rational transAmount;
		Rational amountRemainder = amount;
		Node transNode;
		
		do {
			ran = getTransitions(amountRemainder);
			transAmount = ran.getRational();
			transNode = ran.getNode();
			
			//Update the weights
			for (Node node : tree.vertexSet()) {
				Rational weight = node.getWeight();
				Rational rate = node.getRate();
				node.setWeight(weight.add(rate.multiply(transAmount)));
			}
			
			//Check if this will cause node addition/removal 
			Rational tnwBefore = transNode.getWeight();
			Rational tnwAfter = tnwBefore.add(transAmount);
			Rational intDiff = tnwAfter.getAbsoluteValue().getFloor().minus(tnwBefore.getAbsoluteValue().getFloor());
			
			if ( !intDiff.equals(ZERO) ) {
				// integer threshold reached for some node so need to add/remove node
				
				assert transNode.getWeight().getFractionalPart().equals(ZERO) : " New parent node weight must be an integer multiple";
				
				// check if transNode is a leaf node and remove it if zero weight, else add new node
				if ( tree.edgesOf(transNode).size() == 1 && transNode.getWeight().equals(ZERO) ) {
					removeLeafNode(transNode);
				} else 
					addLeafNode(transNode);
				
				amountRemainder = amountRemainder.minus(transAmount);
					
			} 
		} while ( amountRemainder.compareTo(ZERO) > 0 );
	}
	
	public void evolve(Structor structor) {
		
	}
	
	// Find the max amount which can be added before one or more leaf vertices are added or removed
	
	private Set<RationalAndNode> getTransitions(Rational amount) {
		
		Rational transAmount = ONE;
		Node transNode = root;
		Rational minAmount = ZERO;
		
		for (Node node : tree.vertexSet()) {
			Rational weight = node.getWeight();
			Rational rate = node.getRate();
			if (weight.getFractionalPart().equals(ZERO)) {
				minAmount = ONE;
			} else if ( amount.isPositive() ) {
				minAmount = weight.getCeil().minus(weight);
				minAmount = minAmount.multiply(rate.getReciprocal());
			} else {
				minAmount = weight.getFloor().add(weight);
				minAmount = minAmount.multiply(rate.getReciprocal());
			}
			if ( minAmount.compareTo(transAmount) < 0 ) {
				transAmount = minAmount;
				transNode = node;
			}
		}
		// Split amount between nodes, proportionate to growth rate
		Rational unitAmount = amount.multiply(getRateSum().getReciprocal());
		Rational returnedAmount = ( unitAmount.compareTo(transAmount) < 0 ) ? unitAmount : transAmount;
		return new RationalAndNode(returnedAmount, transNode);
	}
	
	private Rational getRateSum() {
		Rational rateSum = ZERO;
		for (Node node : tree.vertexSet()) {
			Rational rate = node.getRate();
			rateSum = rateSum.add(rate);
		}
		return rateSum;
	}
	
	private void addLeafNode(Node parent) {
		Rational childRate = this.rate.multiply(parent.getRate());
		Node child = new Node(nextId, childRate, ZERO);
		tree.addVertex(child);
		tree.addEdge(parent, child);
		nextId++;
	}
	
	private void removeLeafNode(Node node) {
		tree.removeVertex(node);
	}
	
	public Graph<Node, DefaultEdge> getTree() {
		return tree;
	}

	public void setTree(Graph<Node, DefaultEdge> tree) {
		this.tree = tree;
	}
	
	public void draw() {
		VisualizationViewer<Node, DefaultEdge> vv =
        VisualizationViewer.builder(tree)
            .viewSize(new Dimension(700, 700))
            .layoutAlgorithm(new KKLayoutAlgorithm<>())
            .build();
    
		vv.getRenderContext().setVertexLabelFunction(v -> v.toString());

	    // create a frame to hold the graph visualization
	    final JFrame frame = new JFrame();
	    frame.getContentPane().add(vv.getComponent());
	    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
	}
	
	class RationalAndNode {
		
		private Rational rational;
		private Node node;
		
		public RationalAndNode(Rational rational, Node node) {
			this.rational = rational;
			this.node = node;
		}

		public Rational getRational() {
			return rational;
		}

		public Node getNode() {
			return node;
		}
	}
}
