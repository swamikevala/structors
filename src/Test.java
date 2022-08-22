import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String args[]) throws InterruptedException {
		
		Rational r1 = new Rational(1,2);
		Rational m1 = new Rational(6,1);
		Rational r2 = new Rational(1,4);
		Rational m2 = new Rational(-6,1);
		Structor struct1 = new Structor(r1,m1);
		Structor struct2 = new Structor(r2,m2);
		//Tree tree = struct1.getTree();
		//tree.evolve(struct2);
		//Tree tree = struct2.getTree();
		//tree.evolve(struct1);
		List<Structor> sList = new ArrayList<Structor>();
		sList.add(struct1);
		sList.add(struct2);
		Tree tree = evolveLoop(sList, 4);
		tree.draw();
	}
	
	public static Tree evolveLoop(List<Structor> sList, int iterations) {
		Tree tree = new Tree(Rational.ONE);
		
		for (int i=0; i < iterations; i++) {
			for (Structor s : sList) {
				tree = tree.evolve(s);
			}
		}
		return tree;
	}
	
	
}
