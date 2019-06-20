import java.util.Arrays;

/**
 * @author li_zhe
 * 思路参考leetcode，双指针，O(n)
 * 针对1-26个字母，每次计算不超过n个字母的最长子序列AtLeastKRepeatingCharacters。
 */
public class LongestSubstringwithAtLeastKRepeatingCharacters {

	public int longestSubstring(String s, int k) {
		if(s == null || s.length() == 0) return 0;
		if(k <= 1) return s.length();
		int[] map = new int[26];
		int max = 0;
		char[] ch = s.toCharArray();
		for(int letterCount = 1; letterCount <= 26; letterCount++){
			Arrays.fill(map, 0);
			int letters = 0;
			int leastkTimes = 0;
			int start = 0, end = 0;
			while(end < s.length()){
				if(letters <= letterCount){
					int index = ch[end] - 'a';
					if(map[index] == 0) letters++;
					map[index]++;
					if(map[index] == k) leastkTimes++;
					end++;
				}else{
					int index = ch[start] - 'a';
					if(map[index] == k) leastkTimes--;
					map[index]--;
					if(map[index] == 0) letters--;
					start++;
				}
				if(letters == letterCount && letters == leastkTimes) max = Math.max(max, end - start);
			}
		}
		return max;
	}
	
	public static void main(String[] args) {
		LongestSubstringwithAtLeastKRepeatingCharacters longest = new LongestSubstringwithAtLeastKRepeatingCharacters();
		System.out.println(longest.longestSubstring("ababb", 2));
		System.out.println(longest.longestSubstring("aaabb", 3));
		System.out.println(longest.longestSubstring("aaabbbbbb", 4));
		System.out.println(longest.longestSubstring("aacbbbdc", 2));
	}

}
