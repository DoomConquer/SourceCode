package toutiao;

public class SearchInRotatedSortedArray {

    public int search(int[] nums, int target) {
        int left = 0, right = nums.length - 1;
        while(left <= right){
        	int mid = left + (right - left) / 2;
        	if(nums[mid] == target) return mid;
        	else if(nums[mid] > target){
        		if(nums[left] > target){
        			if(nums[right] < nums[mid]) left = mid + 1;
        			else right = mid - 1;
        		}else{
        			right = mid - 1;
        		}
        	}else{
        		if(nums[right] < target){
        			if(nums[left] < nums[mid]) left = mid + 1;
        			else right = mid - 1;
        		}else{
        			left = mid + 1;
        		}
        	}
        }
        return -1;
    }
    
	public static void main(String[] args) {
		SearchInRotatedSortedArray searchInRotatedSortedArray = new SearchInRotatedSortedArray();
		System.out.println(searchInRotatedSortedArray.search(new int[]{4,5,6,7,0,1,2}, 0));
		System.out.println(searchInRotatedSortedArray.search(new int[]{4,5,6,7,0,1,2}, 3));
		System.out.println(searchInRotatedSortedArray.search(new int[]{4,5,6,7,0,1,2}, 7));
		System.out.println(searchInRotatedSortedArray.search(new int[]{4,5,6,7,0,1,2}, 4));
		System.out.println(searchInRotatedSortedArray.search(new int[]{4,5,6,7,9,12,0,2,3}, 4));
		System.out.println(searchInRotatedSortedArray.search(new int[]{4,5,6,7,9,12,0,2,3}, 12));
	}

}
