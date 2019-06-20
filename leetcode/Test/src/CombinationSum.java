import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinationSum {
	public List<List<Integer>> combinationSum(int[] candidates, int target) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		Arrays.sort(candidates);
		combination(candidates, target, 0, new ArrayList<Integer>(), res);
		return res;
    }
	private void combination(int[] candidates, int target, int start, List<Integer> one, List<List<Integer>> res){
		if(target == 0){
			res.add(new ArrayList<Integer>(one));
		}else if(target < 0){
			return;
		}else{
			int len = candidates.length;
			for(int i = start; i < len; i++){
				one.add(candidates[i]);
				combination(candidates, target - candidates[i], i, one, res);
				one.remove(one.size() - 1);
			}
		}
	}

	public static void main(String[] args) {
		CombinationSum combination = new CombinationSum();
		System.out.println(combination.combinationSum(new int[]{2,6,3,7}, 7));
	}
}
