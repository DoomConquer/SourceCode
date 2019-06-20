import java.util.ArrayList;
import java.util.List;

public class Combination {

	public List<List<Integer>> combine(int n, int k) {
		int[] nums = new int[n];
		for(int i = 1; i <= n; i++)
			nums[i - 1] = i;
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		if(nums.length == 0 || nums.length < k)
			return res;
		permutation(nums, new ArrayList<Integer>(), res, k, 0);
		return res;
	}
	private void permutation(int[] nums, List<Integer> one, List<List<Integer>> res, int k, int start){
		int len = nums.length;
		if(one.size() == k){
			res.add(new ArrayList<Integer>(one));
		}else{
			for(int i = start; i < len; i++){
				if(one.contains(nums[i])) continue;
				one.add(nums[i]);
				permutation(nums, one, res, k, i + 1);
				one.remove(one.size() - 1);
			}
		}
	}
	
	public static void main(String[] args) {
		Combination combination = new Combination();
		System.out.println(combination.combine(4, 2));
	}

}
