import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SubSet {
	public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        recurse(result, nums, new Stack<>(), 0);
        return result;
    }
    
    private void recurse(List<List<Integer>> result, int[] nums, Stack<Integer> path, int position) {
        if(position == nums.length) {
            result.add(new ArrayList<>(path));
            return;
        }
        path.push(nums[position]);
        recurse(result, nums, path, position + 1);
        path.pop();
        recurse(result, nums, path, position + 1);
    }
    
	public static void main(String[] args) {
		SubSet subSet = new SubSet();
		int[] nums = new int[]{1,2,3};
		List<List<Integer>> res = subSet.subsets(nums);
		System.out.println(res.toString());
	}
}
