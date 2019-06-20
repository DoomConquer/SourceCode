
public class RemoveDuplicatesfromSortedArrayII {

	public int removeDuplicates(int[] nums) {
		if(nums.length == 0) return 0;
		int curr = 1;
		boolean flag = true;
		for(int i = 1; i < nums.length; i++){
			if(nums[i] > nums[curr - 1]){
				nums[curr++] = nums[i];
				flag = true;
			}else if(nums[i] == nums[curr - 1] && flag){
				nums[curr++] = nums[i];
				flag = false;
			}
		}
		return curr;
	}
	
	public static void main(String[] args) {
		RemoveDuplicatesfromSortedArrayII remove = new RemoveDuplicatesfromSortedArrayII();
		System.out.println(remove.removeDuplicates(new int[]{1,1,1,2,2,3}));
	}

}
