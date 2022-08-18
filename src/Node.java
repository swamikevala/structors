public class Node {
	
	private int id;
	private Rational rate;
	private Rational weight;
	
	public Node(int id, Rational rate, Rational weight) {
		this.id = id;
		this.rate = rate;
		this.weight = weight;
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

}
