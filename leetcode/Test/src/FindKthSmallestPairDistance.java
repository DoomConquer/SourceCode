import java.util.Arrays;

// 思路来源leetcode，二分查找解空间
public class FindKthSmallestPairDistance {

    public int smallestDistancePair(int[] nums, int k) {
        Arrays.sort(nums);
        int n = nums.length;
        int right = nums[n - 1] - nums[0];
        int left = 0;
        while(left < right){
        	int mid = (left + right) / 2;
        	if(moreThanK(nums, mid, k)){
        		right = mid;
        	}else{
        		left = mid + 1;
        	}
        }
        return left;
    }
    private boolean moreThanK(int[] nums, int mid , int k){
    	int n = nums.length;
    	int count = 0;
    	for(int i = 0, j = 0; i < n; i++){
    		while(j < n && nums[j] - nums[i] <= mid) j++;
    		count += j - i - 1;
    	}
    	return count >= k;
    }
    
	public static void main(String[] args) {
		FindKthSmallestPairDistance FindKthSmallestPairDistance = new FindKthSmallestPairDistance();
		System.out.println(FindKthSmallestPairDistance.smallestDistancePair(new int[]{1,3,1}, 1));
	}

}
