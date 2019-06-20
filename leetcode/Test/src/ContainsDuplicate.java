import java.util.HashSet;
import java.util.Set;

public class ContainsDuplicate {

	public boolean containsDuplicate(int[] nums) {
		Set<Integer> set = new HashSet<>();
		for(int num : nums){
			if(set.contains(num)) return true;
			set.add(num);
		}
		return false;
	}
	
	public static void main(String[] args) {
		ContainsDuplicate contains = new ContainsDuplicate();
		System.out.println(contains.containsDuplicate(new int[]{1,1,1,3,3,4,3,2,4,2}));
	}

}
