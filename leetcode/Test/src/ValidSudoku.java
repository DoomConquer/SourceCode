import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValidSudoku {

	public boolean isValidSudoku(char[][] board) {
		Map<Integer, Set<Character>> row = new HashMap<>();
		Map<Integer, Set<Character>> col = new HashMap<>();
		Map<Integer, Set<Character>> grid = new HashMap<>();
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[i].length; j++){
				if(board[i][j] == '.') continue;
				if(check(row, board[i][j], i) || check(col, board[i][j], j) || check(grid, board[i][j], (i/3) * 3 + j/3)) return false;
			}
		}
		return true;
	}
	private boolean check(Map<Integer, Set<Character>> map, char ch, int key){
		Set<Character> set = map.getOrDefault(key, new HashSet<Character>());
		if(set.contains(ch)) return true;
		else {
			set.add(ch);
			map.put(key, set);
		}
		return false;
	}
	
	
	public static void main(String[] args) {
		ValidSudoku valid = new ValidSudoku();
		System.out.println(valid.isValidSudoku(new char[][]{
			  {'5','3','.','.','7','.','.','.','.'},
			  {'6','.','.','1','9','5','.','.','.'},
			  {'.','9','8','.','.','.','.','6','.'},
			  {'8','.','.','.','6','.','.','.','3'},
			  {'4','.','.','8','.','3','.','.','1'},
			  {'7','.','.','.','2','.','.','.','6'},
			  {'.','6','.','.','.','.','2','8','.'},
			  {'.','.','.','4','1','9','.','.','5'},
			  {'.','.','.','.','8','.','.','7','9'}
			}));
		System.out.println(valid.isValidSudoku(new char[][]{
			  {'8','3','.','.','7','.','.','.','.'},
			  {'6','.','.','1','9','5','.','.','.'},
			  {'.','9','8','.','.','.','.','6','.'},
			  {'8','.','.','.','6','.','.','.','3'},
			  {'4','.','.','8','.','3','.','.','1'},
			  {'7','.','.','.','2','.','.','.','6'},
			  {'.','6','.','.','.','.','2','8','.'},
			  {'.','.','.','4','1','9','.','.','5'},
			  {'.','.','.','.','8','.','.','7','9'}
			}));
	}

}
