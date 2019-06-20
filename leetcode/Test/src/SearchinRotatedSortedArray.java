
public class SearchinRotatedSortedArray {

	public int search(int[] nums, int target) {
		if(nums == null || nums.length == 0) return -1;
		return find(0, nums.length - 1, nums, target);
	}
	private int find(int left, int right, int[] nums, int target){
		if(left > right) return -1;
		int mid = (left + right) >>> 1;
		if(nums[mid] == target) return mid;
		if(nums[mid] < nums[left]){
			if(nums[mid] < target && nums[right] >= target) return find(mid + 1, right, nums, target);
			else return find(left, mid - 1, nums, target);
		}else if(nums[mid] > nums[right]){
			if(nums[mid] > target && nums[left] <= target) return find(left, mid - 1, nums, target);
			else return find(mid + 1, right, nums, target);
		}else{
			if(nums[mid] > target) return find(left, mid - 1, nums, target);
			else return find(mid + 1, right, nums, target);
		}
	}
	
	public static void main(String[] args) {
		SearchinRotatedSortedArray search = new SearchinRotatedSortedArray();
		System.out.println(search.search(new int[]{4,5,6,7,0,1,2}, 0));
		System.out.println(search.search(new int[]{6,7,0,1,2,4,5}, 5));
		System.out.println(search.search(new int[]{4,5,6,7,0,1,2}, 10));
	}

}
