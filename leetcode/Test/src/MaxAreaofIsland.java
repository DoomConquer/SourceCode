
public class MaxAreaofIsland {

	public int maxAreaOfIsland(int[][] grid) {
		if(grid == null || grid.length == 0) return 0;
		int width = grid.length;
		int height = grid[0].length;
		int max = 0;
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				if(grid[i][j] == 1){
					max = Math.max(max, maxArea(grid, width, height, i, j));
				}
		return max;
	}
	private int maxArea(int[][] grid, int width, int height, int x, int y){
		if(x < 0 || x >= width || y < 0 || y >= height) return 0;
		if(grid[x][y] == 0) return 0;
		grid[x][y] = 0;
		int area = 1;
		area += maxArea(grid, width, height, x - 1, y) 
			 + maxArea(grid, width, height, x + 1, y)
			 + maxArea(grid, width, height, x, y - 1)
			 + maxArea(grid, width, height, x, y + 1);
		return area;
	}
	
	public static void main(String[] args) {
		MaxAreaofIsland max = new MaxAreaofIsland();
		System.out.println(max.maxAreaOfIsland(new int[][]{{0,0,1,0,0,0,0,1,0,0,0,0,0},{0,0,0,0,0,0,0,1,1,1,0,0,0},{0,1,1,0,1,0,0,0,0,0,0,0,0},{0,1,0,0,1,1,0,0,1,0,1,0,0},{0,1,0,0,1,1,0,0,1,1,1,0,0},{0,0,0,0,0,0,0,0,0,0,1,0,0},{0,0,0,0,0,0,0,1,1,1,0,0,0},{0,0,0,0,0,0,0,1,1,0,0,0,0}}));
		System.out.println(max.maxAreaOfIsland(new int[][]{{0,1,0,1,1,0,0,0}}));
	}

}
