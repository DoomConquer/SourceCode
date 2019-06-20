
/**
 * @author li_zhe
 * 思路来源leetcode
 * DP
 *  1, If p.charAt(j) == s.charAt(i) :  dp[i][j] = dp[i-1][j-1];
	2, If p.charAt(j) == '.' : dp[i][j] = dp[i-1][j-1];
	3, If p.charAt(j) == '*': 
	   here are two sub conditions:
	               1   if p.charAt(j-1) != s.charAt(i) : dp[i][j] = dp[i][j-2]  //in this case, a* only counts as empty
	               2   if p.charAt(i-1) == s.charAt(i) or p.charAt(i-1) == '.':
	                              dp[i][j] = dp[i-1][j]    //in this case, a* counts as multiple a 
	                           or dp[i][j] = dp[i][j-1]   // in this case, a* counts as single a
	                           or dp[i][j] = dp[i][j-2]   // in this case, a* counts as empty
 * 
 * 递归(效率差一些)
 * bool isMatch(string s, string p) {
        if (p.empty())    return s.empty();
        
        if ('*' == p[1])
            // x* matches empty string or at least one character: x* -> xx*
            // *s is to ensure s is non-empty
            return (isMatch(s, p.substr(2)) || !s.empty() && (s[0] == p[0] || '.' == p[0]) && isMatch(s.substr(1), p));
        else
            return !s.empty() && (s[0] == p[0] || '.' == p[0]) && isMatch(s.substr(1), p.substr(1));
    }
 */
public class RegularExpressionMatching {

	public boolean isMatch(String s, String p) {
		int slen = s.length();
		int plen = p.length();
		boolean[][] dp = new boolean[slen + 1][plen + 1];
		dp[0][0] = true;
		char[] sch = s.toCharArray();
		char[] pch = p.toCharArray();
		for(int i = 0; i < plen; i++){
			if(pch[i] == '*' && dp[0][i - 1]) dp[0][i + 1] = true;
		}
		for(int i = 0; i < slen; i++){
			for(int j = 0; j < plen; j++){
				if(sch[i] == pch[j] || pch[j] == '.') dp[i + 1][j + 1] = dp[i][j];
				if(pch[j] == '*'){
					if(sch[i] != pch[j - 1] && pch[j - 1] != '.') dp[i + 1][j + 1] = dp[i + 1][j - 1];
					else dp[i + 1][j + 1] = dp[i + 1][j - 1] || dp[i][j + 1] || dp[i + 1][j];
				}
			}
		}
		return dp[slen][plen];
	}
	
	public static void main(String[] args) {
		RegularExpressionMatching matching = new RegularExpressionMatching();
		System.out.println(matching.isMatch("aa", "a*"));
		System.out.println(matching.isMatch("aa", "ab"));
		System.out.println(matching.isMatch("aab", "c*a*b"));
	}

}
