// one pass
public class SortColors {

	public void sortColors(int[] nums) {
		for(int left = 0, right = nums.length - 1, mid = 0; left < right && mid <= right;){
			if(nums[mid] == 0){
				nums[mid] = nums[left];
				nums[left] = 0;
				left++;
			}
			if(nums[mid] == 2){
				nums[mid] = nums[right];
				nums[right] = 2;
				right--;
				mid--;
			}
			mid++;
		}
	}
	
	public void sortColors1(int[] nums) {
		int left = 0, mid = 0, right = nums.length - 1;
		while(mid <= right){
			if(nums[mid] == 0){
				nums[mid] = nums[left];
				nums[left++] = 0;
			}else if(nums[mid] == 2){
				nums[mid--] = nums[right];
				nums[right--] = 2;
			}
			mid++;
		}
	}
	
	public static void main(String[] args) {
		SortColors colors = new SortColors();
		int[] nums = new int[]{2,0,2,1,1,0};
		colors.sortColors1(nums);
		for(int num : nums)
			System.out.print(num + "  ");
		
		System.out.println();
		colors.sortColors(nums);
		for(int num : nums)
			System.out.print(num + "  ");
	}

}
