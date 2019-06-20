import java.util.Arrays;

// 参考leetcode思路，两个同时从走上角走到右下角，dp[k][i][j]表示走了k步，第一个的横坐标为i，第二个的横坐标为j的樱桃数值
// 时间复杂度O(n^3)，空间复杂度O(n^2)
public class CherryPickup {

    public int cherryPickup(int[][] grid) {
    	int N = grid.length;
        int[][][] dp = new int[2][N][N];
        dp[0][0][0] = grid[0][0];
        for(int k = 1; k <= 2 * N - 2; k++){
        	for(int i = 0; i <= k && i < N; i++){
        		Arrays.fill(dp[k & 1][i], -1);
        		if(k - i >= N || grid[i][k - i] == -1) continue;
        		for(int j = 0; j <= k && j < N; j++){
        			if(k - j >= N || grid[j][k - j] == -1) continue;
        			dp[k & 1][i][j] = Math.max(
        					Math.max(
        							(k - i - 1 >= 0 && k - j - 1 >= 0) ? dp[(k - 1) & 1][i][j] : -1, 
        							(j > 0 && k - i - 1 >= 0) ? dp[(k - 1) & 1][i][j - 1] : -1),
        					Math.max(
        							(i > 0 && k - j - 1 >= 0) ? dp[(k - 1) & 1][i - 1][j] : -1, 
									(i > 0 && j > 0) ? dp[(k - 1) & 1][i - 1][j - 1] : -1));
        			
        			if(dp[k & 1][i][j] == -1) continue;
        			if(i == j){
        				dp[k & 1][i][j] += grid[i][k - i];
        			}else{
        				dp[k & 1][i][j] += grid[i][k - i] + grid[j][k - j];
        			}
        		}
        	}
        }
        int res = dp[(2 * N - 2) & 1][N - 1][N - 1];
        return res == -1 ? 0 : res;
    }
    
	public static void main(String[] args) {
		CherryPickup cherryPickup = new CherryPickup();
		System.out.println(cherryPickup.cherryPickup(new int[][]{{0, 1, -1},{1, 0, -1},{1, 1, 1}}));
		System.out.println(cherryPickup.cherryPickup(new int[][]{{1, 1, -1},{1, -1, 1},{-1, 1, 1}}));
		System.out.println(cherryPickup.cherryPickup(new int[][]{{1, 1, -1},{1, 0, -1},{1, 1, 1}}));
		System.out.println(cherryPickup.cherryPickup(new int[][]{{0, -1, -1},{-1, 0, -1},{1, 1, 1}}));
		System.out.println(cherryPickup.cherryPickup(new int[][]{{1}}));
		System.out.println(cherryPickup.cherryPickup(new int[][]{{1,1,1,1,0,0,0},{0,0,0,1,0,0,0},{0,0,0,1,0,0,1},{1,0,0,1,0,0,0},{0,0,0,1,0,0,0},{0,0,0,1,0,0,0},{0,0,0,1,1,1,1}}));
	}

}
