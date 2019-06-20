
public class SearchInsertPosition {

	public int searchInsert(int[] nums, int target) {
		int left = 0, right = nums.length - 1;
		while(left <= right){
			int mid = (left + right) >>> 1;
			if(nums[mid] == target) return mid;
			else if(nums[mid] > target) right = mid - 1;
			else left = mid + 1;
		}
		return left;
	}
	
	public static void main(String[] args) {
		SearchInsertPosition search = new SearchInsertPosition();
		System.out.println(search.searchInsert(new int[]{1, 3, 5, 6}, 5));
	}

}
