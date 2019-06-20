import java.util.HashSet;
import java.util.Set;

public class IntersectionofTwoArrays {

	public int[] intersection(int[] nums1, int[] nums2) {
		Set<Integer> set = new HashSet<Integer>();
		for(int num : nums1)
			set.add(num);
		Set<Integer> resSet = new HashSet<Integer>();
		for(int num : nums2)
			if(set.contains(num)) resSet.add(num);
		int[] res = new int[resSet.size()];
		java.util.Iterator<Integer> iter = resSet.iterator();
		int i = 0;
		while(iter.hasNext()){
			res[i++] = iter.next();
		}
		return res;
	}
	
	public static void main(String[] args) {
		IntersectionofTwoArrays intersection = new IntersectionofTwoArrays();
		System.out.println(intersection.intersection(new int[]{1, 2, 2, 1}, new int[]{2,2})[0]);
	}

}
