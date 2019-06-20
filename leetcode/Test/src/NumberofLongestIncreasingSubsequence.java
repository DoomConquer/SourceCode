import java.util.Arrays;

/**
 * @author li_zhe
 * 思路参考leetcode,自己想的差不多了,但是处理连续相同数字总有问题
 * DP思路, 两个数组,一个存储以i结尾的当前序列的最长子序列长度,一个存储以i结尾的序列的个数
 */
public class NumberofLongestIncreasingSubsequence {

	public int findNumberOfLIS(int[] nums) {
		if(nums == null || nums.length == 0) return 0;
		int[] longest = new int[nums.length];
		int[] count = new int[nums.length];
		Arrays.fill(longest, 1);
		Arrays.fill(count, 1);
		int maxLen = 0; int num = 0;
		for(int i = 0; i < nums.length; i++){
			for(int j = 0; j < i; j++){
				if(nums[i] > nums[j]){
					if(longest[i] == longest[j] + 1) count[i] += count[j];
					else if(longest[i] < longest[j] + 1){
						longest[i] = longest[j] + 1;
						count[i] = count[j];
					}
				}
			}
			if(maxLen == longest[i]) num += count[i];
			if(longest[i] > maxLen){
				maxLen = longest[i];
				num = count[i];
			}
		}
		return num;
	}
	
	public static void main(String[] args) {
		NumberofLongestIncreasingSubsequence longest = new NumberofLongestIncreasingSubsequence();
		System.out.println(longest.findNumberOfLIS(new int[]{1,3,5,4,7}));
		System.out.println(longest.findNumberOfLIS(new int[]{1,2,2,5,4,7}));
		System.out.println(longest.findNumberOfLIS(new int[]{2,2,2,2,2}));
		System.out.println(longest.findNumberOfLIS(new int[]{1,1,2,2,2}));
		System.out.println(longest.findNumberOfLIS(new int[]{1,2}));
	}

}
