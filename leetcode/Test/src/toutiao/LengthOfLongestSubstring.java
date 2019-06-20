package toutiao;

import java.util.HashMap;
import java.util.Map;

public class LengthOfLongestSubstring {

    public int lengthOfLongestSubstring(String s) {
        if(s == null || s.length() == 0) return 0;
        char[] sch = s.toCharArray();
        int left = 0, right = 0;
        int maxLen = 0;
        Map<Character, Integer> map = new HashMap<>();
        while(right < sch.length){
        	if(!map.containsKey(sch[right])){
        		map.put(sch[right], right);
        	}else{
        		int start = left;
        		left = map.get(sch[right]) + 1;
        		while(start < left) map.remove(sch[start++]);
        		map.put(sch[right], right);
        	}
        	maxLen = Math.max(maxLen, right - left + 1);
        	right++;
        }
        return maxLen;
    }
    
	public static void main(String[] args) {
		LengthOfLongestSubstring lengthOfLongestSubstring = new LengthOfLongestSubstring();
		System.out.println(lengthOfLongestSubstring.lengthOfLongestSubstring("abcabcbb"));
		System.out.println(lengthOfLongestSubstring.lengthOfLongestSubstring("bbbbbb"));
		System.out.println(lengthOfLongestSubstring.lengthOfLongestSubstring("pwwkew"));
		System.out.println(lengthOfLongestSubstring.lengthOfLongestSubstring("abccbabcad"));
		System.out.println(lengthOfLongestSubstring.lengthOfLongestSubstring("abcdef"));
	}

}
