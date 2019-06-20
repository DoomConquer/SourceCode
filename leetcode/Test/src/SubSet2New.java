import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubSet2New {
	public List<List<Integer>> subsetsWithDup(int[] nums) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		Arrays.sort(nums);
		sub(res, new ArrayList<Integer>(), nums, 0);
		return res;
			
	}
	private void sub(List<List<Integer>> res, List<Integer> one, int[] nums, int start){
		int len = nums.length;
		res.add(new ArrayList<Integer>(one));
		for(int i = start; i < len; i++){
			if(i > start && nums[i] == nums[i - 1]) continue;
			one.add(nums[i]);
			sub(res, one, nums, i + 1);
			one.remove(one.size() - 1);
		}
	}
	
	public static void main(String[] args) {
		SubSet2New subset = new SubSet2New();
		System.out.println(subset.subsetsWithDup(new int[]{4,4,4,1,4}));
	}
}
