import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermutationsII {

	public List<List<Integer>> permuteUnique(int[] nums) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		if(nums.length == 0)
			return res;
		Arrays.sort(nums);
		permutation(nums, new ArrayList<Integer>(), res, new boolean[nums.length]);
		return res;
	}
	private void permutation(int[] nums, List<Integer> one, List<List<Integer>> res, boolean[] flag){
		int len = nums.length;
		if(one.size() == len){
			res.add(new ArrayList<Integer>(one));
		}else{
			for(int i = 0; i < len; i++){
				if(flag[i] || (i > 0 && nums[i] == nums[i - 1] && !flag[i - 1])) continue;
				flag[i] = true;
				one.add(nums[i]);
				permutation(nums, one, res, flag);
				flag[i] = false;
				one.remove(one.size() - 1);
			}
		}
	}
	
	public static void main(String[] args) {
		PermutationsII permutation = new PermutationsII();
		System.out.println(permutation.permuteUnique(new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2}));
	}

}
