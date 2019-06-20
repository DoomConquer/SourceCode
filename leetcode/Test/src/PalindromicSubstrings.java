
/**
 * @author li_zhe
 * DP思路  时间复杂度O（n^2）
 */
public class PalindromicSubstrings {

	public int countSubstrings(String s) {
		if(s == null || s.length() == 0) return 0;
		int len = s.length();
		int[][] dp = new int[len][len];
		char[] ch = s.toCharArray();
		int sum = 0;
		for(int i = len - 1; i >= 0; i--){
			for(int j = i ; j < len; j++){
				if(ch[i] == ch[j] && (j - i < 3 || dp[i + 1][j - 1] > 0)) dp[i][j] += 1;
				sum += dp[i][j];
			}
		}
		return sum;
	}
	
	public static void main(String[] args) {
		PalindromicSubstrings pali = new PalindromicSubstrings();
		System.out.println(pali.countSubstrings("aaa"));
		System.out.println(pali.countSubstrings("abc"));
	}

}
