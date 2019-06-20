// dp思路，dp[k][i]表示前i个元素分成k组的最大平均和
public class LargestSumofAverages {

    public double largestSumOfAverages(int[] A, int K) {
    	int n = A.length;
        double[] sum = new double[n + 1];
        for(int i = 1; i <= n; i++) sum[i] = sum[i - 1] + A[i - 1];
        double[][] dp = new double[K + 1][n + 1];
        dp[1][1] = A[0];
        for(int k = 1; k <= K; k++){
	        for(int i = 2; i <= n; i++){
	        	if(k == 1){ dp[k][i] = sum[i] / i; continue; }
	        	for(int j = k - 1; j > 0 && j <= i - 1; j++){
	        		dp[k][i] = Math.max(dp[k][i], dp[k - 1][j] + (sum[i] - sum[j]) / (i - j));
	        	}
	        }
        }
        return dp[K][n];
    }
    
	public static void main(String[] args) {
		LargestSumofAverages largestSumofAverages = new LargestSumofAverages();
		System.out.println(largestSumofAverages.largestSumOfAverages(new int[]{9,1,2,3,9}, 1));
		System.out.println(largestSumofAverages.largestSumOfAverages(new int[]{9,1,2,3,9}, 3));
		System.out.println(largestSumofAverages.largestSumOfAverages(new int[]{9,1,2,3,9}, 5));
	}

}
