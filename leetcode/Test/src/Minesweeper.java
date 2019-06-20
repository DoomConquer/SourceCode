
public class Minesweeper {

	public char[][] updateBoard(char[][] board, int[] click) {
		if(board == null || board.length == 0 || board[0].length == 0) return board;
		int n = board.length;
		int m = board[0].length;
		if(board[click[0]][click[1]] == 'M'){
			board[click[0]][click[1]] = 'X';
			return board;
		}
		sweeper(board, click[0], click[1], n ,m);
		return board;
	}
	private void sweeper(char[][] board, int x, int y, int width, int height){
		if(x < 0 || x >= width || y < 0 || y >= height) return;
		if(board[x][y] == 'E'){
			int mine = 0;
			if(x - 1 >= 0 && y - 1 >= 0 && board[x - 1][y - 1] == 'M') mine++;
			if(x - 1 >= 0 && board[x - 1][y] == 'M') mine++;
			if(x - 1 >= 0 && y + 1 < height && board[x - 1][y + 1] == 'M') mine++;
			if(y - 1 >= 0 && board[x][y - 1] == 'M') mine++;
			if(y + 1 < height && board[x][y + 1] == 'M') mine++;
			if(x + 1 < width && y - 1 >= 0 && board[x + 1][y - 1] == 'M') mine++;
			if(x + 1 < width && board[x + 1][y] == 'M') mine++;
			if(x + 1 < width && y + 1 < height && board[x + 1][y + 1] == 'M') mine++;
			if(mine == 0){
				board[x][y] = 'B';
				sweeper(board, x - 1, y, width, height);
				sweeper(board, x + 1, y, width, height);
				sweeper(board, x, y - 1, width, height);
				sweeper(board, x, y + 1, width, height);
				
				sweeper(board, x - 1, y - 1, width, height);
				sweeper(board, x + 1, y - 1, width, height);
				sweeper(board, x - 1, y + 1, width, height);
				sweeper(board, x + 1, y + 1, width, height);
			}else board[x][y] = (char) (48 + mine);
		}
	}
	
	public static void main(String[] args) {
		Minesweeper mine = new Minesweeper();
		char[][] res = mine.updateBoard(new char[][]{{'E', 'E', 'E', 'E', 'E'},{'E', 'E', 'M', 'E', 'E'},{'E', 'M', 'E', 'E', 'E'},{'E', 'E', 'E', 'E', 'E'}}, new int[]{3,0});
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[0].length; j++)
				System.out.print(res[i][j] + " ");
			System.out.println();
		}
		res = mine.updateBoard(res, new int[]{1,2});
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[0].length; j++)
				System.out.print(res[i][j] + " ");
			System.out.println();
		}
	}

}
