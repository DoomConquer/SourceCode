
public class IslandPerimeter {

	public int islandPerimeter(int[][] grid) {
		if(grid == null || grid.length == 0) return 0;
		int width = grid.length;
		int height = grid[0].length;
		int herizon = 0;
		int vertical = 0;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(i == 0 && grid[i][j] == 1) herizon++;
				if(i == width - 1 && grid[i][j] == 1) herizon++;
				if(j == 0 && grid[i][j] == 1) vertical++;
				if(j == height - 1 && grid[i][j] == 1) vertical++;
				if((i + 1 <= width - 1 && grid[i][j] == 0 && grid[i + 1][j] == 1) || (i + 1 <= width - 1 && grid[i][j] == 1 && grid[i + 1][j] == 0)) herizon++;
				if((j + 1 <= height - 1 && grid[i][j] == 0 && grid[i][j + 1] == 1) || (j + 1 <= height - 1 && grid[i][j] == 1 && grid[i][j + 1] == 0)) vertical++;
			}
		}
		return herizon + vertical;
	}
	
	public static void main(String[] args) {
		IslandPerimeter island = new IslandPerimeter();
		System.out.println(island.islandPerimeter(new int[][]{{0,1,0,0},{1,1,1,0},{0,1,0,0},{1,1,0,0}}));
		System.out.println(island.islandPerimeter(new int[][]{{0,0,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}}));
		System.out.println(island.islandPerimeter(new int[][]{{0,0,0,0},{0,1,0,0},{0,0,0,0},{0,0,0,0}}));
		System.out.println(island.islandPerimeter(new int[][]{{1}}));
	}

}
