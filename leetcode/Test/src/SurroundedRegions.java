
public class SurroundedRegions {

	public void solve(char[][] board) {
		if(board == null || board.length == 0) return;
		int width = board.length;
		int height = board[0].length;
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++){
				if(i == 0 || j == 0 || i == width - 1 || j == height - 1){
					if(board[i][j] == 'O') flipping(board, width, height, i, j);
				}
			}
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(board[i][j] == 'O') board[i][j] = 'X';
				if(board[i][j] == 'o') board[i][j] = 'O';
				System.out.print(board[i][j] + "  ");
			}
			if(i < width - 1) System.out.print("\n");
		}
	}
	private void flipping(char[][] board, int width, int height, int x, int y){
		if(x < 0 || x >= width || y < 0 || y >= height) return;
		if(board[x][y] != 'O') return;
		board[x][y] = 'o';
		flipping(board, width, height, x - 1, y);
		flipping(board, width, height, x + 1, y);
		flipping(board, width, height, x, y - 1);
		flipping(board, width, height, x, y + 1);
	}
	
	public static void main(String[] args) {
		SurroundedRegions region = new SurroundedRegions();
		region.solve(new char[][]{{'X','X','X','X'},{'X','O','O','X'},{'X','X','O','X'},{'X','O','X','X'}});
	}

}
