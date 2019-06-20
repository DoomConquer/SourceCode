/**
 * @author li_zhe
 * 思路参考leetcode
 * map + 双指针
 */
public class MinimumWindowSubstring {

	public String minWindow(String s, String t) {
		if(s == null || s.length() == 0) return "";
		int[] map = new int[256];
		char[] sch = s.toCharArray();
		for(char ch : t.toCharArray()) map[ch]++;
		int left = 0, right = 0, count = t.length();
		int min = Integer.MAX_VALUE;
		int start = 0;
		int slen = s.length();
		while(right < slen){
			while(right < slen && count > 0){
				if(map[sch[right]] > 0) count--;
				map[sch[right]]--;
				right++;
			}
			while(count == 0){
				if(right - left < min){
					min = right - left;
					start = left;
				}
				map[sch[left]]++;
				if(map[sch[left]] > 0) count++;
				left++;
			}
		}
		return min == Integer.MAX_VALUE ? "" : s.substring(start, start + min);
	}
	
	public static void main(String[] args) {
		MinimumWindowSubstring window = new MinimumWindowSubstring();
		System.out.println(window.minWindow("ADOBECODEBANC", "ABC"));
		System.out.println(window.minWindow("ADOBECODEBANC", "ODB"));
	}

}
