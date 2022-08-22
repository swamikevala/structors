import java.math.BigInteger;

public class Rational implements Comparable<Rational>{

	private long numerator = 0;
	private long denominator = 1;
	
	public static final Rational ZERO = new Rational(0,1);
	public static final Rational ONE = new Rational(1,1);
	public static final Rational MINUS_ONE = new Rational(-1,1);
	
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
		return new Rational(a*d  -  b*c, b*d).getReduced();
	}
	
	public Rational power(int power) {
		Rational result = ONE;
		if ( power == 0 )
			return result;
		
		for (int i=0; i < Math.abs(power); i++) {
			result = result.multiply(this);
		}
		if ( power < 0 )
			result = result.getReciprocal();
		
		return result;
	}
	
	public Rational negative() {
		return this.multiply(MINUS_ONE);
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
		if ( this.lessThan(ZERO) ) 
			abs = abs.multiply(MINUS_ONE);
		return abs;
	}
	
	public Rational getReciprocal() {
		if (numerator == 0) {
			throw new ArithmeticException("Rational cannot have a zero denominator");
		} 
		return new Rational(denominator, numerator);
	}
	
	public Rational getCeil() {
		Rational ceil = ZERO.clone();
		long intPart = numerator / denominator;
		long fracPart = numerator % denominator;
		if ( fracPart == 0 || this.lessThan(ZERO) )
			ceil.setNumerator(intPart);
		else 
			ceil.setNumerator(intPart + 1);
		return ceil;
	}
	
	public Rational getFloor() {
		Rational floor = ZERO.clone();
		long intPart = numerator / denominator;
		long fracPart = numerator % denominator;
		if ( fracPart == 0 || this.greaterThan(ZERO) )
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
	
	public boolean lessThan(Rational other) {
		boolean result = false;
		if ( this.compareTo(other) < 0 )
			result = true;
		return result;
	}
	
	public boolean lessThanOrEqual(Rational other) {
		boolean result = false;
		if ( this.compareTo(other) <= 0 )
			result = true;
		return result;
	}
	
	public boolean greaterThan(Rational other) {
		boolean result = false;
		if ( this.compareTo(other) > 0 )
			result = true;
		return result;
	}
	
	public boolean greaterThanOrEqual(Rational other) {
		boolean result = false;
		if ( this.compareTo(other) >= 0 )
			result = true;
		return result;
	}
	
	public Rational clone() {
		return new Rational(numerator, denominator);
	}
	
	public String toString() {
		long displayNum = numerator;
		long displayDen = denominator;
		if (numerator < 0 && denominator < 0) {
			displayNum = -numerator;
			displayDen = -denominator;
		}
		return displayNum + "/" + displayDen;
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
