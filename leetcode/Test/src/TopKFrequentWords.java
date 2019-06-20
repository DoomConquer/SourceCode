import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TopKFrequentWords {

	class Pair{
		int count;
		String word;
	}
	public List<String> topKFrequent(String[] words, int k) {
		PriorityQueue<Pair> heap = new PriorityQueue<>(new Comparator<Pair>(){
			@Override
			public int compare(Pair o1, Pair o2) {
				if(o1.count < o2.count) return 1;
				if(o1.count == o2.count) return o1.word.compareTo(o2.word);
				return -1;
			}
		});
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(String word : words){
			map.put(word, map.getOrDefault(word, 0) + 1);
		}
		for(Map.Entry<String, Integer> entry : map.entrySet()){
			Pair pair = new Pair();
			pair.word = entry.getKey();
			pair.count = entry.getValue();
			heap.add(pair);
		}
		List<String> res = new ArrayList<String>();
		while(k-- > 0){
			res.add(heap.poll().word);
		}
		return res;
	}
	
	public static void main(String[] args) {
		TopKFrequentWords top = new TopKFrequentWords();
		System.out.println(top.topKFrequent(new String[]{"a", "aa", "aaa"}, 2));
	}

}
