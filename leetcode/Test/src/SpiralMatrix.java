import java.util.ArrayList;
import java.util.List;

public class SpiralMatrix {

	public List<Integer> spiralOrder(int[][] matrix) {
		List<Integer> res = new ArrayList<>();
		if(matrix == null || matrix.length == 0) return res;
		int m = matrix.length;
		int n = matrix[0].length;
		int left = 0, right = n -1, up = 0, down = m - 1;
		while(left <= right && up <= down){
			int i = left;
			while(i <= right) res.add(matrix[up][i++]);
			if(up == down) break;
			i = up + 1;
			while(i <= down) res.add(matrix[i++][right]);
			if(left == right) break;
			i = right - 1;
			while(i >= left) res.add(matrix[down][i--]);
			i = down -1;
			while(i > up) res.add(matrix[i--][left]);
			left++; right--; up++; down--;
		}
		return res;
	}
	
	public static void main(String[] args) {
		SpiralMatrix spiral = new SpiralMatrix();
		System.out.println(spiral.spiralOrder(new int[][]{
			{1, 2, 3, 4},
			{5, 6, 7, 8},
			{9,10,11,12}
		}));
	}

}
