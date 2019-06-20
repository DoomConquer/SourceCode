public class DistinctSubsequences {

    public int numDistinct(String s, String t) {
        if(s == null || t == null || s.length() == 0 || t.length() == 0) return 0;
        int sLen = s.length(), tLen = t.length();
        int[][] dp = new int[sLen][tLen];
        for(int i = 0; i < tLen; i++){
        	for(int j = i; j < sLen; j++){
        		if(i == 0){
        			if(s.charAt(j) == t.charAt(0)){
        				if(j == 0) dp[j][0] = 1;
        				else dp[j][0] = dp[j - 1][0] + 1;
        			}else{
        				if(j == 0) dp[j][0] = 0;
        				else dp[j][0] = dp[j - 1][0];
        			}
        			continue;
        		}
        		if(s.charAt(j) == t.charAt(i)) dp[j][i] = dp[j - 1][i] + dp[j - 1][i - 1];
        		else dp[j][i] = dp[j - 1][i];
        	}
        }
        return dp[sLen - 1][tLen - 1];
    }
    
	public static void main(String[] args) {
		DistinctSubsequences distinctSubsequences = new DistinctSubsequences();
		System.out.println(distinctSubsequences.numDistinct("rabbbit", "rabbit"));
		System.out.println(distinctSubsequences.numDistinct("babgbag", "bag"));
		System.out.println(distinctSubsequences.numDistinct("babgbag", "b"));
		System.out.println(distinctSubsequences.numDistinct("babgbag", "ag"));
	}

}
