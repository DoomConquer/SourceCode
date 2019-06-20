import java.util.HashMap;
import java.util.Map;

public class LongestSubstringWithoutRepeatingCharacters {

	public int lengthOfLongestSubstring(String s) {
		if(s.isEmpty()) return 0;
		if(s.length() == 1) return 1;
		int maxLen = Integer.MIN_VALUE;
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		for(int left = 0, right = 0; right < s.length(); right++){
			char ch = s.charAt(right);
			if(map.containsKey(ch)){
				left = Math.max(left, map.get(ch) + 1);
			}
			map.put(ch, right);
			maxLen = Math.max(maxLen, right - left + 1);
		}
		return maxLen;
	}
	
	public static void main(String[] args) {
		LongestSubstringWithoutRepeatingCharacters longest = new LongestSubstringWithoutRepeatingCharacters();
		System.out.println(longest.lengthOfLongestSubstring("abba"));
	}

}
