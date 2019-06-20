
public class RemoveElement {

	public int removeElement(int[] nums, int val) {
		int left = 0;
		for(int right = nums.length - 1; left <= right;){
			if(nums[left] == val && nums[right] != val) nums[left++] = nums[right--];
			else if(nums[left] != val) left++;
			else if(nums[right] == val) right--;
		}
		return left;
	}
	
	public static void main(String[] args) {
		RemoveElement remove = new RemoveElement();
		System.out.println(remove.removeElement(new int[]{1,2,3,4,5,5,5,5}, 5));
	}

}
