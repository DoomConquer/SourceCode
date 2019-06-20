public class LongestArithmeticSequence {

    public int longestArithSeqLength(int[] A) {
    	int n = A.length;
        int[][] dp = new int[20001][n];
        int max = 0;
        for(int i = 0; i < n; i++){
        	for(int j = 0; j < i; j++){
        		dp[A[i] - A[j] + 10000][i] = Math.max(dp[A[i] - A[j] + 10000][i], dp[A[i] - A[j] + 10000][j] + 1);
        		max = Math.max(max, dp[A[i] - A[j] + 10000][i]);
        	}
        }
        return max + 1;
    }
    
	public static void main(String[] args) {
		LongestArithmeticSequence longestArithmeticSequence = new LongestArithmeticSequence();
		System.out.println(longestArithmeticSequence.longestArithSeqLength(new int[]{3,6}));
		System.out.println(longestArithmeticSequence.longestArithSeqLength(new int[]{3,6,9,12}));
		System.out.println(longestArithmeticSequence.longestArithSeqLength(new int[]{9,4,7,2,10}));
		System.out.println(longestArithmeticSequence.longestArithSeqLength(new int[]{20,1,15,3,10,5,8}));
		System.out.println(longestArithmeticSequence.longestArithSeqLength(new int[]{83,20,17,43,52,78,68,45}));
		System.out.println(longestArithmeticSequence.longestArithSeqLength(new int[]{24,13,1,100,0,94,3,0,3}));
	}

}
