
/**
 * @author li_zhe
 * 思路leetcode，DP
 * 如果能和前一位组合成一个1-26的数,dp[i] = dp[i-1] + dp[i-2]
 */
public class DecodeWays {

	public int numDecodings(String s) {
		if(s == null || s.length() == 0) return 0;
		int[] ways = new int[s.length() + 1];
		ways[0] = 1;
		ways[1] = s.charAt(0) == '0' ? 0 : 1;
		for(int i = 2; i <= s.length(); i++){
			int lastOne = Integer.parseInt(s.substring(i - 1, i));
			int lastTwo = Integer.parseInt(s.substring(i - 2, i));
			if(lastOne >= 1 && lastOne <= 9) ways[i] = ways[i - 1];
			if(lastTwo >= 10 && lastTwo <= 26) ways[i] += ways[i - 2];
		}
		return ways[s.length()];
	}
	
	public static void main(String[] args) {
		DecodeWays decode = new DecodeWays();
		System.out.println(decode.numDecodings("226"));
		System.out.println(decode.numDecodings("12"));
		System.out.println(decode.numDecodings("122331112212321"));
	}

}
