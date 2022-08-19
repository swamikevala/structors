public class Node implements Comparable<Node>{
	
	private int id;
	private Rational rate;
	private Rational weight;
	private Rational posTransitionAmount;
	private Rational negTransitionAmount;
	
	public Node(int id, Rational rate, Rational weight) {
		this.id = id;
		this.rate = rate;
		this.weight = weight;
		this.posTransitionAmount = rate.getReciprocal();
		this.negTransitionAmount = new Rational(0,1);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
