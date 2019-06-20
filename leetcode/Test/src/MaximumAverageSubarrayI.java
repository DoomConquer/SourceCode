public class MaximumAverageSubarrayI {
	public double findMaxAverage(int[] nums, int k) {
        int sum = 0;
        for(int i = 0; i < k; i++){
        	sum += nums[i];
        }
        int max = sum;
        for(int i = k; i < nums.length; i++){
        	sum = sum - nums[i - k] + nums[i];
        	max = Math.max(max, sum);
        }
        return max * 1.0 / k;
    }
	
	public static void main(String[] args) {
		MaximumAverageSubarrayI maximumAverageSubarrayI = new MaximumAverageSubarrayI();
		System.out.println(maximumAverageSubarrayI.findMaxAverage(new int[]{1,12,-5,-6,50,3}, 4));
		System.out.println(maximumAverageSubarrayI.findMaxAverage(new int[]{-1,-1,-2}, 2));
		System.out.println(maximumAverageSubarrayI.findMaxAverage(new int[]{-1}, 1));
	}

}
