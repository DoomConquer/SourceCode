
/**
 * @author li_zhe
 * 参考leetcode解题思路
 * 一个标记第一列是否有0
 */
public class SetMatrixZeroes {

	public void setZeroes(int[][] matrix) {
		boolean firstColZeroFlag = false;
		for(int i = 0; i < matrix.length; i++){
			if(matrix[i][0] == 0) firstColZeroFlag = true;
			for(int j = 1; j < matrix[i].length; j++){
				if(matrix[i][j] == 0){
					matrix[i][0] = 0;
					matrix[0][j] = 0;
				}
			}
		}
		for(int i = matrix.length - 1; i >= 0; i--){
			for(int j = matrix[i].length - 1; j >= 1; j--){
				if(matrix[i][0] == 0 || matrix[0][j] == 0) matrix[i][j] = 0;
			}
			if(firstColZeroFlag){
				matrix[i][0] = 0;
			}
		}
	}
	
	public static void main(String[] args) {
		SetMatrixZeroes set = new SetMatrixZeroes();
		set.setZeroes(new int[][]{
			  {1,1,1},
			  {1,0,1},
			  {1,1,1}
			});
	}

}
