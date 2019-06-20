
public class FactorialTrailingZeroes {

	public int trailingZeroes(int n) {
		return n == 0 ? 0 : n / 5 + trailingZeroes(n / 5);
	}
	
	public static void main(String[] args) {
		FactorialTrailingZeroes factor = new FactorialTrailingZeroes();
		System.out.println(factor.trailingZeroes(100));
		System.out.println(factor.trailingZeroes(6));
	}

}
