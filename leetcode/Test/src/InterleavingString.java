public class InterleavingString {
	public boolean isInterleave(String s1, String s2, String s3) {
        if(s1 == null || s2 == null || s3 == null) return false;
        if(s1.length() + s2.length() != s3.length()) return false;
        int len1 = s1.length();
        int len2 = s2.length();
        boolean[][] dp = new boolean[len1 + 1][len2 + 1];
        dp[0][0] = true;
        for(int i = 0; i <= len1; i++){
        	for(int j = 0; j <= len2; j++){
        		if(i < len1 && s1.charAt(i) == s3.charAt(i + j)){
        			dp[i + 1][j] = dp[i][j] || dp[i + 1][j];
        		}
        		if(j < len2 && s2.charAt(j) == s3.charAt(i + j)){
        			dp[i][j + 1] = dp[i][j] || dp[i][j + 1];
        		}
        	}
        }
        return dp[len1][len2];
    }
	
	public static void main(String[] args) {
		InterleavingString string = new InterleavingString();
		System.out.println(string.isInterleave("", "", ""));
		System.out.println(string.isInterleave("aabcc", "dbbca", "aadbbcbcac"));
		System.out.println(string.isInterleave("aabcc", "dbbca", "aadbbbaccc"));
	}
}
