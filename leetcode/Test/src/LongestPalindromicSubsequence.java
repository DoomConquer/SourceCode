
/**
 * @author li_zhe
 * �Լ���˼·,���벻�Ǻܼ��
 * DP, O(n^2)
 * ˼·��Burst Balloons���ƣ��Ե��������
 */
public class LongestPalindromicSubsequence {

	public int longestPalindromeSubseq(String s) {
		if(s == null || s.length() == 0) return 0;
		int n = s.length();
		int[][] dp = new int[n][n];
		char[] sch = s.toCharArray();
		for(int i = 0; i < n; i++){
			for(int j = 0; j + i < n; j++){
				if(j < i + j){
					if(sch[j] == sch[i + j])
						dp[j][i + j] = dp[j + 1][i + j - 1] + 2;
					else
						dp[j][i + j] = Math.max(dp[j + 1][i + j], dp[j][i + j - 1]);
				}else{
					dp[j][i + j] = 1;
				}
			}
		}
		return dp[0][n - 1];
	}
	
	public static void main(String[] args) {
		LongestPalindromicSubsequence longest = new LongestPalindromicSubsequence();
		System.out.println(longest.longestPalindromeSubseq("bbadsssbbs"));
		System.out.println(longest.longestPalindromeSubseq("bbadssss"));
		System.out.println(longest.longestPalindromeSubseq("sbbadssss"));
		System.out.println(longest.longestPalindromeSubseq("sb"));
		System.out.println(longest.longestPalindromeSubseq("s"));
	}

}
