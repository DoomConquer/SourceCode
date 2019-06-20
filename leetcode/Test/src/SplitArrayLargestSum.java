// 参考leetcode思路，dp[i][j]表示0-i的数组分割成j份子数组的和的最大值
public class SplitArrayLargestSum {
	public int splitArray(int[] nums, int m) {
        int len = nums.length;
        long[][] dp = new long[len][m + 1];
        for(int i = 0; i < len; i++)
        	java.util.Arrays.fill(dp[i], Long.MAX_VALUE);
        long[] sum = new long[len];
        sum[0] = nums[0];
        for(int i = 1; i < len; i++){
        	sum[i] = sum [i - 1] + nums[i];
        }
        for(int i = 0; i < len; i++){
        	int maxSub = Math.min(m, i + 1);
        	for(int j = 1; j <= maxSub; j++){
        		if(j == 1) dp[i][j] = sum[i];
        		else{
        			for(int k = i; k >= j - 1; k--){ // k位置处做分割，从k到i为一个子数组，0-k分为j-1个子数组
        				long sum_k_i = sum[i] - sum[k] + nums[k];
        				if(sum_k_i > dp[i][j]) break; // 如果k到i的和超过最大值，不用往前计算，往前分割还要加元素和只会更大
        				dp[i][j] = Math.min(dp[i][j], Math.max(sum_k_i, dp[k - 1][j - 1]));
        			}
        		}
        	}
        }
        return (int)dp[len - 1][m];
    }

	// 另一种二分查找的方法，解空间是0-sum(nums)，使用二分查找方式找到满足条件的解
	public int splitArray2(int[] nums, int m) {
		long sum = nums[0];
		int max = nums[0];
		int len = nums.length;
		for(int i = 1; i < len; i++){
			max = Math.max(max, nums[i]);
			sum += nums[i];
		}
		long left = max, right = sum;
		while(left <= right){
			long mid = (left + right) / 2;
			if(valid(nums, mid, m)) right = mid - 1;
			else left = mid + 1;
		}
		return (int)left;
	}
	private boolean valid(int[] nums, long target, int m){
		long sum = 0;
		for(int num : nums){
			if(num > target) return false;
			if(sum + num <= target) sum += num;
			else{
				sum = num;
				m--;
			}
		}
		m--;
		return m >= 0 ? true : false;
	}
	
	public static void main(String[] args) {
		SplitArrayLargestSum splitArrayLargestSum = new SplitArrayLargestSum();
		System.out.println(splitArrayLargestSum.splitArray(new int[]{7,2,5,10,8}, 2));
		System.out.println(splitArrayLargestSum.splitArray2(new int[]{7,2,5,10,8}, 2));
	}

}
