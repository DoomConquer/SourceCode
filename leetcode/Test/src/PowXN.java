
public class PowXN {

	public double myPow(double x, int n) {
		if(n < 0) {
			x = 1 / x;
			n = -n;
		}
		double res = pow(x, n);
		return res;
	}
	private double pow(double x, int pow){
		if(pow == 0) return 1;
		if(pow == 1) return x;
		int mid = pow >>> 1;
		double left = pow(x, mid);
		if((pow & 1) == 1) return left * left * x;
		return left * left;
	}
	
	public static void main(String[] args) {
		PowXN pow = new PowXN();
		System.out.println(pow.myPow(2, -2));
		System.out.println(pow.myPow(2.10000, 3));
		System.out.println(pow.myPow(2.00000, 10));
		System.out.println(pow.myPow(2, 1000));
		System.out.println(pow.myPow(0.00001, 2147483647));
		System.out.println(pow.myPow(2.0, -2147483648));
	}

}
