
public class SearchaTwoDMatrix {

	public boolean searchMatrix(int[][] matrix, int target) {
		if(matrix == null || matrix.length == 0 || matrix[0].length == 0) return false;
		int left = 0, right = matrix.length - 1;
		while(left <= right){
			int mid = (left + right) >> 1;
			if(matrix[mid][0] == target) return true;
			else if(matrix[mid][0] > target) right = mid - 1;
			else left = mid + 1;
		}
		if(left > 0)
			left--;
		int row = left;
		left = 0; right = matrix[row].length - 1;
		while(left <= right){
			int mid = (left + right) >> 1;
			if(matrix[row][mid] == target) return true;
			else if(matrix[row][mid] > target) right = mid - 1;
			else left = mid + 1;
		}
		return false;
	}
	
	public static void main(String[] args) {
		SearchaTwoDMatrix search = new SearchaTwoDMatrix();
		System.out.println(search.searchMatrix(new int[][]{
			{1,   3,  5,  7},
			{10, 11, 16, 20},
			{23, 30, 34, 50}
		}, 20));
	}

}
