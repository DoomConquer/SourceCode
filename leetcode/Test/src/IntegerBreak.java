public class IntegerBreak {
	public int integerBreak(int n) {
        int[] dp = new int[n + 1];
        dp[0] = dp[1] = 1;
        for(int i = 2; i <= n; i++){
        	for(int j = 1; i - j > 0; j++)
        		dp[i] = Math.max(dp[i], Math.max(dp[j] * (i - j), j * (i - j)));
        }
        return dp[n];
    }

	public static void main(String[] args) {
		IntegerBreak integerBreak = new IntegerBreak();
		System.out.println(integerBreak.integerBreak(2));
		System.out.println(integerBreak.integerBreak(3));
		System.out.println(integerBreak.integerBreak(4));
		System.out.println(integerBreak.integerBreak(10));
		System.out.println(integerBreak.integerBreak(58));
	}
	
}
