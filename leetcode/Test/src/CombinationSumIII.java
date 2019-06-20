import java.util.ArrayList;
import java.util.List;

public class CombinationSumIII {

	public List<List<Integer>> combinationSum3(int k, int n) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		combination(res, new ArrayList<Integer>(), k, n, 0, 1);
		return res;
	}
	private void combination(List<List<Integer>> res, List<Integer> one, int k, int n, int sum, int curr){
		if(one.size() == k && sum == n){
			res.add(new ArrayList<Integer>(one)); 
			return;
		}else if(one.size() > k && sum > n)
			return;
		for(int i = curr; i < 10; i++){
			if(!one.contains(i) && (sum + i) <= n){
				one.add(i);
				sum += i;
				combination(res, one, k, n, sum, i + 1);
				sum -= i;
				one.remove(one.size() - 1);
			}
		}
	}
	
	public static void main(String[] args) {
		CombinationSumIII combanation = new CombinationSumIII();
		System.out.println(combanation.combinationSum3(3, 9));
	}

}
