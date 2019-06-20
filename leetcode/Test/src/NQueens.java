import java.util.ArrayList;
import java.util.List;

public class NQueens {

	public List<List<String>> solveNQueens(int n) {
		List<List<String>> res = new ArrayList<List<String>>();
		solve(res, new ArrayList<Integer>(), n, 0, new boolean[3][2 * n]);
		return res;
	}
	private void solve(List<List<String>> res, List<Integer> one, int n, int curr, boolean[][]flag){
		if(one.size() == n){
			List<String> lines = new ArrayList<String>();
			for(int i : one){
				StringBuffer sb = new StringBuffer();
				for(int j = 0; j < n; j++){
					if(j == i){
						sb.append("Q");
					}else{
						sb.append(".");
					}
				}
				lines.add(sb.toString());
			}
			res.add(lines);
		}else{
			for(int i = 0; i < n; i++){
				if(!flag[0][i] && !flag[1][curr - i + n] && !flag[2][curr + i]){
					one.add(i);
					flag[0][i] = true;
					flag[1][curr - i + n] = true;
					flag[2][curr + i] = true;
					solve(res, one, n, curr + 1, flag);
					flag[0][i] = false;
					flag[1][curr - i + n] = false;
					flag[2][curr + i] = false;
					one.remove(one.size() - 1);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		NQueens queens = new NQueens();
		System.out.println(queens.solveNQueens(8));
	}

}
