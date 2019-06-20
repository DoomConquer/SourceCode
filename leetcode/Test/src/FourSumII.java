import java.util.HashMap;
import java.util.Map;

public class FourSumII {

	public int fourSumCount(int[] A, int[] B, int[] C, int[] D) {
		int len = A.length;
		int count = 0;
		Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
		Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
		for(int i = 0; i < len; i++){
			for(int j = 0; j < len; j++){
				int sum = A[i] + B[j];
				if(map1.containsKey(sum)){
					map1.put(sum, map1.get(sum) + 1);
				}else{
					map1.put(sum, 1);
				}
			}
		}
		for(int i = 0; i < len; i++){
			for(int j = 0; j < len; j++){
				int sum = C[i] + D[j];
				if(map2.containsKey(sum)){
					map2.put(sum, map2.get(sum) + 1);
				}else{
					map2.put(sum, 1);
				}
			}
		}
		for(Map.Entry<Integer, Integer> entry : map1.entrySet()){
			int key = entry.getKey();
			if(map2.containsKey(-key)){
				count += entry.getValue() * map2.get(-key);
			}
		}
		return count;
	}
	
	public static void main(String[] args) {
		FourSumII fourSum = new FourSumII();
		System.out.println(fourSum.fourSumCount(new int[]{1,2}, new int[]{-2,-1}, new int[]{-1,2}, new int[]{0,2}));
	}

}
