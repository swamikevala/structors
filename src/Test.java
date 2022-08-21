public class Test {

	public static void main(String args[]) throws InterruptedException {
		
		Rational r1 = new Rational(1,2);
		Rational m1 = new Rational(5,2);
		Rational r2 = new Rational(2,1);
		Rational m2 = new Rational(-5,2);
		Structor struct1 = new Structor(r1,m1);
		Structor struct2 = new Structor(r2,m2);
		//Tree tree = struct1.getTree();
		//tree.evolve(struct2);
		Tree tree = struct2.getTree();
		tree.evolve(struct1);
		tree.draw();
	}
	
	
}
