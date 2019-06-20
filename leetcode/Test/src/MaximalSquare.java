public class MaximalSquare {

	public int maximalSquare(char[][] matrix) {
		if(matrix == null || matrix.length == 0) return 0;
		int rows = matrix.length;
		int cols = matrix[0].length;
		int max = 0;
		int[][] dp = new int[rows][cols];
		for(int i = 0; i < rows; i++) if(matrix[i][0] == '1'){ dp[i][0] = 1; max = 1; }
		for(int i = 0; i < cols; i++) if(matrix[0][i] == '1'){ dp[0][i] = 1; max = 1; }
		for(int i = 1; i < rows; i++){
			for(int j = 1; j < cols; j++){
				if(matrix[i][j] == '1'){
					if(dp[i - 1][j - 1] >= 1){
						int k = 1;
						while(k <= dp[i - 1][j - 1]){
							if(matrix[i - k][j] != '1' || matrix[i][j - k] != '1') break;
							k++;
						}
						if(k == 1) dp[i][j] = 1;
						else dp[i][j] = k;
					}else{
						dp[i][j] = 1;
					}
				}
				max = Math.max(max, dp[i][j]);
			}
		}
		return max * max;
	}
	
	public int maximalSquare1(char[][] matrix) {
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
		MaximalSquare max = new MaximalSquare();
		System.out.println(max.maximalSquare(new char[][]{
			{'1','0','1','0','0'},
			{'1','0','1','1','1'},
			{'1','1','1','1','1'},
			{'1','0','0','1','0'}
		}));
	}

}
