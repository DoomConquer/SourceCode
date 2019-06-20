import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SlidingPuzzle {

	class Pair{
		int x;
		int y;
		int[][] board;
		public Pair(int x, int y, int[][] board){
			this.x = x;
			this.y = y;
			this.board = new int[board.length][board[0].length];
			for(int i = 0; i < board.length; i++)
				for(int j = 0; j < board[i].length; j++)
					this.board[i][j] = board[i][j];
		}
	}
	public int slidingPuzzle(int[][] board) {
		if(board == null || board.length == 0 || board[0].length == 0) return -1;
		if(str(board).equals("123450")) return 0;
		Queue<Pair> queue = new LinkedList<>();
		Set<String> map = new HashSet<>();
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[i].length; j++){
				if(board[i][j] == 0){
					Pair pair = new Pair(i, j, board);
					queue.offer(pair);
					map.add(str(board));
					break;
				}
			}
		}
		int res = -1;
		int[] dx = new int[]{-1,1,0,0};
		int[] dy = new int[]{0,0,-1,1};
		while(!queue.isEmpty()){
			res++;
			int size = queue.size();
			for(int i = 0; i < size; i++){
				Pair p = queue.poll();
				if(str(p.board).equals("123450")) return res;
				int x = p.x;
				int y = p.y;
				board = p.board;
				for(int j = 0; j < 4; j++){
					int xx = x + dx[j];
					int yy = y + dy[j];
					if(xx >= 0 && xx < board.length && yy >= 0 && yy <board[0].length){
						board[x][y] = board[xx][yy];
						board[xx][yy] = 0;
						if(!map.contains(str(board))){
							Pair newPair = new Pair(xx, yy, board);
							queue.offer(newPair);
							map.add(str(board));
						}
						board[xx][yy] = board[x][y];
						board[x][y] = 0;
					}
				}
			}
		}
		return -1;
	}
	private String str(int[][] board){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < board.length; i++)
			for(int j = 0; j < board[0].length; j++)
				sb.append(board[i][j]);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		SlidingPuzzle puzzle = new SlidingPuzzle();
		System.out.println(puzzle.slidingPuzzle(new int[][]{{4,1,2},{5,0,3}}));
		System.out.println(puzzle.slidingPuzzle(new int[][]{{1,2,3},{4,0,5}}));
		System.out.println(puzzle.slidingPuzzle(new int[][]{{3,2,4},{1,5,0}}));
	}

}
