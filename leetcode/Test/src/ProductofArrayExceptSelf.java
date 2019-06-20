
public class ProductofArrayExceptSelf {

	public int[] productExceptSelf(int[] nums) {
		int[] res = new int[nums.length];
		res[0] = 1;
		for(int i = 1; i < nums.length; i++){
			res[i] = res[i - 1] * nums[i - 1];
		}
		int rightProduct = 1;
		for(int i = nums.length - 2; i >= 0; i--){
			rightProduct *= nums[i + 1];
			res[i] *= rightProduct;
		}
		return res;
	}
	
	public static void main(String[] args) {
		ProductofArrayExceptSelf product = new ProductofArrayExceptSelf();
		int[] nums = product.productExceptSelf(new int[]{1,2,3,4});
		for(int num : nums)
			System.out.print(num + " ");
	}

}
