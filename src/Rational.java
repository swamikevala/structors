import java.math.BigInteger;

public class Rational implements Comparable<Rational>{

	private long numerator = 0;
	private long denominator = 1;
	
	public Rational(long numerator, long denominator) {
		
		if (denominator == 0) {
			throw new ArithmeticException("Rational cannot have a zero denominator");
		}
		
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public Rational() {
		
		if (denominator == 0) {
			throw new ArithmeticException("Rational cannot have a zero denominator");
		}
		
		this.numerator = 0;
		this.denominator = 0;
	}
	
	public Rational(Rational r) {
		
		this.numerator = r.getNumerator();
		this.denominator = r.getDenominator();
	}

	public long getNumerator() {
		return numerator;
	}

	public void setNumerator(long numerator) {
		this.numerator = numerator;
	}

	public long getDenominator() {
		return denominator;
	}

	public void setDenominator(long denominator) {
		this.denominator = denominator;
	}
	
	public Rational multiply(Rational other) {
		long a = numerator;
		long b = denominator;
		long c = other.getNumerator();
		long d = other.getDenominator();
		return new Rational(a*c, b*d).getReduced();
	}
	
	public Rational add(Rational other) {
		long a = numerator;
		long b = denominator;
		long c = other.getNumerator();
		long d = other.getDenominator();
		return new Rational(a*d  +  b*c, b*d).getReduced();
	}
	
	public Rational minus(Rational other) {
		long a = numerator;
		long b = denominator;
		long c = other.getNumerator();
		long d = other.getDenominator();
		return new Rational(Math.abs(a*d  -  b*c), b*d).getReduced();
	}
	
	public boolean isPositive() {
		boolean isPos = true;
		if (numerator < 0 && denominator > 0 || numerator > 0 && denominator < 0)
			isPos = false;
		return isPos;
	}
	
	public Rational getIntegerPart() {
		return new Rational(numerator / denominator, 1);
	}
	
	public Rational getFractionalPart() {
		long remainder = numerator % denominator;
		return new Rational(remainder, denominator);
	}
	
	public Rational getAbsoluteValue() {
		Rational abs = this.clone();
		Rational minusOne = new Rational(-1,1);
		if ( !this.isPositive() ) 
			abs = abs.multiply(minusOne);
		return abs;
	}
	
	public Rational getReciprocal() {
		if (numerator == 0) {
			throw new ArithmeticException("Rational cannot have a zero denominator");
		} 
		return new Rational(denominator, numerator);
	}
	
	public Rational getCeil() {
		Rational ceil = new Rational(0,1);
		long intPart = numerator / denominator;
		long fracPart = numerator % denominator;
		if ( fracPart == 0 || !this.isPositive() )
			ceil.setNumerator(intPart);
		else 
			ceil.setNumerator(intPart + 1);
		return ceil;
	}
	
	public Rational getFloor() {
		Rational floor = new Rational(0,1);
		long intPart = numerator / denominator;
		long fracPart = numerator % denominator;
		if ( fracPart == 0 || this.isPositive() )
			floor.setNumerator(intPart);
		else 
			floor.setNumerator(intPart - 1);
		return floor;
	}
	
	public Rational getReduced() {
		BigInteger n = BigInteger.valueOf(numerator);
		BigInteger d = BigInteger.valueOf(denominator);
		BigInteger gcd = n.gcd(d);
		return new Rational(n.divide(gcd).longValue(), d.divide(gcd).longValue());
	}
	
	public Rational clone() {
		return new Rational(numerator, denominator);
	}
	
	public String toString() {
		return numerator + "/" + denominator;
	}
	
	@Override
	public int compareTo(Rational other) {
		long ad = numerator * other.getDenominator();
		long bc = denominator * other.getNumerator();
		if (ad < bc) {
			return -1;
		} else if (ad > bc) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public boolean equals(Rational other) {
		if (this.compareTo(other) == 0) return true;
		else return false;
	}
}
