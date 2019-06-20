
public class GameofLife {

	public void gameOfLife(int[][] board) {
		if(board == null || board.length == 0) return;
		int[] dirX = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
		int[] dirY = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};
		int width = board.length;
		int height = board[0].length;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				int live = 0;
				for(int k = 0; k < 8; k++){
					int x = i + dirX[k];
					int y = j + dirY[k];
					if(x < 0 || x >= width || y < 0 || y >= height) continue;
					if(board[x][y] == 1 || board[x][y] == -1) live++;
				}
				if(board[i][j] == 1){
					if(live < 2 || live > 3) board[i][j] = -1;
				}else{
					if(live == 3) board[i][j] = -2;
				}
			}
		}
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(board[i][j] == -1) board[i][j] = 0;
				else if(board[i][j] == -2) board[i][j] = 1;
			}
		}
	}
	
	private static void print(int[][] board){
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[i].length; j++)
				System.out.print(board[i][j] + " ");
			System.out.println();
		}
	}
	public static void main(String[] args) {
		GameofLife life = new GameofLife();
		int[][] res = new int[][]{
			{0,1,0},
			{0,0,1},
			{1,1,1},
			{0,0,0}
		};
		life.gameOfLife(res);
		print(res);
	}

}
