package toutiao;

public class MaximalSquare {

    public int maximalSquare(char[][] matrix) {
    	if(matrix == null || matrix.length == 0) return 0;
        int[][] dp = new int[matrix.length][matrix[0].length];
        int max = 0;
        for(int i = 0; i < matrix.length; i++){
        	for(int j = 0; j < matrix[i].length; j++){
        		if(i == 0 || j == 0) {
        			if(matrix[i][j] == '1'){
        				dp[i][j] = 1; max = Math.max(max, dp[i][j] * dp[i][j]);
        			}
        			continue;
        		}
        		if(matrix[i][j] == '1'){
        			dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
        			max = Math.max(max, dp[i][j]);
        		}
        	}
        }
        return max * max;
    }
    
	public static void main(String[] args) {
		MaximalSquare maximalSquare = new MaximalSquare();
		System.out.println(maximalSquare.maximalSquare(new char[][]{{'1','0','1','0','0'},{'1','0','1','1','1'},{'1','1','1','1','1'},{'1','0','0','1','0'}}));
		System.out.println(maximalSquare.maximalSquare(new char[][]{{'1','1','1','1','1'},{'1','1','1','1','1'},{'1','1','1','1','1'},{'1','1','1','1','1'}}));
		System.out.println(maximalSquare.maximalSquare(new char[][]{{'1'}}));
		System.out.println(maximalSquare.maximalSquare(new char[][]{}));
		System.out.println(maximalSquare.maximalSquare(new char[][]{{'1','1','0','0','0'},{'1','1','0','0','0'},{'0','0','1','1','1'},{'1','1','1','1','1'},{'1','1','1','1','1'}}));
	}

}
