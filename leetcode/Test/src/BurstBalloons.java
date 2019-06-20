/**
 * @author li_zhe
 * 思路参考leetcode
 * DP O(n^3)
 * 自底向上求解，dp[i][j]表示i到j之间的最大coins，那么求解dp[i][j]可以假设i-j之间每个气球都最后burst的最大值
 * 所以状态转移公式为：dp[i][j] = max(dp[i][j], dp[i][k - 1] + nums[i - 1] * nums[k] * nums[j + 1] + dp[k + 1][j]), i<=k<=j
 */
public class BurstBalloons {

	public int maxCoins(int[] nums) {
		if(nums == null || nums.length == 0) return 0;
		int n = nums.length;
		int[] newNums = new int[n + 2];
		System.arraycopy(nums, 0, newNums, 1, n);
		newNums[0] = newNums[n + 1] = 1;
		int[][] dp = new int[n + 2][n + 2];
		for(int i = 0; i <= n; i++){
			for(int j = 1; i + j <= n; j++){
				for(int k = j; k <= i + j; k++){
					dp[j][i + j] = Math.max(dp[j][i + j], 
							dp[j][k - 1] + newNums[j - 1] * newNums[k] * newNums[i + j + 1] + dp[k + 1][i + j]);
				}
			}
		}
		return dp[1][n];
	}
	
	// 分治法 + memorization
	public int maxCoins1(int[] nums) {
		if(nums == null || nums.length == 0) return 0;
		int n = nums.length;
		int[] newNums = new int[n + 2];
		System.arraycopy(nums, 0, newNums, 1, n);
		newNums[0] = newNums[n + 1] = 1;
		int[][] mem = new int[n + 2][n + 2];
		return intervalMax(newNums, 1, n, mem);
	}
	private int intervalMax(int[] nums, int left, int right, int[][]mem){
		if(mem[left][right] > 0) return mem[left][right];
		for(int curr = left; curr <= right; curr++){
			mem[left][right] = Math.max(mem[left][right], 
					intervalMax(nums, left, curr - 1, mem) 
					+ nums[left - 1] * nums[curr] * nums[right + 1] 
					+ intervalMax(nums, curr + 1, right, mem));
		}
		return mem[left][right];
	}
	
	
	public static void main(String[] args) {
		BurstBalloons balloons = new BurstBalloons();
		System.out.println(balloons.maxCoins1(new int[]{3,1,5,8}));
		System.out.println(balloons.maxCoins1(new int[]{3,1,5}));
	}

}
