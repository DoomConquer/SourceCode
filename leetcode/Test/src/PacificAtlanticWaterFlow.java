import java.util.ArrayList;
import java.util.List;

public class PacificAtlanticWaterFlow {

	public List<int[]> pacificAtlantic(int[][] matrix) {
		List<int[]> res = new ArrayList<>();
		if(matrix == null || matrix.length == 0 || matrix[0].length == 0) return res;
		int n = matrix.length;
		int m = matrix[0].length;
		boolean[][] pacific = new boolean[n][m];
		boolean[][] atlantic = new boolean[n][m];
			
		for(int i = 0; i < n; i++){
			flow(matrix, pacific, i, 0, matrix[i][0]);
			flow(matrix, atlantic, i, m - 1, matrix[i][m - 1]);
		}
		for(int i = 0; i < m; i++){
			flow(matrix, pacific, 0, i, matrix[0][i]);
			flow(matrix, atlantic, n - 1, i, matrix[n - 1][i]);
		}
		for(int i = 0; i < n; i++)
			for(int j = 0; j < m; j++){
				if(pacific[i][j] && atlantic[i][j]) res.add(new int[]{i, j});
			}
		return res;
	}
	private void flow(int[][] matrix, boolean[][] flag, int x, int y, int height){
		if(x < 0 || y < 0 || x >= matrix.length || y >= matrix[0].length) return;
		if(flag[x][y] || matrix[x][y] < height) return;
		flag[x][y] = true;
		flow(matrix, flag, x - 1, y, matrix[x][y]);
		flow(matrix, flag, x + 1, y, matrix[x][y]);
		flow(matrix, flag, x, y - 1, matrix[x][y]);
		flow(matrix, flag, x, y + 1, matrix[x][y]);
	}
	
	public static void main(String[] args) {
		PacificAtlanticWaterFlow water = new PacificAtlanticWaterFlow();
		List<int[]> res = water.pacificAtlantic(new int[][]{{1,2,2,3,5},{3,2,3,4,4},{2,4,5,3,1},{6,7,1,4,5},{5,1,1,2,4}});
		for(int[] nums : res){
			for(int num : nums)
				System.out.print(num + "  ");
			System.out.println();
		}
		res = water.pacificAtlantic(new int[][]{{1,1},{1,1},{1,1}});
		for(int[] nums : res){
			for(int num : nums)
				System.out.print(num + "  ");
			System.out.println();
		}
	}

}
