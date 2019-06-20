import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntersectionofTwoArraysII {

	public int[] intersect(int[] nums1, int[] nums2) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		List<Integer> list = new ArrayList<Integer>();
		for(int num : nums1)
			map.put(num, map.getOrDefault(num, 0) + 1);
		for(int num : nums2)
			if(map.containsKey(num)) {
				list.add(num);
				map.put(num, map.get(num) - 1);
				if(map.get(num) == 0) map.remove(num);
			}
		int[] res = new int[list.size()];
		for(int i = 0; i < list.size(); i++)
			res[i] = list.get(i);
		return res;
	}
	
	public static void main(String[] args) {
		IntersectionofTwoArraysII intesection = new IntersectionofTwoArraysII();
		System.out.println(intesection.intersect(new int[]{1,2,2,1}, new int[]{2,2}).length);
	}

}
