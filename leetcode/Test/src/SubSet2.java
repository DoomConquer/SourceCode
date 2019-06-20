import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubSet2 {
   public List<List<Integer>> subsetsWithDup(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<List<Integer>>();
        if(nums.length == 0 || nums == null) return result;
        helper(nums, new ArrayList<Integer>(), 0, result);
        return result;
    }
    
    public void helper(int[] nums, ArrayList<Integer> current, int index, List<List<Integer>> result) {
        result.add(current);
        for(int i = index; i < nums.length; i++) {
            if(i > index && nums[i] == nums[i - 1]) continue;
            ArrayList<Integer> newCurrent = new ArrayList<Integer>(current);
            newCurrent.add(nums[i]);
            helper(nums, newCurrent, i + 1, result);
        }
    }

	public static void main(String[] args) {
		SubSet2 subSet2 = new SubSet2();
		int[] nums = new int[]{1,2,3,4};
		List<List<Integer>> res = subSet2.subsetsWithDup(nums);
		System.out.println(res.toString());
		System.out.println(res.size());
	}

}
