
public class UniquePathsII {

	public int uniquePathsWithObstacles(int[][] obstacleGrid) {
		if(obstacleGrid == null || obstacleGrid.length == 0) return 0;
		int width = obstacleGrid.length;
		int height = obstacleGrid[0].length;
		int[][] dp = new int[width][height];
		if(obstacleGrid[0][0] == 1 || obstacleGrid[width - 1][height - 1] == 1)
			return 0;
		dp[0][0] = 1;
		for(int i = 1; i < width; i++){
			if(obstacleGrid[i][0] == 1) dp[i][0] = 0;
			else dp[i][0] = dp[i - 1][0];
		}
		for(int i = 1; i < height; i++){
			if(obstacleGrid[0][i] == 1) dp[0][i] = 0;
			else dp[0][i] = dp[0][i -1];
		}
		for(int i = 1; i < width; i++){
			for(int j = 1; j < height; j++){
				if(obstacleGrid[i][j] == 1) dp[i][j] = 0;
				else dp[i][j] = dp[i - 1][j] + dp[i][j -1];
			}
		}
		return dp[width - 1][height - 1];
	}
	
	public static void main(String[] args) {
		UniquePathsII path = new UniquePathsII();
		System.out.println(path.uniquePathsWithObstacles(new int[][]{
			{0,0,0},
			{0,1,0},
			{0,0,0}
		}));
	}

}
