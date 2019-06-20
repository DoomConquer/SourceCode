
/**
 * @author li_zhe
 * ²Î¿¼leetcodeË¼Â·
 * (a * b) mod c = ((a mod c) * (b mod c)) mod c
 * (a ^ b) mod c = ((a mod b) ^ b) mod c
 * 
 * 
	Given input a = 2, b = {1,2,3,4}
	dp[0] = (2^4) mod 1337
	dp[1] = (2^34) mod 1337
	dp[2] = (2^234) mod 1337
	dp[3] = (2^1234) mod 1337
	
	DP base case :
	dp[0] = 2^4 mod 1337 = (2 mod 1337)^4 mod 1337 ==>(skill 2)
	
	I call (2 mod 1337) the factor. It can be reused in the next round DP building.
	
	dp[1] = (2^4 * 2^(3 * 10)) mod 1337 = ((2^4 mod 1337) * (2^(3*10) mod 1337)) mod 1337 ==>(skill 1)
	
	dp[1] = (dp[0] * (((2^10 mod 1337)^3) mod 1337)) mod 1337 ==>(skill 2)
	
	dp[1] = (dp[0] * ((((2 mod 1337)^10) mod 1337)^3)) mod 1337 ==>(skill 2)
	
	dp[1] = (dp[0] * (((factor^10) mod 1337)^3)) mod 1337
	
	finally, update the factor for next round reusing : factor = (factor^10) mod 1337
	
	By the above derivation, the DP recursion formula is :
	factor = (factor^10) mod 1337;
	dp[i] = (dp[i-1] * (factor^b[b.length-i-1]) % 1337) % 1337
 */
public class SuperPow {

	int mod = 1337;
	public int superPow(int a, int[] b) {
		int[] pow = new int[b.length];
		int factor = a % mod;
		pow[0] = powXN(a, b[b.length - 1]);
		for(int i = 1; i < b.length; i++){
			factor = powXN(factor, 10) % mod;
			pow[i] = (pow[i - 1] * (powXN(factor, b[b.length - 1 - i]) % mod)) % mod;
		}
		return pow[b.length - 1];
	}
	private int powXN(int a, int b) {
		return b == 0 ? 1 % mod : b == 1 ? a % mod : a % mod * powXN(a, b - 1) % mod;
	}
	
	public static void main(String[] args) {
		SuperPow superPow = new SuperPow();
		System.out.println(superPow.superPow(2, new int[]{1,0}));
		System.out.println(superPow.superPow(2, new int[]{1}));
		System.out.println(superPow.superPow(2, new int[]{0}));
		System.out.println(superPow.superPow(2147483647, new int[]{2,0,0}));
	}

}
