import java.util.ArrayList;
import java.util.List;

public class NQueensII {
	public int totalNQueens(int n) {
		List<Integer> res = new ArrayList<Integer>();
		solve(res, n , 0, new boolean[3][2 * n]);
		return res.size();
	}
	private void solve(List<Integer> res, int n, int curr, boolean[][]flag){
		if(curr == n){
			res.add(n);
		}else{
			for(int i = 0; i < n; i++){
				if(!flag[0][i] && !flag[1][curr - i + n] && !flag[2][curr + i]){
					flag[0][i] = true;
					flag[1][curr - i + n] = true;
					flag[2][curr + i] = true;
					solve(res, n, curr + 1, flag);
					flag[0][i] = false;
					flag[1][curr - i + n] = false;
					flag[2][curr + i] = false;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		NQueensII queens = new NQueensII();
		System.out.println(queens.totalNQueens(8));
	}
}
