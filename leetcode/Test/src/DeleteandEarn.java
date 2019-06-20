
/**
 * @author li_zhe
 * 参考leetcode
 * DP思路
 * 如果根据每次添加一个数,时间不能做到O(n)，转化为根据nums[i]的值，这样时间复杂度为O(w)
 * dp[i]表示值不大于i的这些数的最大earn
 */
public class DeleteandEarn {

	public int deleteAndEarn(int[] nums) {
		if(nums == null || nums.length == 0) return 0;
		int MAX_SIZE = 10001;
		int[] count = new int[MAX_SIZE];
		for(int num : nums) count[num]++;
		int[] dp = new int[MAX_SIZE];
		dp[1] = count[1];
		for(int i = 2; i < MAX_SIZE; i++){
			dp[i] = Math.max(dp[i - 1], dp[i - 2] + i * count[i]);
		}
		return dp[MAX_SIZE - 1];
	}
	
	public static void main(String[] args) {
		DeleteandEarn delete = new DeleteandEarn();
		System.out.println(delete.deleteAndEarn(new int[]{1, 1, 1}));
		System.out.println(delete.deleteAndEarn(new int[]{3, 4, 2}));
		System.out.println(delete.deleteAndEarn(new int[]{2, 2, 3, 3, 3, 4}));
	}

}
