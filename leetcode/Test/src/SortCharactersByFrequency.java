import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class SortCharactersByFrequency {

	public String frequencySort(String s) {
		if(s.isEmpty()) return s;
		Map<Character, Integer> map = new HashMap<>();
		for(char ch : s.toCharArray())
			map.put(ch, map.getOrDefault(ch, 0) + 1);
		PriorityQueue<Map.Entry<Character, Integer>> heap = new PriorityQueue<>((o1, o2)-> { return o2.getValue() - o1.getValue(); });
		for(Map.Entry<Character, Integer> entry : map.entrySet())
			heap.add(entry);
		StringBuilder sb = new StringBuilder();
		while(!heap.isEmpty()){
			Map.Entry<Character, Integer> entry = heap.poll();
			int n = entry.getValue();
			char[] array = new char[n];
            Arrays.fill(array, entry.getKey());
			sb.append(new String(array));
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		SortCharactersByFrequency sort = new SortCharactersByFrequency();
		System.out.println(sort.frequencySort("cccaaa"));
	}

}
