
public class RotateArray {

	public void rotate(int[] nums, int k) {
		if(k >= nums.length) k %= nums.length;
		reverse(nums, 0, nums.length - k - 1);
		reverse(nums, nums.length - k, nums.length - 1);
		reverse(nums, 0, nums.length - 1);
	}
	private void reverse(int[] nums, int left, int right){
		while(left < right){
			int temp = nums[left];
			nums[left] = nums[right];
			nums[right] = temp;
			left++; right--;
		}
	}
	
	public static void main(String[] args) {
		RotateArray rotate = new RotateArray();
		int[] nums = new int[]{1,2,3};
		rotate.rotate(nums, 4);
		for(int num : nums)
			System.out.print(num + "  ");
	}

}
