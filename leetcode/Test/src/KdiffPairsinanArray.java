import java.util.Arrays;

public class KdiffPairsinanArray {

	public int findPairs(int[] nums, int k) {
		Arrays.sort(nums);
		int sum = 0;
		for(int slow = 0, fast = 0; slow < nums.length; slow++){
			if(fast <= slow) fast = slow + 1;
			while(fast < nums.length && nums[fast] - nums[slow] < k) fast++;
			if(fast < nums.length && nums[fast] - nums[slow] == k) sum++;
			while(slow < nums.length - 1 && nums[slow] == nums[slow + 1]) slow++;
		}
		return sum;
	}
	
	public static void main(String[] args) {
		KdiffPairsinanArray kdiff = new KdiffPairsinanArray();
		System.out.println(kdiff.findPairs(new int[]{1,1,1,2,3}, 1));
	}

}
