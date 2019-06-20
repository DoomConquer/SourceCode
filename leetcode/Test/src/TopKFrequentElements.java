import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TopKFrequentElements {

	class Pair{
		int count;
		int num;
	}
	public List<Integer> topKFrequent(int[] nums, int k) {
		PriorityQueue<Pair> heap = new PriorityQueue<>(new Comparator<Pair>(){
			@Override
			public int compare(Pair o1, Pair o2) {
				return o2.count - o1.count;
			}
		});
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(int num : nums)
			map.put(num, map.getOrDefault(num, 0) + 1);
		for(Map.Entry<Integer, Integer> entry : map.entrySet()){
			Pair pair = new Pair();
			pair.num = entry.getKey();
			pair.count = entry.getValue();
			heap.add(pair);
		}
		List<Integer> res = new ArrayList<Integer>();
		while(k-- > 0)
			res.add(heap.poll().num);
		return res;
	}
	
	public static void main(String[] args) {
		TopKFrequentElements top = new TopKFrequentElements();
		System.out.println(top.topKFrequent(new int[]{1,1,1,2,2,3}, 2));
	}

}
