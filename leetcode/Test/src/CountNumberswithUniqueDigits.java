public class CountNumberswithUniqueDigits {
	public int countNumbersWithUniqueDigits(int n) {
		if(n == 0) return 1;
        int[] dp = new int[n + 1];
        dp[1] = 10;
        int fi = 9;
        for(int i = 2; i <= n; i++){
    		if(i <= 10)
    			fi *= (9 - i + 2);
    		else
    			fi = 0;
        	dp[i] = dp[i - 1] + fi;
        }
        return dp[n];
    }

	public static void main(String[] args) {
		CountNumberswithUniqueDigits countNumberswithUniqueDigits = new CountNumberswithUniqueDigits();
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(0));
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(1));
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(2));
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(8));
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(10));
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(11));
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(12));
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(13));
		System.out.println(countNumberswithUniqueDigits.countNumbersWithUniqueDigits(20));
	}

}
