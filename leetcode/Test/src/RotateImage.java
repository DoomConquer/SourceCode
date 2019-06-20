
public class RotateImage {

	public void rotate(int[][] matrix) {
		if(matrix == null || matrix.length == 0) return;
		int n = matrix.length;
		int start = 0, end = n -1;
		while(start < end){
			int left = start, right = end, up = start, down = end;
			while(left < end && right > start && up < end && down > start){
				int temp = matrix[start][left];
				matrix[start][left] = matrix[down][start];
				matrix[down][start] = matrix[end][right];
				matrix[end][right] = matrix[up][end];
				matrix[up][end] = temp;
				left++; right--; up++; down--;
			}
			start++; end--;
		}
	}
	
	public static void main(String[] args) {
		RotateImage image = new RotateImage();
		image.rotate(new int[][]{
			{5, 1, 9,11},
			{2, 4, 8,10},
			{13, 3, 6, 7},
			{15,14,12,16}
		});
	}

}
