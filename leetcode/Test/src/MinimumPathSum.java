
public class MinimumPathSum {

	public int minPathSum(int[][] grid) {
		if(grid == null || grid.length == 0) return 0;
		int width = grid.length;
		int height = grid[0].length;
		int[][] dp = new int[width][height];
		dp[0][0] = grid[0][0];
		for(int i = 1; i < width; i++) dp[i][0] = dp[i - 1][0] + grid[i][0];
		for(int i = 1; i < height; i++) dp[0][i] = dp[0][i - 1] + grid[0][i];
		for(int i = 1; i < width; i++){
			for(int j = 1; j < height; j++){
				dp[i][j] = Math.min(dp[i - 1][j], dp[i][j - 1]) + grid[i][j];
			}
		}
		return dp[width - 1][height - 1];
	}
	
	public static void main(String[] args) {
		MinimumPathSum path = new MinimumPathSum();
		System.out.println(path.minPathSum(new int[][]{
		  {1,3,1},
		  {1,5,1},
		  {4,2,1}
		  }));
	}

}
