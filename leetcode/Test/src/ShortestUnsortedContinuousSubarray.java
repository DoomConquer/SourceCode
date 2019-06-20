import java.util.Arrays;

public class ShortestUnsortedContinuousSubarray {

	public int findUnsortedSubarray(int[] nums) {
		int[] copy = Arrays.copyOf(nums, nums.length);
		Arrays.sort(copy);
		int start = -1;
		for(int i = 0; i < nums.length; i++){
			if(nums[i] != copy[i]){
				start = i;
				break;
			}
		}
		if(start == -1) return 0;
		int end = nums.length - 1;
		for(int i = nums.length - 1; i >= 0; i--){
			if(nums[i] != copy[i]){
				end = i;
				break;
			}
		}
		return end - start + 1;
	}
	
	public static void main(String[] args) {
		ShortestUnsortedContinuousSubarray shortest = new ShortestUnsortedContinuousSubarray();
		System.out.println(shortest.findUnsortedSubarray(new int[]{2, 6, 4, 8, 10, 9, 15}));
	}

}
