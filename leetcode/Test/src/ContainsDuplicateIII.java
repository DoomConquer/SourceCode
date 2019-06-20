import java.util.TreeSet;

public class ContainsDuplicateIII {

	public boolean containsNearbyAlmostDuplicate(int[] nums, int k, int t) {
		TreeSet<Long> set = new TreeSet<>();
		for(int i = 0; i < nums.length; i++){
			if(i > k) set.remove((long)nums[i - k - 1]);
			Long ceiling = set.ceiling((long)nums[i]);
			Long floor = set.floor((long)nums[i]);
			if((ceiling != null && ceiling - nums[i] <= t) || (floor != null && nums[i] - floor <= t)) return true;
			set.add((long)nums[i]);
		}
		return false;
	}
	
	public static void main(String[] args) {
		ContainsDuplicateIII contains = new ContainsDuplicateIII();
		System.out.println(contains.containsNearbyAlmostDuplicate(new int[]{1,2,3,1}, 4, 0));
		System.out.println(contains.containsNearbyAlmostDuplicate(new int[]{4,2}, 2, 1));
		System.out.println(contains.containsNearbyAlmostDuplicate(new int[]{4,1,6,3}, 100, 1));
		System.out.println(contains.containsNearbyAlmostDuplicate(new int[]{1,2}, 0, 1));
		System.out.println(contains.containsNearbyAlmostDuplicate(new int[]{0,2147483647}, 1, 2147483647));
	}

}
