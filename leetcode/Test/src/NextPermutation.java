
public class NextPermutation {

	public void nextPermutation(int[] nums) {
		if(nums == null || nums.length == 0)
			return;
		int start = nums.length - 1;
		for(; start > 0; start--){
			if(nums[start] > nums[start - 1]){
				break;
			}
		}
		if(start > 0){
			int next = nums.length - 1;
			for(; next >= start; next--){
				if(nums[start - 1] < nums[next]){
					break;
				}
			}
			swap(nums, start - 1, next);
		}
		reverse(nums, start);
	}
	private void swap(int[] nums, int i, int j){
		int temp = nums[i];
		nums[i] = nums[j];
		nums[j] = temp;
	}
	private void reverse(int[] nums, int i){
		for(int end = nums.length - 1; i < end; i++, end--){
			int temp = nums[i];
			nums[i] = nums[end];
			nums[end] = temp;
		}
	}
	
	public static void main(String[] args) {
		NextPermutation permutation = new NextPermutation();
		permutation.nextPermutation(new int[]{3,2,1});
	}

}
