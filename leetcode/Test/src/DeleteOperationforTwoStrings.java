
/**
 * @author li_zhe
 * 思路类似EditDistance, DP
 */
public class DeleteOperationforTwoStrings {

	public int minDistance(String word1, String word2) {
		if(word1 == null || word2 == null) return 0;
		int len1 = word1.length();
		int len2 = word2.length();
		int[][] dp = new int[len1 + 1][len2 + 1];
		for(int i = 0; i <= len1; i++) dp[i][0] = i;
		for(int i = 0; i <= len2; i++) dp[0][i] = i;
		for(int i = 1; i <= len1; i++){
			for(int j = 1; j <= len2; j++){
				if(word1.charAt(i - 1) == word2.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];
				else dp[i][j] = Math.min(dp[i][j - 1] + 1, Math.min(dp[i - 1][j] + 1, dp[i - 1][j - 1] + 2));
			}
		}
		return dp[len1][len2];
	}
	
	// 最长公共子序列
	public int minDistance1(String word1, String word2) {
	    int dp[][] = new int[word1.length()+1][word2.length()+1];
	    for(int i = 0; i <= word1.length(); i++) {
	        for(int j = 0; j <= word2.length(); j++) {
	            if(i == 0 || j == 0) dp[i][j] = 0;
	            else dp[i][j] = (word1.charAt(i-1) == word2.charAt(j-1)) ? dp[i-1][j-1] + 1
	                    : Math.max(dp[i-1][j], dp[i][j-1]);
	        }
	    }
	    int val =  dp[word1.length()][word2.length()];
	    return word1.length() - val + word2.length() - val;
	}
	
	public static void main(String[] args) {
		DeleteOperationforTwoStrings delete = new DeleteOperationforTwoStrings();
		System.out.println(delete.minDistance("sea", "eat"));
		System.out.println(delete.minDistance("sea", ""));
		System.out.println(delete.minDistance("sea", "se"));
		System.out.println(delete.minDistance("sea", "saesa"));
	}

}
