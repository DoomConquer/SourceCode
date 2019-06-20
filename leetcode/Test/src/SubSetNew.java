import java.util.ArrayList;
import java.util.List;

public class SubSetNew {
	public List<List<Integer>> subsets(int[] nums) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		sub(res, new ArrayList<Integer>(), nums, 0);
		return res;
			
	}
	private void sub(List<List<Integer>> res, List<Integer> one, int[] nums, int start){
		int len = nums.length;
		res.add(new ArrayList<Integer>(one));
		for(int i = start; i < len; i++){
			one.add(nums[i]);
			sub(res, one, nums, i + 1);
			one.remove(one.size() - 1);
		}
	}
	
	public static void main(String[] args) {
		SubSetNew subset = new SubSetNew();
		System.out.println(subset.subsets(new int[]{1,2,3,4}));
	}
}
