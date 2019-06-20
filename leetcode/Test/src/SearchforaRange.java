
public class SearchforaRange {

	public int[] searchRange(int[] nums, int target) {
		int[] res = new int[]{-1,-1};
		if(nums == null || nums.length == 0) return res;
		res[0] = searchLeft(nums, target);
		res[1] = searchRight(nums, target);
		return res;
	}
	private int searchLeft(int[] nums, int target){
		int left = 0, right = nums.length - 1;
		while(left <= right){
			int mid = (left + right) >>> 1;
			if(nums[mid] == target){
				if(mid - 1 >= 0 && nums[mid - 1] == target) right = mid - 1;
				else return mid;
			}
			else if(nums[mid] > target) right = mid - 1;
			else left = mid + 1;
		}
		return -1;
	}
	private int searchRight(int[] nums, int target){
		int left = 0, right = nums.length - 1;
		while(left <= right){
			int mid = (left + right) >>> 1;
			if(nums[mid] == target){
				if(mid + 1 < nums.length && nums[mid + 1] == target) left = mid + 1;
				else return mid;
			}
			else if(nums[mid] > target) right = mid - 1;
			else left = mid + 1;
		}
		return -1;
	}
	
	public static void main(String[] args) {
		SearchforaRange search = new SearchforaRange();
		int[] res = search.searchRange(new int[]{5,7,7,8,8,10}, 8);
		System.out.println(res[0] + "  " + res[1]);
		res = search.searchRange(new int[]{7,7,7,8,8,10}, 7);
		System.out.println(res[0] + "  " + res[1]);
		res = search.searchRange(new int[]{7,7,7}, 7);
		System.out.println(res[0] + "  " + res[1]);
	}

}
