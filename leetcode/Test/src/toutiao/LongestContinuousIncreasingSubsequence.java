package toutiao;

public class LongestContinuousIncreasingSubsequence {

    public int findLengthOfLCIS(int[] nums) {
    	if(nums.length == 0) return 0;
        int max = 1, curr = 1;
        for(int i = 1; i < nums.length; i++){
        	if(nums[i] > nums[i - 1]) curr++;
        	else curr = 1;
        	if(curr > max) max = curr;
        }
        return max;
    }
    
	public static void main(String[] args) {
		LongestContinuousIncreasingSubsequence longestContinuousIncreasingSubsequence = new LongestContinuousIncreasingSubsequence();
		System.out.println(longestContinuousIncreasingSubsequence.findLengthOfLCIS(new int[]{1,3,5,4,7}));
		System.out.println(longestContinuousIncreasingSubsequence.findLengthOfLCIS(new int[]{1,3,5,7,9}));
		System.out.println(longestContinuousIncreasingSubsequence.findLengthOfLCIS(new int[]{2,2,2,2,2}));
		System.out.println(longestContinuousIncreasingSubsequence.findLengthOfLCIS(new int[]{2}));
	}

}
