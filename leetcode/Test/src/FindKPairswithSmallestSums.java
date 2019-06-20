import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class FindKPairswithSmallestSums {

	class Pair{
		int i;
		int j;
		int sum;
	}
	public List<int[]> kSmallestPairs(int[] nums1, int[] nums2, int k) {
		List<int[]> res = new ArrayList<int[]>();
		PriorityQueue<Pair> heap = new PriorityQueue<>((o1, o2)-> { return o1.sum - o2.sum; });
		for(int i = 0; i < nums1.length && nums2.length > 0; i++){
			Pair pair = new Pair();
			pair.i = i;
			pair.j = 0;
			pair.sum = nums1[i] + nums2[0];
			heap.add(pair);
		}
		while(k-- > 0 && !heap.isEmpty()){
			Pair pair = heap.poll();
			res.add(new int[]{nums1[pair.i], nums2[pair.j]});
			Pair next = new Pair();
			if(pair.j == nums2.length) continue;
			next.i = pair.i;
			next.j = pair.j + 1;
			next.sum = nums1[pair.i] + nums2[pair.j];
			heap.add(next);
		}
		return res;
	}
	
	public static void main(String[] args) {
		FindKPairswithSmallestSums find = new FindKPairswithSmallestSums();
		List<int[]> res = find.kSmallestPairs(new int[]{1,7,11}, new int[]{2,4,6}, 3);
		for(int[] arr : res){
			System.out.print("[");
			for(int num : arr)
				System.out.print(num + "  ");
			System.out.print("]  ");
		}
	}

}
