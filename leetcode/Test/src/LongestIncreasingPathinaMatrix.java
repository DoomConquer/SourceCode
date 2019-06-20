
public class LongestIncreasingPathinaMatrix {

	public int longestIncreasingPath(int[][] matrix) {
		if(matrix == null || matrix.length == 0) return 0;
		int width = matrix.length;
		int height = matrix[0].length;
		int max = 0;
		int[][] flag = new int[width][height];
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				max = Math.max(max, find(matrix, i, j, width, height, flag));
			}
		}
		return max;
	}
	int[] dirX = new int[]{-1,1,0,0};
	int[] dirY = new int[]{0,0,-1,1};
	private int find(int[][] matrix, int x, int y, int width, int height, int[][] flag){
		if(flag[x][y] > 0) return flag[x][y];
		int maxLen = 0;
		for(int i = 0; i < 4; i++){
			int dx = x + dirX[i];
			int dy = y + dirY[i];
			if(dx < 0 || dx >= width || dy < 0 || dy >= height) continue;
			if(matrix[x][y] < matrix[dx][dy]){
				maxLen = Math.max(maxLen, find(matrix, dx, dy, width, height, flag));
			}
		}
		flag[x][y] = 1 + maxLen;
		return flag[x][y];
	}
	
	public static void main(String[] args) {
		LongestIncreasingPathinaMatrix matrix = new LongestIncreasingPathinaMatrix();
		System.out.println(matrix.longestIncreasingPath(new int[][]{{9,9,4},{6,6,8},{2,1,1}}));
		System.out.println(matrix.longestIncreasingPath(new int[][]{{3,4,5},{3,2,6},{2,2,1}}));
	}

}
