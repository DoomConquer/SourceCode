
public class WordSearch {

	public boolean exist(char[][] board, String word) {
		if(word == null || word.length() == 0) return true;
		for(int i = 0; i < board.length; i++)
			for(int j = 0; j < board[i].length; j++){
				if(board[i][j] == word.charAt(0)){
					boolean isExist = find(board, word, 0, i, j);
					if(isExist) return true;
				}
			}
		return false;
	}
	private boolean find(char[][] board, String word, int n, int i, int j){
		if(n == word.length()){
			return true;
		}
		if(i >= 0 && i < board.length && j >= 0 && j < board[i].length && board[i][j] == word.charAt(n)){
			board[i][j] = '*';
			boolean res = find(board, word, n + 1, i + 1, j) ||
					      find(board, word, n + 1, i - 1, j) ||
					      find(board, word, n + 1, i, j + 1) ||
					      find(board, word, n + 1, i, j - 1);
			board[i][j] = word.charAt(n);
			return res;
		}
		return false;
	}
	
	public static void main(String[] args) {
		WordSearch word = new WordSearch();
		System.out.println(word.exist(new char[][]{{'A','B','C','E'}, {'S','F','E','S'}, {'A','D','E','E'}}, "ABCESEEEFS"));
	}
}
