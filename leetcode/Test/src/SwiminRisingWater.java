import java.util.Arrays;

public class SwiminRisingWater {

    public int swimInWater(int[][] grid) {
    	int[] water = new int[grid.length * grid[0].length];
    	int index = 0;
    	for(int[] g : grid){
    		for(int num : g) water[index++] = num;
    	}
    	Arrays.sort(water);
    	int left = 0, right = water.length - 1;
    	int res = Integer.MAX_VALUE;
    	while(left <= right){
    		int mid = left + (right - left) / 2;
    		if(dfs(grid, 0, 0, water[mid], new boolean[grid.length][grid[0].length])){
    			res = Math.min(res, water[mid]); 
    			right = mid - 1; 
    		}else left = mid + 1;
    	}
        return  res;
    }
    private boolean dfs(int[][] grid, int x, int y, int max, boolean[][] map){
    	int width = grid[0].length - 1, height = grid.length - 1;
    	if(x < 0 || x > width || y < 0 || y > height) return false;
    	if(x == width && y == height && grid[x][y] <= max) return true;
    	if(map[x][y]) return false;
    	map[x][y] = true;
    	if(grid[x][y] > max) return false;
    	boolean res = dfs(grid, x + 1, y, max, map) |
    	dfs(grid, x - 1, y, max, map) |
    	dfs(grid, x, y + 1, max, map) |
    	dfs(grid, x, y - 1, max, map);
    	return res;
    }
    
	public static void main(String[] args) {
		SwiminRisingWater swiminRisingWater = new SwiminRisingWater();
		System.out.println(swiminRisingWater.swimInWater(new int[][]{{0,2},{1,3}}));
		System.out.println(swiminRisingWater.swimInWater(new int[][]{{0,0},{0,0}}));
		System.out.println(swiminRisingWater.swimInWater(new int[][]{{0,0},{1,0}}));
		System.out.println(swiminRisingWater.swimInWater(new int[][]{{0,1,2,3,4},{24,23,22,21,5},{12,13,14,15,16},{11,17,18,19,20},{10,9,8,7,6}}));
		System.out.println(swiminRisingWater.swimInWater(new int[][]{{26,99,80,1,89,86,54,90,47,87},{9,59,61,49,14,55,77,3,83,79},{42,22,15,5,95,38,74,12,92,71},{58,40,64,62,24,85,30,6,96,52},{10,70,57,19,44,27,98,16,25,65},{13,0,76,32,29,45,28,69,53,41},{18,8,21,67,46,36,56,50,51,72},{39,78,48,63,68,91,34,4,11,31},{97,23,60,17,66,37,43,33,84,35},{75,88,82,20,7,73,2,94,93,81}}));
	}

}
