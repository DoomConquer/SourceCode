import java.util.ArrayList;
import java.util.List;

/**
 * @author li_zhe
 * 自己思路
 * DP, dp[i][j] = Math.max(dp[i][j], Math.max(dp[i - l[0]][j - l[1]] + 1, Math.max(j > 0 ? dp[i][j - 1] : 0, i > 0 ? dp[i - 1][j] : 0)));
 * 不要比较dp[i][j - 1],dp[i - 1][j]
 */
public class OnesandZeroes {

	public int findMaxForm(String[] strs, int m, int n) {
		if(m == 0 && n == 0 || strs.length == 0) return 0;
		int[][] dp = new int[m + 1][n + 1];
		List<int[]> list = new ArrayList<>();
		for(String s : strs){
			int n0 = 0, n1 = 0;
			for(char ch : s.toCharArray()){
				if(ch == '0') n0++;
				else if(ch == '1') n1++;
			}
			list.add(new int[]{n0, n1});
		}
		
		for(int[] l : list){
			for(int i = m; i >= 0; i--){
				for(int j = n; j >= 0; j--){
					if(i >= l[0] && j >= l[1]){
						dp[i][j] = Math.max(dp[i][j], dp[i - l[0]][j - l[1]] + 1);
					}
				}
			}
		}
		return dp[m][n];
	}
	
	public static void main(String[] args) {
		OnesandZeroes ones = new OnesandZeroes();
		System.out.println(ones.findMaxForm(new String[]{"10", "0001", "111001", "1", "0"}, 5, 3));
		System.out.println(ones.findMaxForm(new String[]{"10", "1", "0"}, 1, 1));
		System.out.println(ones.findMaxForm(new String[]{"10", "1", "0"}, 1, 0));
	}

}
