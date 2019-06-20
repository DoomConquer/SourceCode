
/**
 * @author li_zhe
 * 递归解法超时(自己想的),参考leetcode,DP思路
 * dp[i][j] represents whether s[0,i) and p[0,j) match.
	(1) if p[j-1] != '*' :
	----------------- if (s[i-1] == p[j-1] || p[j-1] == '?')
	------------------------------------------------dp[i][j] = dp[i-1][j-1]
	(2)else p[j-1] == '*' :
	------------------1. if * matches nothing:
	------------------------------------------------ dp[i][j] = dp[i][j-1]
	----------------- 2. if * matches at least one character:
	------------------------------------------------ dp[i][j] = dp[i-1[j]
 */
public class WildcardMatching {

	// 递归,超时
	public boolean isMatch1(String s, String p) {
		return match(s.toCharArray(), p.toCharArray(), 0, 0);
	}
	private boolean match(char[] s, char[] p, int si, int pi){
		if(si == s.length){
			while(pi < p.length && p[pi] == '*') pi++;
			if(pi == p.length) return true;
			else return false;
		}
		if(si == s.length || pi == p.length) return false;
		if(p[pi] == '*') 
			return match(s, p, si + 1, pi) 
				|| match(s, p, si, pi + 1) 
				|| match(s, p, si + 1, pi + 1);
		else if(p[pi] == '?') return match(s, p, si + 1, pi + 1);
		else if(p[pi] != s[si]) return false;
		else return match(s, p, si + 1, pi + 1);
	}
	
	public boolean isMatch(String s, String p) {
        int m = s.length(), n = p.length();
        boolean[][] dp = new boolean[m+1][n+1];
        dp[0][0] = true;
        
        for (int i = 0; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (p.charAt(j-1) == '*') {
                    dp[i][j] = dp[i][j-1] || (i > 0 && dp[i-1][j]);
                } else {
                    dp[i][j] = i > 0 && dp[i-1][j-1] && (s.charAt(i-1) == p.charAt(j-1) || p.charAt(j-1) == '?');
                }
            }
        }
        return dp[m][n];
    }
	
	public static void main(String[] args) {
		WildcardMatching wildcard = new WildcardMatching();
		System.out.println(wildcard.isMatch("", "*"));
		System.out.println(wildcard.isMatch("ho", "ho**"));
		System.out.println(wildcard.isMatch("", "?"));
		System.out.println(wildcard.isMatch("abc", "*dc"));
		System.out.println(wildcard.isMatch("abc", "*abc"));
		System.out.println(wildcard.isMatch("abc", "?abc"));
		System.out.println(wildcard.isMatch("aa", "a"));
		System.out.println(wildcard.isMatch("abc", "*"));
		System.out.println(wildcard.isMatch("cb", "?a"));
		System.out.println(wildcard.isMatch("adceb", "*a*b"));
		System.out.println(wildcard.isMatch("acdcb", "a*c?b"));
	}

}
