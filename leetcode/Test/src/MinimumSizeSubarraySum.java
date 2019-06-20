
public class MinimumSizeSubarraySum {

	public int minSubArrayLen(int s, int[] nums) {
		int min = Integer.MAX_VALUE;
		int sum = 0;
		for(int left = 0, right = 0; left < nums.length;){
			if(sum >= s){
				min = Math.min(min, right - left);
				sum -= nums[left];
				left++;
			}else{
				if(right < nums.length){
					sum += nums[right];
					right++;
				}else{
					break;
				}
			}
			if(right == nums.length && sum < s) break;
		}
		return min == Integer.MAX_VALUE ? 0 : min;
	}
	
	public static void main(String[] args) {
		MinimumSizeSubarraySum mini = new MinimumSizeSubarraySum();
		System.out.println(mini.minSubArrayLen(11, new int[]{1,2,3,4,5}));
	}

}
