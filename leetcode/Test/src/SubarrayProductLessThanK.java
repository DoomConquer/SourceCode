
public class SubarrayProductLessThanK {

	public int numSubarrayProductLessThanK(int[] nums, int k) {
		int sum = 0;
		int product = 1;
		for(int left = 0, right = 0; right < nums.length; right++){
			product *= nums[right];
			while(left <= right && product >= k) product /= nums[left++];
			sum += right - left + 1;
		}
		return sum;
	}
	
	public static void main(String[] args) {
		SubarrayProductLessThanK sub = new SubarrayProductLessThanK();
		System.out.println(sub.numSubarrayProductLessThanK(new int[]{10, 5, 2, 6}, 1));
	}
}
