// �ο�leetcode˼·��dp[i][j]��ʾ0-i������ָ��j��������ĺ͵����ֵ
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
        			for(int k = i; k >= j - 1; k--){ // kλ�ô����ָ��k��iΪһ�������飬0-k��Ϊj-1��������
        				long sum_k_i = sum[i] - sum[k] + nums[k];
        				if(sum_k_i > dp[i][j]) break; // ���k��i�ĺͳ������ֵ��������ǰ���㣬��ǰ�ָҪ��Ԫ�غ�ֻ�����
        				dp[i][j] = Math.min(dp[i][j], Math.max(sum_k_i, dp[k - 1][j - 1]));
        			}
        		}
        	}
        }
        return (int)dp[len - 1][m];
    }

	// ��һ�ֶ��ֲ��ҵķ�������ռ���0-sum(nums)��ʹ�ö��ֲ��ҷ�ʽ�ҵ����������Ľ�
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
