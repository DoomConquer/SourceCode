import java.util.Arrays;
import java.util.Comparator;

public class RussianDollEnvelopes {

	// 自己的思路，但是时间复杂度为O(n^2)
	class Envelope{
		int w, h;
		public Envelope(int w, int h){
			this.w = w; this.h = h;
		}
	}
    public int maxEnvelopes(int[][] envelopes) {
        if(envelopes == null || envelopes.length == 0) return 0;
        int len = envelopes.length;
        Envelope[] envs = new Envelope[len];
        for(int i = 0; i < len; i++) envs[i] = new Envelope(envelopes[i][0], envelopes[i][1]);
        Arrays.sort(envs, (Envelope o1, Envelope o2) -> { return o1.w == o2.w ? o1.h - o2.h : o1.w - o2.w; });
        int[] dp = new int[len]; Arrays.fill(dp, 1);
        int max = 1;
        for(int i = 0; i < len - 1; i++){
        	for(int j = i + 1; j < len; j++){
        		if(envs[i].w < envs[j].w && envs[i].h < envs[j].h) dp[j] = Math.max(dp[j], dp[i] + 1);
        		max = Math.max(max, dp[j]);
        	}
        }
        return max;
    }
    
    // 根据w升序，h降序排序，这样不用考虑w（例如[3,3],[3,4]排序后为[3,4],[3,3]，求LIS时就会排除计算），将问题转化为求h的最长递增子序列（LIS），时间复杂度O(n*logn)
    public int maxEnvelopes1(int[][] envelopes) {
        if(envelopes == null || envelopes.length == 0 
           || envelopes[0] == null || envelopes[0].length != 2)
            return 0;
        Arrays.sort(envelopes, new Comparator<int[]>(){
            public int compare(int[] arr1, int[] arr2){
                if(arr1[0] == arr2[0])
                    return arr2[1] - arr1[1];
                else
                    return arr1[0] - arr2[0];
           } 
        });
        int dp[] = new int[envelopes.length];
        int len = 0;
        for(int[] envelope : envelopes){
            int index = Arrays.binarySearch(dp, 0, len, envelope[1]);
            if(index < 0)
                index = -(index + 1);
            dp[index] = envelope[1];
            if(index == len)
                len++;
        }
        return len;
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
