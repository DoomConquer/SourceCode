
/**
 * @author li_zhe
 * ²Î¿¼leetcode
 */
public class LongestPalindromicSubstring {

	public String longestPalindrome(String s) {
		if(s == null || s.length() == 0) return "";
		int len = s.length();
		int max = 0;
		int start = 0 ,end = 0;
		for(int i = 0; i < len; i++){
			int iLen = 1;
			int j = i;
			int k = 0; // duplicate element
			while(j + 1 < len && s.charAt(j) == s.charAt(j + 1)) {
				iLen++;
				k++; j++;
			}
			j = 0;
			while(i - j >= 0 && i + k + j < len){
				if(s.charAt(i - j) == s.charAt(i + k + j)){
					iLen += 2;
					j++;
				}else break;
			}
			if(max < iLen){
				max = iLen;
				start = i - j;
				end = i + k + j;
			}
		}
		return s.substring(start + 1, end);
	}
	
	public static void main(String[] args) {
		LongestPalindromicSubstring longest = new LongestPalindromicSubstring();
		System.out.println(longest.longestPalindrome("aadddsaaaad"));
		System.out.println(longest.longestPalindrome("babad"));
		System.out.println(longest.longestPalindrome("cbbd"));
		System.out.println(longest.longestPalindrome("ab"));
	}

}
