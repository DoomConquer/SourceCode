
public class LongestContinuousIncreasingSubsequence {

	public int findLengthOfLCIS(int[] nums) {
		if(nums == null || nums.length == 0) return 0;
		int max = 1, currLen = 1;
		for(int i = 1; i < nums.length; i++){
			if(nums[i] > nums[i - 1]) currLen++;
			else currLen = 1;
			max = Math.max(max, currLen);
		}
		return max;
	}
	
	public static void main(String[] args) {
		LongestContinuousIncreasingSubsequence longest = new LongestContinuousIncreasingSubsequence();
		System.out.println(longest.findLengthOfLCIS(new int[]{1,3,5,4,7}));
		System.out.println(longest.findLengthOfLCIS(new int[]{2,2,2,2,2}));
		System.out.println(longest.findLengthOfLCIS(new int[]{1,3,5,6,7}));
		System.out.println(longest.findLengthOfLCIS(new int[]{1}));
		System.out.println(longest.findLengthOfLCIS(new int[]{1,3,5,4,2,3,4,5}));
	}

}
