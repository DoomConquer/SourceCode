package toutiao;

import java.util.Arrays;

public class RussianDollEnvelopes {

	public int maxEnvelopes(int[][] envelopes) {
        if(envelopes == null || envelopes.length == 0) return 0;
        int len = envelopes.length;
        Arrays.sort(envelopes, (int[] o1, int[] o2) -> { return o1[0] == o2[0] ? o1[1] - o2[1] : o1[0] - o2[0]; });
        int[] dp = new int[len]; Arrays.fill(dp, 1);
        int max = 1;
        for(int i = 0; i < len - 1; i++){
        	for(int j = i + 1; j < len; j++){
        		if(envelopes[i][0] < envelopes[j][0] && envelopes[i][1] < envelopes[j][1]) dp[j] = Math.max(dp[j], dp[i] + 1);
        		max = Math.max(max, dp[j]);
        	}
        }
        return max;
    }
	
	public static void main(String[] args) {
		RussianDollEnvelopes russianDollEnvelopes = new RussianDollEnvelopes();
		System.out.println(russianDollEnvelopes.maxEnvelopes(new int[][]{{5,4},{6,4},{6,7},{2,3}}));
		System.out.println(russianDollEnvelopes.maxEnvelopes(new int[][]{{5,4}}));
		System.out.println(russianDollEnvelopes.maxEnvelopes(new int[][]{{5,4},{5,1}}));
		System.out.println(russianDollEnvelopes.maxEnvelopes(new int[][]{{5,4},{5,1},{1,1}}));
		System.out.println(russianDollEnvelopes.maxEnvelopes(new int[][]{{5,4},{5,10},{4,7}}));
		System.out.println(russianDollEnvelopes.maxEnvelopes(new int[][]{{46,89},{50,53},{52,68},{72,45},{77,81}}));
	}
}
