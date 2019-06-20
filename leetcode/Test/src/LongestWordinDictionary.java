import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class LongestWordinDictionary {

	public String longestWord(String[] words) {
		Map<String, Object> map = new HashMap<String, Object>();
		for(String s : words)
			map.put(s, null);
		Arrays.sort(words, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				if(o1.length() > o2.length()) return 1;
				if(o1.length() == o2.length()) return -o1.compareTo(o2);
				return -1;
			}
		});
		for(int n = words.length - 1; n >= 0; n--){
			String s = words[n];
			int i = 1;
			for(; i <= s.length(); i++){
				if(!map.containsKey(s.substring(0, i))) break;
			}
			if(i == s.length() + 1) return s;
		}
		return "";
	}
	
	public static void main(String[] args) {
		LongestWordinDictionary lonest = new LongestWordinDictionary();
		System.out.println(lonest.longestWord(new String[]{"a", "banana", "app", "appl", "ap", "apply", "apple"}));
	}

}
