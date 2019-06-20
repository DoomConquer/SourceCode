import java.util.Arrays;

/**
 * @author li_zhe
 * 参考leetcode  backtracking
 * 自己根据coin change想的DP思路
 * 
 */
public class PartitionEqualSubsetSum {

	public boolean canPartition1(int[] nums) {
		if(nums == null || nums.length == 0) return false;
		int sum = 0;
		for(int num : nums) sum += num;
		if((sum & 1) != 0) return false;
		return partion(nums, nums.length - 1, sum >> 1);
	}
	private boolean partion(int[] nums, int i, int sum) {
        if(sum == 0) return true;
        else if(i < 0 || sum < 0 || sum < nums[i]) {
            return false;
        }
        else {
            return partion(nums, i - 1, sum - nums[i]) || partion(nums, i - 1, sum);
        }
    }
	
	public boolean canPartition(int[] nums) {
		if(nums == null || nums.length == 0) return false;
		int sum = 0;
		for(int num : nums) sum += num;
		if((sum & 1) != 0) return false;
		int half = sum >> 1;
		int[][] dp = new int[nums.length + 1][half + 1];
		for(int i = 1; i < nums.length; i++){
			for(int j = 1; j <= half; j++){
				if(j >= nums[i - 1]){
					dp[i][j] = Math.max(dp[i - 1][j], dp[i -1][j - nums[i - 1]] + nums[i - 1]);
				}
				if(dp[i][j] == half) return true;
			}
		}
		return false;
	}
	
	// 另一种DP思路
	public boolean canPartition2(int[] nums) {
        Arrays.sort(nums);
        int sum = 0;
        for(int num : nums) sum += num;
        if((sum & 1) != 0) return false;
        int target = sum >> 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;
      
        for(int i = 0; i < nums.length; ++i){
            for(int j = target; j >= nums[i]; --j){
                if(dp[j - nums[i]]){
                    dp[j] = true;
                }
            }
        }
        
        return dp[target];
    }
	
	public static void main(String[] args) {
		PartitionEqualSubsetSum partion = new PartitionEqualSubsetSum();
		System.out.println(partion.canPartition(new int[]{1, 5, 11, 5}));
		System.out.println(partion.canPartition(new int[]{11, 5, 5, 1}));
		System.out.println(partion.canPartition(new int[]{1, 2, 3, 5}));
	}

}
