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
	private Rational rate; // current rate (tree rate can change for compound structors) 
	private Graph<Node, DefaultEdge> tree;
	private Node root;
	
	public Tree(Rational rate) {
		this.rate = rate;
		tree = new DefaultUndirectedGraph<Node, DefaultEdge>(DefaultEdge.class);
		root = new Node(this);
		tree.addVertex(root);
		nextId = 2;
	}

	public void evolve(Rational amount) {
		evolve(this.rate, amount);
	}
	
	public void evolve(Structor structor) {
		evolve(structor.getRate(), structor.getMagnitude());
	}
	
	public void evolve(Rational newRate, Rational amount){
		
		if ( !rate.equals(newRate) )
			updateTree(newRate);
		
		SortedSet<Node> transNodes = new TreeSet<Node>();
		Rational amountRemainder = amount;
		Rational updateAmount;
		Rational transAmount;
		
		do {
			transNodes = getTransitionNodes(amountRemainder);

			if ( updateAmount(transNodes.first()).getAbsoluteValue().lessThan(amountRemainder.getAbsoluteValue()) ) {
				
				// update nodes and add/remove nodes at transitions
				Node firstTransNode = transNodes.first();
				transAmount = ( amount.greaterThan(Rational.ZERO) ) ? firstTransNode.getPosTransitionAmount() : firstTransNode.getNegTransitionAmount();
				updateAmount = updateNodes(transAmount);
				
				// Handle node addition/removal
				for ( Node transNode : transNodes ) {
						
					assert transNode.getWeight().getFractionalPart().equals(Rational.ZERO) : " New parent node weight must be an integer multiple";
					
					// check if transNode is a leaf node and remove it if zero weight, else add new node
					if ( tree.edgesOf(transNode).size() == 1 && transNode.getWeight().equals(Rational.ZERO) ) {
						removeLeafNode(transNode);
					} else 
						addLeafNode(transNode);
				} 
				
			} else {
				
				// Not enough remaining amount to reach a transition
				// Split amount between nodes, proportionate to growth rate
				Rational unitAmount = amountRemainder.multiply(getRateSum().getReciprocal());
				updateAmount = updateNodes(unitAmount);
				
				assert updateAmount.equals(amountRemainder) : " Non-transition update amount should be the same as the full amount"; 
			}
			
			amountRemainder = amountRemainder.minus(updateAmount);
				
		} while ( !amountRemainder.equals(Rational.ZERO) );
	}
	
	// Calculate transition amounts
	// Find the max amount which can be added before one or more leaf vertices are added or removed
	
	private SortedSet<Node> getTransitionNodes(Rational amount) {
		
		SortedSet<Node> transitionNodes = new TreeSet<Node>();
		Rational smallestAmt = ( amount.greaterThan(Rational.ZERO) ) ? Rational.ONE : Rational.MINUS_ONE;
		
		assert !amount.equals(Rational.ZERO)  : " Amount must be non-zero";
		
		// Find smallest
		for (Node node : tree.vertexSet()) {
			Rational posTransAmt = node.getPosTransitionAmount();
			Rational negTransAmt = node.getNegTransitionAmount();
			
			if ( amount.greaterThan(Rational.ZERO) ) {
				smallestAmt = ( posTransAmt.lessThanOrEqual(smallestAmt) ) ? posTransAmt : smallestAmt;
			} else {
				smallestAmt = ( negTransAmt.greaterThanOrEqual(smallestAmt) ) ? negTransAmt : smallestAmt;
			}
		}
		// Find all nodes having minimum
		for (Node node : tree.vertexSet()) {
			if ( amount.greaterThan(Rational.ZERO) && node.getPosTransitionAmount().equals(smallestAmt) 
					|| amount.lessThan(Rational.ZERO) && node.getNegTransitionAmount().equals(smallestAmt) ) {
				transitionNodes.add(node);
			} 
		}
		return transitionNodes;
	}
	
	private Rational updateNodes(Rational amount) {
		
		Rational updateAmount = Rational.ZERO;
		
		//Update the weights and transition amounts
		for (Node node : tree.vertexSet()) {
			// add weights
			Rational weight = node.getWeight();
			Rational rate = node.getRate();
			Rational increment = rate.multiply(amount);
			Rational newWeight = weight.add(increment);
			
			assert !( weight.getAbsoluteValue().getFloor().lessThan(newWeight.getAbsoluteValue().getFloor()) && !newWeight.getFractionalPart().equals(Rational.ZERO) )  
			: " updated node weight should not have crossed integer transition";
			
			node.setWeight(newWeight);
			updateAmount = updateAmount.add(increment);
			
			//update transition amounts
			updateTransitionAmounts(node);
		}
		return updateAmount;
	}
	
	// Calculates total tree update amount needed for reaching transition
	private Rational updateAmount(Node transNode) {
		
		Rational updateAmount = Rational.ZERO;
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
		Rational rateSum = Rational.ZERO;
		for (Node node : tree.vertexSet()) {
			Rational rate = node.getRate();
			rateSum = rateSum.add(rate);
		}
		return rateSum;
	}
	
	private void addLeafNode(Node parent) {
		Node child = new Node(this, parent);
		tree.addVertex(child);
		tree.addEdge(parent, child);
		nextId++;
	}
	
	private void removeLeafNode(Node node) {
		tree.removeVertex(node);
	}
	
	private void updateTree(Rational newRate) {
		
		//Update the rates and transition amounts
		for (Node node : tree.vertexSet()) {
			// update rates
			int depth = node.getDepth();
			Rational newNodeRate = newRate.power(depth);
			node.setRate(newNodeRate);
			
			//update transition amounts
			updateTransitionAmounts(node);
		}
	}
	
	private void updateTransitionAmounts(Node node) {
		
		Rational weight = node.getWeight();
		Rational rate = node.getRate();
		if ( weight.getFractionalPart().equals(Rational.ZERO) ) {
			node.setPosTransitionAmount(rate.getReciprocal());
			node.setNegTransitionAmount(rate.getReciprocal().negative());
		} else {
			node.setPosTransitionAmount(weight.getCeil().minus(weight).multiply(rate.getReciprocal()));
			node.setNegTransitionAmount(weight.getFloor().minus(weight).multiply(rate.getReciprocal()));
		}
	}
	
	public int getNextId() {
		return this.nextId;
	}
	
	public Rational getRate() {
		return this.rate;
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
