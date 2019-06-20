import java.util.HashMap;
import java.util.Map;

public class SubarraySumEqualsK {

	public int subarraySum(int[] nums, int k) {
		Map<Integer, Integer> map = new HashMap<>();
		int sum = 0;
		int num = 0;
		map.put(0, 1);
		for(int i = 0; i < nums.length; i++){
			sum += nums[i];
			num += map.getOrDefault(sum - k, 0);
			map.put(sum, map.getOrDefault(sum, 0) + 1);
		}
		return num;
	}
	
	public static void main(String[] args) {
		SubarraySumEqualsK subarray = new SubarraySumEqualsK();
		System.out.println(subarray.subarraySum(new int[]{1,1,1}, 2));
	}

}
