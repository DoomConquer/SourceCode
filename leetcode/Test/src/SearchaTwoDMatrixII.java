
public class SearchaTwoDMatrixII {

	public boolean searchMatrix(int[][] matrix, int target) {
		if(matrix == null || matrix.length == 0 || matrix[0].length == 0) return false;
		int row = matrix.length - 1, col = 0;
		while(row >= 0 && col < matrix[row].length){
			if(matrix[row][col] == target) return true;
			else if(matrix[row][col] > target) row--;
			else col++;
		}
		return false;
	}
	
	public static void main(String[] args) {
		SearchaTwoDMatrixII search = new SearchaTwoDMatrixII();
		System.out.println(search.searchMatrix(new int[][]{
			{1,   4,  7, 11, 15},
			{2,   5,  8, 12, 19},
			{3,   6,  9, 16, 22},
			{10, 13, 14, 17, 24},
			{18, 21, 23, 26, 30}
		}, 5));
	}

}
