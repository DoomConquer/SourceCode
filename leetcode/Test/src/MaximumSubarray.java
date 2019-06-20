
public class MaximumSubarray {
	public int maxSubArray(int[] nums) {
		int sum = Integer.MIN_VALUE;
		int currMax = 0;
		for(int num : nums){
			currMax = Math.max(num, num + currMax);
			sum = Math.max(sum, currMax);
		}
		return sum;
	}
	
	public static void main(String[] args) {
		MaximumSubarray subarray = new MaximumSubarray();
		System.out.println(subarray.maxSubArray(new int[]{5,-2,1,-1}));
	}

}
