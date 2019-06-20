// dp˼·��dp[i][j][k]��ʾs1��i��ʼ��k���ַ���s2��j��ʼ��k���ַ��Ƿ���Scramble��i...m...i+k-1��j...m...j+k-1�ָ�1��k���Ƿ�����
public class ScrambleString {
	public boolean isScramble(String s1, String s2) {
        if(s1 == null || s2 == null || s1.length() != s2.length()) return false;
        int len = s1.length();
        if(len == 0) return true;
        boolean[][][] dp = new boolean[len][len][len + 1];
        for(int k = 1; k <= len; k++){
	        for(int i = 0; i + k <= len; i++){
	        	for(int j = 0; j + k <= len; j++){
	        		if(k == 1) dp[i][j][k] = s1.charAt(i) == s2.charAt(j) ? true : false;
	        		else{
	        			for(int m = 1; m < k; m++){
	        				if(dp[i][j][k]) break;
		        			dp[i][j][k] = (dp[i][j][m] && dp[i + m][j + m][k - m]) || (dp[i + m][j][k - m] && dp[i][j + k - m][m]);
	        			}
	        		}
	        	}
	        }
        }
        return dp[0][0][len];
    }
	
	public static void main(String[] args) {
		ScrambleString scrambleString = new ScrambleString();
		System.out.println(scrambleString.isScramble("", ""));
		System.out.println(scrambleString.isScramble("great", "rgeat"));
		System.out.println(scrambleString.isScramble("great", "rgtae"));
		System.out.println(scrambleString.isScramble("great", "rgtae"));
		System.out.println(scrambleString.isScramble("abcde", "caebd"));
	}

}
