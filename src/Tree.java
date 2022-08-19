import java.awt.Dimension;
import java.util.TreeSet;
import java.util.SortedSet;

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
		
		SortedSet<Node> transNodes = new TreeSet<Node>();
		Rational amountRemainder = amount;
		Rational updateAmount;
		Rational transAmount;
		
		do {
			transNodes = getTransitionNodes(amountRemainder);

			if ( updateAmount(transNodes.first()).compareTo(amountRemainder) > 0 ) {
				
				// Not enough remaining amount to reach a transition
				// Split amount between nodes, proportionate to growth rate
				Rational unitAmount = amountRemainder.multiply(getRateSum().getReciprocal());
				updateAmount = updateNodes(unitAmount);
				
				assert updateAmount.equals(amount) : " Non-transition update amount should be the same as the full amount"; 
				
			} else {
				
				// update nodes and add/remove nodes at transitions
				Node firstTransNode = transNodes.first();
				transAmount = ( amount.isPositive() ) ? firstTransNode.getPosTransitionAmount() : firstTransNode.getNegTransitionAmount();
				updateAmount = updateNodes(transAmount);
				
				// Handle node addition/removal
				for ( Node transNode : transNodes ) {
						
					assert transNode.getWeight().getFractionalPart().equals(ZERO) : " New parent node weight must be an integer multiple";
					
					// check if transNode is a leaf node and remove it if zero weight, else add new node
					if ( tree.edgesOf(transNode).size() == 1 && transNode.getWeight().equals(ZERO) ) {
						removeLeafNode(transNode);
					} else 
						addLeafNode(transNode);
				} 
				amountRemainder = amountRemainder.minus(updateAmount);
			}
				
		} while ( amountRemainder.compareTo(ZERO) > 0 );
	}
	
	public void evolve(Structor structor) {
		
	}
	
	// Calculate transition amounts
	// Find the max amount which can be added before one or more leaf vertices are added or removed
	
	private SortedSet<Node> getTransitionNodes(Rational amount) {
		
		SortedSet<Node> transitionNodes = new TreeSet<Node>();
		Rational minAmt = ONE;
		
		assert !amount.equals(ZERO)  : " Amount must be non-zero";
		
		// Find minimum
		for (Node node : tree.vertexSet()) {
			Rational posTransAmt = node.getPosTransitionAmount();
			Rational negTransAmt = node.getNegTransitionAmount();
			
			if ( amount.isPositive() ) {
				minAmt = ( posTransAmt.compareTo(minAmt) < 0 ) ? posTransAmt : minAmt;
			} else {
				minAmt = ( negTransAmt.compareTo(minAmt) < 0 ) ? negTransAmt : minAmt;
			}
		}
		// Find all nodes having minimum
		for (Node node : tree.vertexSet()) {
			if ( amount.isPositive() && node.getPosTransitionAmount().equals(minAmt) || !amount.isPositive() && node.getNegTransitionAmount().equals(minAmt) ) {
				transitionNodes.add(node);
			} 
		}
		return transitionNodes;
	}
	
	private Rational updateNodes(Rational amount) {
		
		Rational updateAmount = ZERO;
		
		//Update the weights and transition amounts
		for (Node node : tree.vertexSet()) {
			// add weights
			Rational weight = node.getWeight();
			Rational rate = node.getRate();
			Rational increment = rate.multiply(amount);
			Rational newWeight = weight.add(increment);
			
			assert !( weight.getAbsoluteValue().getFloor().compareTo(newWeight.getAbsoluteValue().getFloor()) < 0 && !newWeight.getFractionalPart().equals(ZERO) )  
			: " updated node weight should not have crossed integer transition";
			
			node.setWeight(newWeight);
			updateAmount = updateAmount.add(increment);
			
			//update transition amounts
			if ( newWeight.getFractionalPart().equals(ZERO) ) {
				node.setPosTransitionAmount(ONE);
				node.setNegTransitionAmount(ZERO);
			} else {
				node.setPosTransitionAmount(newWeight.getCeil().minus(newWeight).multiply(rate.getReciprocal()));
				node.setNegTransitionAmount(newWeight.getFloor().add(newWeight).multiply(rate.getReciprocal()));
			}
		}
		return updateAmount;
	}
	
	// Calculates total tree update amount needed for reaching transition
	private Rational updateAmount(Node transNode) {
		
		Rational updateAmount = ZERO;
		Rational scalingFactor = transNode.getWeight().multiply(transNode.getRate().getReciprocal());
		for (Node node : tree.vertexSet()) {
			// add scaled amounts
			Rational rate = node.getRate();
			Rational increment = rate.multiply(scalingFactor);
			updateAmount = updateAmount.add(increment);
		}
		return updateAmount;
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
    
		vv.getRenderContext().setVertexLabelFunction(v -> v.getWeight().toString());

	    // create a frame to hold the graph visualization
	    final JFrame frame = new JFrame();
	    frame.getContentPane().add(vv.getComponent());
	    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
	}
	
}
