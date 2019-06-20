
public class MaximumProductSubarray {

	public int maxProduct(int[] nums) {
		int currMaxProduct = 1;
		int currMinProduct = 1;
		int maxProduct = nums[0];
		for(int num : nums){
			int temp = currMaxProduct;
			currMaxProduct = Math.max(Math.max(currMaxProduct * num, currMinProduct * num), num);
			currMinProduct = Math.min(Math.min(currMinProduct * num, temp * num), num);
			maxProduct = Math.max(currMaxProduct, maxProduct);
		}
		return maxProduct;
	}
	
	public static void main(String[] args) {
		MaximumProductSubarray subarray = new MaximumProductSubarray();
		System.out.println(subarray.maxProduct(new int[]{0,1,0,7}));
	}

}
