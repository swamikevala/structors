import java.awt.Dimension;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
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
	private Rational currentRate; // current rate (tree rate can change for compound structors) 
	private Rational currentMagnitude; // total amount (magnitude) tree needs to evolve by
	private Graph<Node, DefaultEdge> tree;
	private Node root;
	
	public Tree(Rational rate) {
		this.currentRate = rate;
		this.currentMagnitude = Rational.ZERO;
		tree = new DefaultUndirectedGraph<Node, DefaultEdge>(DefaultEdge.class);
		root = new Node(this);
		tree.addVertex(root);
		nextId = 2;
	}

	public Tree evolve(Rational amount) {
		return evolve(this.currentRate, amount);
	}
	
	public Tree evolve(Structor structor) {
		return evolve(structor.getRate(), structor.getMagnitude());
	}
	
	public Tree evolve(Rational newRate, Rational amount){
		
		if ( !currentRate.equals(newRate) )
			updateTree(newRate);
		
		this.currentMagnitude = amount;
		SortedSet<Node> transNodes = new TreeSet<Node>();
		Rational amountRemainder = amount;
		List<Node> nodesForRemoval = new ArrayList<Node>();
		List<Node> createdNodes = new ArrayList<Node>();
		Rational updateAmount;
		Rational transAmount;
		
		do {
			transNodes = getTransitionNodes(amountRemainder);

			if ( !transNodes.isEmpty() && updateAmount(transNodes.first(), amount).getAbsoluteValue().lessThanOrEqual(amountRemainder.getAbsoluteValue()) ) {
				
				// update nodes and add nodes at growth transitions
				Node firstTransNode = transNodes.first();
				transAmount = isGrowing() ? firstTransNode.getPosTransitionAmount() : firstTransNode.getNegTransitionAmount();
				updateAmount = updateNodes(transAmount);
				
				// Handle transition node addition/removal
				for ( Node transNode : transNodes ) {
					
					assert transNode.getWeight().getFractionalPart().equals(Rational.ZERO) : " New parent node weight must be an integer multiple";
				
					if ( !transNode.isRoot() && tree.edgesOf(transNode).size() == 1 && transNode.getWeight().equals(Rational.ZERO) ) {
						removeNode(transNode);
					} else {
						createdNodes.add(addNode(transNode));
					}
				}	
				
				// Mark (non-newly created) nodes for removal (if any have reached zero simultaneously)
				for (Node node : tree.vertexSet()) {
					if ( !createdNodes.contains(node) && !node.isRoot() && tree.edgesOf(node).size() == 1 && node.getWeight().equals(Rational.ZERO) ) {
						nodesForRemoval.add(node);
					}
				}
				
			// Not enough remaining amount to reach a transition
			} else {
				
				// Split amount between nodes, proportionate to growth rate
				Rational unitAmount = amountRemainder.multiply(getRateSum().getReciprocal());
				updateAmount = updateNodes(unitAmount);
				
				assert updateAmount.equals(amountRemainder) : " Non-transition update amount should be the same as the full amount"; 
			}
			
			// Remove zero leaf nodes
			for (Node node : nodesForRemoval) {
				removeNode(node);
			}
			nodesForRemoval.clear();
			createdNodes.clear();
			amountRemainder = amountRemainder.minus(updateAmount);
				
		} while ( !amountRemainder.equals(Rational.ZERO) );
		
		// finally remove any remaining zero leaf nodes
		for (Node node : tree.vertexSet()) {
			if ( !node.isRoot() && tree.edgesOf(node).size() == 1 && node.getWeight().equals(Rational.ZERO) ) 
				removeNode(node);
		}
		
		return this;
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
	private Rational updateAmount(Node transNode, Rational amount) {
		
		Rational updateAmount = Rational.ZERO;
		Rational transNodeAmount = (amount.greaterThan(Rational.ZERO)) ? transNode.getPosTransitionAmount() : transNode.getNegTransitionAmount();

		for (Node node : tree.vertexSet()) {
			// add scaled amounts
			Rational rate = node.getRate();
			Rational increment = rate.multiply(transNodeAmount);
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
	
	private Node addNode(Node parent) {
		Node child = new Node(this, parent);
		tree.addVertex(child);
		tree.addEdge(parent, child);
		nextId++;
		System.out.println("add: " + child.getId() + " " + child.getDepth() + " " + parent.getId());
		return child;
	}
	
	private void removeNode(Node node) {
		
		tree.removeVertex(node);
		System.out.println("rem: " + node.getId() + " " + node.getDepth() + " " + node.getParent().getId());
	}
	
	private void updateTree(Rational newRate) {
		
		this.currentRate = newRate;
		
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
	
	private boolean isGrowing() {
		boolean result = false;
		if ( this.currentMagnitude.greaterThan(Rational.ZERO) )
			result = true;
		return result;
	}
	
	public int getNextId() {
		return this.nextId;
	}
	
	public Rational getCurrentRate() {
		return this.currentRate;
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
