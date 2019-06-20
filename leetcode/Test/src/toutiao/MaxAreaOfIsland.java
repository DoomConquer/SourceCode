package toutiao;

public class MaxAreaOfIsland {

    public int maxAreaOfIsland(int[][] grid) {
    	int max = 0;
        for(int i = 0; i < grid.length; i++){
        	for(int j = 0; j < grid[0].length; j++){
        		if(grid[i][j] == 1) max = Math.max(max, count(grid, i, j));
        	}
        }
        return max;
    }
    private int count(int[][] grid, int x, int y){
    	if(x < 0 || x >= grid.length || y < 0 || y >= grid[0].length) return 0;
    	if(grid[x][y] != 1) return 0;
    	grid[x][y] = 0;
    	int count = 1 + count(grid, x + 1, y) + count(grid, x - 1, y) + count(grid, x, y + 1) + count(grid, x, y - 1);
    	return count;
    }
    
	public static void main(String[] args) {
		MaxAreaOfIsland maxAreaOfIsland = new MaxAreaOfIsland();
		System.out.println(maxAreaOfIsland.maxAreaOfIsland(new int[][]{{0,0,0,0,0,0,0,0}}));
		System.out.println(maxAreaOfIsland.maxAreaOfIsland(new int[][]{{0,0,1,0,0,0,0,1,0,0,0,0,0},{0,0,0,0,0,0,0,1,1,1,0,0,0},{0,1,1,0,1,0,0,0,0,0,0,0,0},{0,1,0,0,1,1,0,0,1,0,1,0,0},{0,1,0,0,1,1,0,0,1,1,1,0,0},{0,0,0,0,0,0,0,0,0,0,1,0,0},{0,0,0,0,0,0,0,1,1,1,0,0,0},{0,0,0,0,0,0,0,1,1,0,0,0,0}}));
	}

}
