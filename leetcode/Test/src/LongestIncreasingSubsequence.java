import java.util.Arrays;

/**
 * @author li_zhe
 * 思路参考leetcode，O（nlgn）的解法不太直观（DP + 二分查找）。
 * 思路：维护一个排序好的数组，每次更新比末尾的大就加在后面，否则二分查找应该插入的位置替换原来的元素
 */
public class LongestIncreasingSubsequence {

	public int lengthOfLIS(int[] nums) {
		if(nums == null || nums.length == 0) return 0;
		int[] sorted = new int[nums.length];
		sorted[0] = nums[0];
		int index = 0;
		for(int i = 1; i < nums.length; i++){
			if(nums[i] > sorted[index]){
				index++;
				sorted[index] = nums[i];
			}else{
				int pos = Arrays.binarySearch(sorted, 0, index, nums[i]);
				if(pos < 0){
					sorted[- pos - 1] = nums[i];
				}
			}
		}
		return index + 1;
	}
	
	public static void main(String[] args) {
		LongestIncreasingSubsequence longest = new LongestIncreasingSubsequence();
		System.out.println(longest.lengthOfLIS(new int[]{10,9,2,5,3,7,101,18}));
		System.out.println(longest.lengthOfLIS(new int[]{18,9,2,5,3,7,1,3}));
		System.out.println(longest.lengthOfLIS(new int[]{10,9,2,5,3,4}));
	}

}
