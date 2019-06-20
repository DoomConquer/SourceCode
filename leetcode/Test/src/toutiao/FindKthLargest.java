package toutiao;

public class FindKthLargest {

    public int findKthLargest(int[] nums, int k) {
    	return find(nums, 0, nums.length - 1, k);
    }
    private int find(int[] nums, int low, int high, int k){
    	int left = low, right = high;
    	int temp = nums[left];
    	while(left < right){
    		while(left < right && nums[right] <= temp) right--;
    		nums[left] = nums[right];
    		while(left < right && nums[left] >= temp) left++;
    		nums[right] = nums[left];
    	}
    	if(left == k - 1) return temp;
    	nums[left] = temp;
		if(left > k - 1)
			return find(nums, low, left - 1, k);
		else
			return find(nums, left + 1, high, k);
    }
    
	public static void main(String[] args) {
		FindKthLargest findKthLargest = new FindKthLargest();
		System.out.println(findKthLargest.findKthLargest(new int[]{3,2,1,5,6,4}, 2));
		System.out.println(findKthLargest.findKthLargest(new int[]{3,2,3,1,2,4,5,5,6}, 4));
	}

}
