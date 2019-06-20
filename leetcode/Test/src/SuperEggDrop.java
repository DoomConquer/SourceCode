// dp思路，dp[n][h]表示h层楼用n个鸡蛋最少需要多少次能测试出来
public class SuperEggDrop {

    public int superEggDrop(int K, int N) {
        int[][] dp = new int[K + 1][N + 1];
        for(int n = 1; n <= K; n++){
        	for(int h = 1; h <= N; h++){
        		if(n == 1){
            		dp[n][h] = h; continue;
            	}
	    		int min = Integer.MAX_VALUE;
	    		for(int i = 1; i <= h; i++){
	    			int curr = Math.max(dp[n - 1][i - 1], dp[n][h - i]) + 1;
	    			min = Math.min(min, curr);
	    		}
	    		dp[n][h] = min;
    		}
    	}
        return dp[K][N];
    }
    
    public static void main(String[] args) {
    	SuperEggDrop superEggDrop = new SuperEggDrop();
    	System.out.println(superEggDrop.superEggDrop(1, 2));
    	System.out.println(superEggDrop.superEggDrop(2, 6));
    	System.out.println(superEggDrop.superEggDrop(3, 14));
    	System.out.println(superEggDrop.superEggDrop(2, 100));
	}
}
