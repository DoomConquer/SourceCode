import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreeSum {
	
	public List<List<Integer>> threeSum(int[] nums) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		Arrays.sort(nums);
		for(int i = 0; i < nums.length - 2; i++){
			if(i == 0 || (i > 0 && nums[i] != nums[i - 1])){
				int sum = -nums[i];
				int left = i + 1;
				int right = nums.length - 1;
				while(left < right){
					if(nums[left] + nums[right] == sum){
						res.add(Arrays.asList(new Integer[]{nums[left], nums[right], -sum}));
						while(left < right && nums[left] == nums[left + 1]) left++;
						while(left < right && nums[right] == nums[right - 1]) right--;
						left++; right--;
					}else if(nums[left] + nums[right] > sum) right--;
					else left++;
				}
			}
		}
		return res;
	}
	
	public static void main(String[] args) {
		ThreeSum sum = new ThreeSum();
		System.out.println(sum.threeSum(new int[]{-1, 0, 1, 2, -1, -4}));
	}
}
