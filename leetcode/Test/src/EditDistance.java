
/**
 * @author li_zhe
 * 思路参考leetcode
 * DP,dp[i][j]表示Word1[0~i-1]变为word2[0~j-1]需要的最小步数
 * dp[i][0] = i;
 * dp[0][j] = j;
 * dp[i][j] = dp[i - 1][j - 1], if word1[i - 1] = word2[j - 1];
 * dp[i][j] = min(dp[i - 1][j - 1] + 1, dp[i - 1][j] + 1, dp[i][j - 1] + 1), otherwise.
 */
public class EditDistance {

	public int minDistance(String word1, String word2) {
		if(word1 == null || word2 == null) return 0;
		int len1 = word1.length();
		int len2 = word2.length();
		int[][] dp = new int[len1 + 1][len2 + 1];
		for(int i = 0; i <= len1; i++)
			dp[i][0] = i;
		for(int i = 0; i <= len2; i++)
			dp[0][i] = i;
		for(int i = 1; i <= len1; i++){
			for(int j = 1; j <= len2; j++){
				if(word1.charAt(i - 1) == word2.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];
				else dp[i][j] = Math.min(dp[i - 1][j - 1] + 1, Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
			}
		}
		return dp[len1][len2];
	}
	
	public static void main(String[] args) {
		EditDistance distance = new EditDistance();
		System.out.println(distance.minDistance("", ""));
		System.out.println(distance.minDistance("horse", "ros"));
		System.out.println(distance.minDistance("intention", "execution"));
		System.out.println(distance.minDistance("word", "cord"));
	}

}
