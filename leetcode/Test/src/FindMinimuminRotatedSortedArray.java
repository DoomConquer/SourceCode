public class FindMinimuminRotatedSortedArray {

    public int findMin(int[] nums) {
        int left = 0, right = nums.length - 1;
        while(left < right){
        	int mid = (left + right) >> 1;
        	if(nums[mid] < nums[right]) right = mid;
        	else left = mid + 1;
        }
        return nums[left];
    }
    
	public static void main(String[] args) {
		FindMinimuminRotatedSortedArray findMinimuminRotatedSortedArray = new FindMinimuminRotatedSortedArray();
		System.out.println(findMinimuminRotatedSortedArray.findMin(new int[]{4,5,6,7,0,1,2}));
		System.out.println(findMinimuminRotatedSortedArray.findMin(new int[]{4,5,6,7,0,1}));
		System.out.println(findMinimuminRotatedSortedArray.findMin(new int[]{4,0,1,3}));
	}

}
