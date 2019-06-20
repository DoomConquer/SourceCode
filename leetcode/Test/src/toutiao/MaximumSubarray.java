package toutiao;

public class MaximumSubarray {

    public int maxSubArray(int[] nums) {
        int max = nums[0], currSum = nums[0];
        for(int i = 1; i < nums.length; i++){
        	if(currSum + nums[i] > nums[i]) currSum += nums[i];
        	else currSum = nums[i];
        	max = Math.max(max, currSum);
        }
        return max;
    }
    
    // 分治法
    class Half{
    	// sum为half段数组所有元素的和，maxSum为half段数组的最大连续和，leftSum为half段数组以最左边元素开始的最大和，rightSum为half段以最右边元素结束的最大和
    	int sum, maxSum, leftSum, rightSum;
    	public Half(int sum, int maxSum, int leftSum, int rightSum){
    		this.sum = sum; this.maxSum = maxSum; this.leftSum = leftSum; this.rightSum = rightSum;
    	}
    }
    public int maxSubArray1(int[] nums) {
    	return maxSubArray(nums, 0, nums.length - 1).maxSum;
    }
    private Half maxSubArray(int[] nums, int left, int right){
    	if(left == right) return new Half(nums[left], nums[left], nums[left], nums[left]);
    	int mid = (left + right) >> 1;
    	Half leftHalf = maxSubArray(nums, left, mid);
    	Half rightHalf = maxSubArray(nums, mid + 1, right);
    	int maxSum = Math.max(Math.max(leftHalf.maxSum, rightHalf.maxSum), leftHalf.rightSum + rightHalf.leftSum);
    	int leftSum = Math.max(leftHalf.leftSum, leftHalf.sum + rightHalf.leftSum);
    	int rightSum = Math.max(rightHalf.rightSum, leftHalf.rightSum + rightHalf.sum);
    	int sum = leftHalf.sum + rightHalf.sum;
    	return new Half(sum, maxSum, leftSum, rightSum);
    }
    
	public static void main(String[] args) {
		MaximumSubarray maximumSubarray = new MaximumSubarray();
		System.out.println(maximumSubarray.maxSubArray(new int[]{-2,1,-3,4,-1,2,1,-5,4}));
		System.out.println(maximumSubarray.maxSubArray(new int[]{-2,-1}));
		System.out.println(maximumSubarray.maxSubArray(new int[]{-2}));
		System.out.println(maximumSubarray.maxSubArray(new int[]{1,2,3}));
		System.out.println(maximumSubarray.maxSubArray(new int[]{1,2,3,-5,10}));
		System.out.println(maximumSubarray.maxSubArray(new int[]{1,2,3,-7,10}));
		
		System.out.println(maximumSubarray.maxSubArray1(new int[]{-2,1,-3,4,-1,2,1,-5,4}));
		System.out.println(maximumSubarray.maxSubArray1(new int[]{-2,-1}));
		System.out.println(maximumSubarray.maxSubArray1(new int[]{-2}));
		System.out.println(maximumSubarray.maxSubArray1(new int[]{1,2,3}));
		System.out.println(maximumSubarray.maxSubArray1(new int[]{1,2,3,-5,10}));
		System.out.println(maximumSubarray.maxSubArray1(new int[]{1,2,3,-7,10}));
	}

}
