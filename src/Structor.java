public class Structor {

	private Rational magnitude;
	private Rational rate;
	private Tree tree;

	public Structor(Rational rate, Rational magnitude) {
		this.rate = rate;
		this.magnitude = magnitude;
		tree = new Tree(rate);
		//tree.evolve(magnitude);
	}
	
	public String toString() {
		return "(" + rate.toString() + "," + magnitude.toString() + ")";
	}

	public Rational getMagnitude() {
		return magnitude;
	}

	public Rational getRate() {
		return rate;
	}

	public Tree getTree() {
		return tree;
	}

}
