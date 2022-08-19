public class Test {

	public static void main(String args[]) throws InterruptedException {
		
		Rational r = new Rational(1,2);
		Rational m = new Rational(3,1);
		Structor struct = new Structor(r,m);
		Tree tree = struct.getTree();
		tree.draw();
	}
	
	
}
