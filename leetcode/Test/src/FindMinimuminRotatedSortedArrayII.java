public class FindMinimuminRotatedSortedArrayII {

    public int findMin(int[] nums) {
    	return findMin(nums, 0, nums.length - 1);
    }
    private int findMin(int[] nums, int left, int right){
    	while(left < right){
    		int mid = (left + right) / 2;
    		if(nums[left] == nums[mid] && nums[mid] == nums[right]) 
    			return Math.min(findMin(nums, left, mid - 1), findMin(nums, mid + 1, right));
    		else if(nums[mid] <= nums[right]) right = mid;
    		else left = mid + 1;
    	}
    	return nums[left];
    }
    
	public static void main(String[] args) {
		FindMinimuminRotatedSortedArrayII findMinimuminRotatedSortedArrayII = new FindMinimuminRotatedSortedArrayII();
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{2,2,2,0,1}));
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{1,3,5}));
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{1,3,3}));
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{3,3,1}));
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{1,0,1,1,1}));
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{1,1,0,1,1}));
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{1,1,1,0,1}));
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{1,1,1,1,1}));
		System.out.println(findMinimuminRotatedSortedArrayII.findMin(new int[]{3,3,3,3,3,3,3,1,1,1,1,1,2,2,2,2,2,3,3,3,3,3}));
	}

}
