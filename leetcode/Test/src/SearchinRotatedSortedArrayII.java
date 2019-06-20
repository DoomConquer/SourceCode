public class SearchinRotatedSortedArrayII {

    public boolean search(int[] nums, int target) {
        return search(nums, 0, nums.length - 1, target);
    }
    private boolean search(int[] nums, int left, int right, int target){
    	while(left <= right){
        	int mid = left + (right - left) / 2;
        	if(nums[mid] == target || nums[left] == target || nums[right] == target) return true;
        	else if(nums[left] == nums[mid] && nums[mid] == nums[right]) 
        		return search(nums, left, mid - 1, target) || search(nums, mid + 1, right, target);
        	else if(nums[mid] >= nums[left]){
        		if(nums[left] < target && nums[mid] > target) right = mid - 1;
        		else left = mid + 1;
        	}else if(nums[mid] <= nums[right]){
        		if(nums[right] > target && nums[mid] < target) left = mid + 1;
        		else right = mid - 1;
        	}
        }
        return false;
    }
    
	public static void main(String[] args) {
		SearchinRotatedSortedArrayII searchinRotatedSortedArrayII = new SearchinRotatedSortedArrayII();
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{3,1}, 0));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{1,3,1,1,1}, 3));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{1,3,3,3,1}, 1));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{1,1,3,1}, 3));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{2,5,6,0,0,1,2}, 0));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{2,5,6,0,0,1,2}, 2));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{2,5,6,0,0,1,2}, 3));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{2,5,6,0,0,1,2}, 13));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{2,5,6,6,6,6,6,6,0,0,1,1,1,1,1,2,2,2,2,2,2,2}, 3));
		System.out.println(searchinRotatedSortedArrayII.search(new int[]{2,5,6,6,6,6,6,6,0,0,1,1,1,1,1,2,2,2,2,2,2,2}, 0));
	}

}
