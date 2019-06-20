import java.util.HashMap;
import java.util.Map;

public class SplitArrayintoConsecutiveSubsequences {

	public boolean isPossible(int[] nums) {
		Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		Map<Integer, Integer> seq = new HashMap<Integer, Integer>();
		for(int num : nums)
			count.put(num, count.getOrDefault(num, 0) + 1);
		for(int num : nums){
			if(count.get(num) == 0) continue;
			else if(seq.getOrDefault(num, 0) > 0){
				seq.put(num, seq.get(num) - 1);
				seq.put(num + 1, seq.getOrDefault(num + 1, 0) + 1);
			}else if(count.getOrDefault(num + 1, 0) > 0 && count.getOrDefault(num + 2, 0) > 0){
				count.put(num + 1, count.get(num + 1) - 1);
				count.put(num + 2, count.get(num + 2) - 1);
				seq.put(num + 3, seq.getOrDefault(num + 3, 0) + 1);
			}else return false;
			count.put(num, count.get(num) - 1);
		}
		return true;
	}
	
	public static void main(String[] args) {
		SplitArrayintoConsecutiveSubsequences split = new SplitArrayintoConsecutiveSubsequences();
		System.out.println(split.isPossible(new int[]{1,2,3,4,4,5}));
	}

}
