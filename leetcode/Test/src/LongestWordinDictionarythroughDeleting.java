import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LongestWordinDictionarythroughDeleting {

	public String findLongestWord(String s, List<String> d) {
		Collections.sort(d, new Comparator<String>(){
			@Override
			public int compare(String s1, String s2) {
				if(s1.length() > s2.length()) return 1;
				if(s1.length() == s2.length()) return -s1.compareTo(s2);
				return -1;
			}});
		for(int n = d.size() - 1; n >= 0; n--){
			String curr = d.get(n);
			int j = 0;
			for(int i = 0; i < s.length() && j < curr.length(); i++){
				if(s.charAt(i) == curr.charAt(j)) j++;
			}
			if(j == curr.length()) return curr;
		}
		return "";
	}
	
	public static void main(String[] args) {
		LongestWordinDictionarythroughDeleting lonest = new LongestWordinDictionarythroughDeleting();
		List<String> list = new ArrayList<String>();
		list.add("apple");
		list.add("ewaf");
		list.add("awefawfwaf");
		list.add("awef");
		list.add("awefe");
		list.add("ewafeffewafewf");
		System.out.println(lonest.findLongestWord("aewfafwafjlwajflwajflwafj", list));
	}

}
