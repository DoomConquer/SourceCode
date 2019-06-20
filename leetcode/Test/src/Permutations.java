import java.util.ArrayList;
import java.util.List;

public class Permutations {

	public List<List<Integer>> permute(int[] nums) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		if(nums.length == 0)
			return res;
		permutation(nums, 0, res);
		return res;
	}
	private void permutation(int[] nums, int curr, List<List<Integer>> res){
		int len = nums.length;
		if(curr == len - 1){
			List<Integer> result = new ArrayList<Integer>();
			for(int num : nums)
				result.add(num);
			res.add(result);
			return;
		}else{
			for(int i = curr; i < len; i++){
				int temp = nums[curr];
				nums[curr] = nums[i];
				nums[i] = temp;
				
				permutation(nums, curr + 1, res);
				
				temp = nums[curr];
				nums[curr] = nums[i];
				nums[i] = temp;
			}
		}
	}
	
	public List<List<Integer>> permute1(int[] nums) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		permutation(res, new ArrayList<Integer>(), nums);
		return res;
	}
	private void permutation(List<List<Integer>> res, List<Integer> one, int[] nums){
		int len = nums.length;
		if(one.size() == len)
			res.add(new ArrayList<Integer>(one));
		else{
			for(int i = 0; i < len; i++){
				if(!one.contains(nums[i])){
					one.add(nums[i]);
					permutation(res, one, nums);
					one.remove(one.size() - 1);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Permutations permutation = new Permutations();
		System.out.println(permutation.permute(new int[]{1,2,3}));
		System.out.println(permutation.permute1(new int[]{1,2,3}));
	}

}
