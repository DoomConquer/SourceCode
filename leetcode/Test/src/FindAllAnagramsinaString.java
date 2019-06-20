import java.util.ArrayList;
import java.util.List;

public class FindAllAnagramsinaString {

	public List<Integer> findAnagrams(String s, String p) {
		List<Integer> list = new ArrayList<Integer>();
		int len1 = s.length();
		int len2 = p.length();
		if(len1 < len2) return list;
		int[] map = new int[26];
		for(int i = 0; i < len2; i++)
			map[p.charAt(i) - 'a']++;
		int count = len2;
		for(int left = 0, right = 0; right < len1;){
			if(map[s.charAt(right) - 'a'] >= 1)
				count--;
			map[s.charAt(right) - 'a']--;
			right++;
			if(count == 0) list.add(left);
			if(right - left >= len2){
				if(map[s.charAt(left) - 'a'] >= 0)
					count++;
				map[s.charAt(left) - 'a']++;
				left++;
			}
		}
		return list;
	}
	
	public static void main(String[] args) {
		FindAllAnagramsinaString anagram = new FindAllAnagramsinaString();
		List<Integer> list = anagram.findAnagrams("cbaebabacd", "abc");
		for(int num : list)
			System.out.print(num + "  ");
	}

}
