
public class FindPivotIndex {

	public int pivotIndex(int[] nums) {
		if(nums == null || nums.length == 0) return -1;
		int len = nums.length;
		int[] left = new int[len];
		int[] right = new int[len];
		int leftSum = 0, rightSum = 0;
		for(int i = 0; i < len; i++){
			leftSum += nums[i];
			left[i] = leftSum;
			rightSum += nums[len - 1 -i];
			right[len - 1 - i] = rightSum;
		}
		for(int i = 0; i < len; i++)
			if(left[i] == right[i]) return i;
		return -1;
	}
	
	public static void main(String[] args) {
		FindPivotIndex find = new FindPivotIndex();
		System.out.println(find.pivotIndex(new int[]{1, 7, 3, 6, 5, 6}));
		System.out.println(find.pivotIndex(new int[]{1, 2, 3}));
	}

}
