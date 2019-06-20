
public class RemoveDuplicatesfromSortedArray {

	public int removeDuplicates(int[] nums) {
		if(nums == null || nums.length == 0) return 0;
		int curr = 1;
		int len = nums.length;
		int last = nums[0];
		for(int next = 0; curr < len && next < len; next++){
			if(nums[next] > last){
				nums[curr++] = nums[next];
				last = nums[next];
			}
		}
		return curr;
	}
	
	public static void main(String[] args) {
		RemoveDuplicatesfromSortedArray remove = new RemoveDuplicatesfromSortedArray();
		System.out.println(remove.removeDuplicates(new int[]{1,1,1,2,2,2,3,3,4}));
	}

}
