import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupAnagrams {

	public List<List<String>> groupAnagrams(String[] strs) {
		List<List<String>> res = new ArrayList<>();
		Map<String, List<String>> map = new HashMap<>();
		for(String s : strs){
			int[] count = new int[26];
			for(char ch : s.toCharArray()) count[ch - 'a']++;
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < 26; i++){
				if(count[i] != 0) sb.append(count[i]).append((char)(i + 'a'));
			}
			String key = sb.toString();
			if(map.containsKey(key)){
				map.get(key).add(s);
			}else{
				List<String> list = new ArrayList<>();
				list.add(s);
				map.put(key, list);
			}
		}
		for(Map.Entry<String, List<String>> entry : map.entrySet()){
			res.add(entry.getValue());
		}
		return res;
	}
	
	public static void main(String[] args) {
		GroupAnagrams group = new GroupAnagrams();
		System.out.println(group.groupAnagrams(new String[]{"eat", "tea", "tan", "ate", "nat", "bat"}));
	}

}
