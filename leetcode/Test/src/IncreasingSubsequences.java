import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class IncreasingSubsequences {

	public List<List<Integer>> findSubsequences(int[] nums) {
		if(nums == null || nums.length == 0) return new ArrayList<>();
		List<List<Integer>> res = new ArrayList<>();
		find(nums, res, 0);
		Iterator<List<Integer>> iter = res.iterator();
		Set<String> set = new HashSet<>();
		while(iter.hasNext()){
			List<Integer> list = iter.next();
			if(list.size() == 1) {
				iter.remove();
				continue;
			}
			String s = list.toString();
			if(set.contains(s)) {
				iter.remove();
				continue;
			}
			set.add(s);
		}
		return res;
	}
	private void find(int[] nums, List<List<Integer>> res, int layer){
		if(layer == nums.length) return;
		Iterator<List<Integer>> iter = res.iterator();
		List<List<Integer>> temp = new ArrayList<>();
		while(iter.hasNext()){
			List<Integer> list = iter.next();
			if(list.get(list.size() - 1) <= nums[layer]){
				List<Integer> dup = new ArrayList<>();
				dup.addAll(list);
				dup.add(nums[layer]);
				temp.add(dup);
			}
		}
		res.addAll(temp);
		List<Integer> l = new ArrayList<>();
		l.add(nums[layer]);
		res.add(l);
		find(nums, res, layer + 1);
	}
	
	public static void main(String[] args) {
		IncreasingSubsequences increasing = new IncreasingSubsequences();
		for(List<Integer> list : increasing.findSubsequences(new int[]{4, 6, 7, 7}))
			System.out.println(list);
		
		for(List<Integer> list : increasing.findSubsequences(new int[]{4, -6, 7, 7}))
			System.out.println(list);
	}

}
