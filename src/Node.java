public class Node implements Comparable<Node>{
	
	private int id;
	private Tree tree;
	private Node parent;
	private int depth;
	private Rational rate;
	private Rational weight;
	private Rational posTransitionAmount;
	private Rational negTransitionAmount;
	
	public Node(Tree tree) {
		this.id = 1;
		this.parent = null;
		this.depth = 0;
		this.rate = Rational.ONE;
		init();
	}
	
	public Node(Tree tree, Node parent) {
		this.id = tree.getNextId();
		this.parent = parent;
		this.depth = parent.getDepth() + 1;
		this.rate = parent.getRate().multiply(tree.getCurrentRate());
		init();
	}
	
	private void init() {
		this.weight = Rational.ZERO;
		this.posTransitionAmount = rate.getReciprocal();
		this.negTransitionAmount = rate.getReciprocal().negative();
	}

	public boolean isRoot() {
		boolean result = false;
		if ( this.id == 1 )
			result = true;
		return result;
	}
	
	// Returns null for root node 
	public Node getParent() {
		return parent;
	}
	
	public int getId() {
		return id;
	}

	public int getDepth() {
		return depth;
	}
	
	public Rational getRate() {
		return rate;
	}
	
	public void setRate(Rational rate) {
		this.rate = rate;
	}

	public Rational getWeight() {
		return weight;
	}
	
	public void setWeight(Rational weight) {
		this.weight = weight;
	}

	public Rational getPosTransitionAmount() {
		return posTransitionAmount;
	}

	public void setPosTransitionAmount(Rational posTransitionAmount) {
		this.posTransitionAmount = posTransitionAmount;
	}

	public Rational getNegTransitionAmount() {
		return negTransitionAmount;
	}

	public void setNegTransitionAmount(Rational negTransitionAmount) {
		this.negTransitionAmount = negTransitionAmount;
	}

	@Override
	public int compareTo(Node other) {

		if (id < other.id) {
			return -1;
		} else if (id > other.id) {
			return 1;
		} else {
			return 0;
		}
	}
}
