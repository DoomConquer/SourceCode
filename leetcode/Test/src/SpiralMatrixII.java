
public class SpiralMatrixII {

	public int[][] generateMatrix(int n) {
		if(n <= 0) return new int[][]{};
		int[][] res = new int[n][n];
		int left = 0, right = n -1, up = 0, down = n - 1;
		int num = 1;
		while(left <= right && up <= down){
			int i = left;
			while(i <= right) res[up][i++] = num++;
			if(up == down) break;
			i = up + 1;
			while(i <= down) res[i++][right] = num++;
			if(left == right) break;
			i = right - 1;
			while(i >= left) res[down][i--] = num++;
			i = down -1;
			while(i > up) res[i--][left] = num++;
			left++; right--; up++; down--;
		}
		return res;
	}
	
	public static void main(String[] args) {
		SpiralMatrixII spiral = new SpiralMatrixII();
		spiral.generateMatrix(10);
	}

}
