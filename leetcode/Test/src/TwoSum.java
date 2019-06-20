import java.util.HashMap;
import java.util.Map;

public class TwoSum {

	public int[] twoSum(int[] nums, int target) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		int[] res = new int[2];
		for(int i = 0; i < nums.length; i++){
			map.put(nums[i], i);
		}
		for(int i = 0; i < nums.length; i++){
			if(map.containsKey(target - nums[i]) && map.get(target - nums[i]) != i){
				res[0] = i;
				res[1] = map.get(target - nums[i]);
			}
		}
		return res;
	}
	
	public static void main(String[] args) {
		TwoSum twoSum = new TwoSum();
		System.out.println(twoSum.twoSum(new int[]{2, 7, 11, 15}, 9)[1]);
	}

}
