
public class NumberofIslands {

	public int numIslands(char[][] grid) {
		if(grid == null || grid.length == 0) return 0;
		int num = 0;
		int width = grid.length;
		int height = grid[0].length;
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++){
				if(grid[i][j] == '1') {
					num++;
					find(grid, width, height, i, j);
				}
			}
		return num;
	}
	private void find(char[][] grid, int width, int height, int x, int y){
		if(x < 0 || x >= width || y < 0 || y >= height) return;
		if(grid[x][y] != '1') return;
		grid[x][y] = '2';
		find(grid, width, height, x - 1, y);
		find(grid, width, height, x + 1, y);
		find(grid, width, height, x, y - 1);
		find(grid, width, height, x, y + 1);
	}
	
	public static void main(String[] args){
		NumberofIslands islands = new NumberofIslands();
		System.out.println(islands.numIslands(new char[][]{{'1','1','0','0','0'},{'1','1','0','0','0'},{'0','0','1','0','0'},{'0','0','0','1','1'}}));
	}

}
