import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinationSumII {

	public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList<List<Integer>>();
        Arrays.sort(candidates);
        combination(candidates, target, 0, new ArrayList<Integer>(), res);
        return res;
    }
	private void combination(int[] candidates, int target, int start, List<Integer> one, List<List<Integer>> res){
		int len = candidates.length;
		if(target == 0){
			res.add(new ArrayList<Integer>(one));
		}else{
			for(int i = start; i < len; i++){
				if(i > start && candidates[i] == candidates[i - 1]) continue;
				if(candidates[i] > target) return;
				one.add(candidates[i]);
				combination(candidates, target - candidates[i], i + 1, one, res);
				one.remove(one.size() - 1);
			}
		}
	}
	
	public static void main(String[] args) {
		CombinationSumII combination = new CombinationSumII();
		System.out.println(combination.combinationSum2(new int[]{10, 1, 2, 7, 6, 1, 5}, 8));
	}

}
