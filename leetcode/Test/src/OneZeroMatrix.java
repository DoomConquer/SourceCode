import java.util.LinkedList;
import java.util.Queue;

public class OneZeroMatrix {

	class Pair{
		int x;
		int y;
		int dist;
		public Pair(int x, int y, int dist){
			this.x = x;
			this.y = y;
			this.dist = dist;
		}
	}
	public int[][] updateMatrix(int[][] matrix) {
		if(matrix == null || matrix.length == 0 || matrix[0].length == 0) return matrix;
		int n = matrix.length;
		int m = matrix[0].length;
		Queue<Pair> queue = new LinkedList<>();
		for(int i = 0; i < n; i++)
			for(int j = 0; j < m; j++){
				if(matrix[i][j] == 1) matrix[i][j] = -1;
				else {
					Pair pair = new Pair(i, j, 0);
					queue.offer(pair);
				}
			}
		while(!queue.isEmpty()){
			int size = queue.size();
			for(int i = 0; i < size; i++){
				Pair pair = queue.poll();
				int x = pair.x;
				int y = pair.y;
				int dist = pair.dist;
				if(x - 1 >= 0 && matrix[x - 1][y] == -1){
					matrix[x - 1][y] = dist + 1;
					Pair newPair = new Pair(x - 1, y, dist + 1);
					queue.offer(newPair);
				}
				if(x + 1 < n && matrix[x + 1][y] == -1){
					matrix[x + 1][y] = dist + 1;
					Pair newPair = new Pair(x + 1, y, dist + 1);
					queue.offer(newPair);
				}
				if(y - 1 >= 0 && matrix[x][y - 1] == -1){
					matrix[x][y - 1] = dist + 1;
					Pair newPair = new Pair(x, y - 1, dist + 1);
					queue.offer(newPair);
				}
				if(y + 1 < m && matrix[x][y + 1] == -1){
					matrix[x][y + 1] = dist + 1;
					Pair newPair = new Pair(x, y + 1, dist + 1);
					queue.offer(newPair);
				}
			}
		}
		return matrix;
	}
	
	public static void main(String[] args) {
		OneZeroMatrix matrix = new OneZeroMatrix();
		int[][] res = matrix.updateMatrix(new int[][]{{0,1,0},{1,1,1},{1,1,1}});
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[0].length; j++)
				System.out.print(res[i][j] + " ");
			System.out.println();
		}
	}

}
