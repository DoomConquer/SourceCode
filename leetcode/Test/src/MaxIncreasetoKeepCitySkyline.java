public class MaxIncreasetoKeepCitySkyline {

    public int maxIncreaseKeepingSkyline(int[][] grid) {
        int[] maxRow = new int[grid.length];
        int[] maxColumn = new int[grid[0].length];
        for(int i = 0; i < grid.length; i++){
        	for(int j = 0; j < grid[i].length; j++){
        		if(grid[i][j] > maxColumn[j]) maxColumn[j] = grid[i][j];
        		if(grid[i][j] > maxRow[i]) maxRow[i] = grid[i][j];
        	}
        }
        int sum = 0;
        for(int i = 0; i < grid.length; i++){
        	for(int j = 0; j < grid[i].length; j++){
        		sum += Math.min(maxRow[i], maxColumn[j]) - grid[i][j];
        	}
        }
        return sum;
    }
    
	public static void main(String[] args) {
		MaxIncreasetoKeepCitySkyline MaxIncreasetoKeepCitySkyline = new MaxIncreasetoKeepCitySkyline();
		System.out.println(MaxIncreasetoKeepCitySkyline.maxIncreaseKeepingSkyline(new int[][]{{3,0,8,4},{2,4,5,7},{9,2,6,3},{0,3,1,0}}));
		System.out.println(MaxIncreasetoKeepCitySkyline.maxIncreaseKeepingSkyline(new int[][]{{3,1},{2,0}}));
	}

}
