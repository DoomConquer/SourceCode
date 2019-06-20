
public class BricksFallingWhenHit {

	public int[] hitBricks(int[][] grid, int[][] hits) {
		int width = grid.length;
		int height = grid[0].length;
		for(int i = 0; i < hits.length; i++){
			if(grid[hits[i][0]][hits[i][1]] == 1)
				grid[hits[i][0]][hits[i][1]] = -1;
		}
		unionFind(width * height);
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(grid[i][j] == 1){
					unionAdjacent(i, j, width, height, grid);
				}
			}
		}
		int currCount = count[find(0)];
		int[] res = new int[hits.length];
		for(int i = hits.length - 1; i >= 0; i--){
			if(grid[hits[i][0]][hits[i][1]] == -1){
				unionAdjacent(hits[i][0], hits[i][1], width, height, grid);
				grid[hits[i][0]][hits[i][1]] = 1;
			}
			int afterCount = count[find(0)];
			res[i] = (afterCount - currCount > 0) ? afterCount - currCount - 1 : 0;
			currCount = afterCount;
		}
		return res;
	}
	int[] parent;
	int[] count;
	private void unionFind(int n){
		parent = new int[n];
		count = new int[n];
		for(int i = 0; i < n; i++){
			parent[i] = i;
			count[i] = 1;
		}
	}
	private int find(int x){
		while(x != parent[x]){
			x = parent[parent[x]];
		}
		return x;
	}
	private void union(int x, int y){
		int xx = find(x);
		int yy = find(y);
		if(xx == yy) return;
		parent[xx] = yy;
		count[yy] += count[xx];
	}
	int[] dirX = new int[]{-1, 1, 0, 0};
	int[] dirY = new int[]{0, 0, -1, 1};
	private void unionAdjacent(int x, int y, int width, int height, int[][] grid){
		for(int i = 0; i < 4; i++){
			int dx = x + dirX[i];
			int dy = y + dirY[i];
			if(dx < 0 || dx >= width || dy < 0 || dy >= height) continue;
			if(grid[dx][dy] == 1){
				union(x * height + y, dx * height + dy);
			}
		}
		if(x == 0){
			union(x * height + y, 0);
		}
	}
	
	public static void main(String[] args) {
		BricksFallingWhenHit brick = new BricksFallingWhenHit();
		int[] res = brick.hitBricks(new int[][]{{1,0,0,0},{1,1,0,0}}, new int[][]{{1,1},{1,0}});
		for(int num : res) System.out.print(num + "  ");
		System.out.println();
		res = brick.hitBricks(new int[][]{{1,0,0,0},{1,1,1,0}}, new int[][]{{1,0}});
		for(int num : res) System.out.print(num + "  ");
		System.out.println();
		res = brick.hitBricks(new int[][]{{1},{1},{1},{1},{1}}, new int[][]{{3,0},{4,0},{1,0},{2,0},{0,0}});
		for(int num : res) System.out.print(num + "  ");
		System.out.println();
		res = brick.hitBricks(new int[][]{{1,0,1},{1,1,1}}, new int[][]{{0,0},{0,2},{1,1}});
		for(int num : res) System.out.print(num + "  ");
	}

}
