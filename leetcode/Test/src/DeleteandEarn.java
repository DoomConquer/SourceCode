
/**
 * @author li_zhe
 * �ο�leetcode
 * DP˼·
 * �������ÿ�����һ����,ʱ�䲻������O(n)��ת��Ϊ����nums[i]��ֵ������ʱ�临�Ӷ�ΪO(w)
 * dp[i]��ʾֵ������i����Щ�������earn
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
