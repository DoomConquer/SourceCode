import java.util.HashSet;
import java.util.Set;

public class ContainsDuplicateII {

	public boolean containsNearbyDuplicate(int[] nums, int k) {
		Set<Integer> set = new HashSet<>();
		for(int i = 0; i < nums.length; i++){
			if(i > k){
				set.remove(nums[i - k - 1]);
			}
			if(set.contains(nums[i])) return true;
			set.add(nums[i]);
		}
		return false;
	}
	
	public static void main(String[] args) {
		ContainsDuplicateII contains = new ContainsDuplicateII();
		System.out.println(contains.containsNearbyDuplicate(new int[]{1,2,3,1}, 3));
		System.out.println(contains.containsNearbyDuplicate(new int[]{1,0,1,1}, 1));
		System.out.println(contains.containsNearbyDuplicate(new int[]{1,2,1}, 0));
		System.out.println(contains.containsNearbyDuplicate(new int[]{1,2,1}, 1));
	}

}
