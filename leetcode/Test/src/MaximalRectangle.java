import java.util.Stack;

public class MaximalRectangle {

	public int maximalRectangle(char[][] matrix) {
		if(matrix == null || matrix.length == 0) return 0;
		int rows = matrix.length;
		int cols = matrix[0].length;
		int[] row = new int[cols];
		int max = 0;
		Stack<Integer> stack = new Stack<>();
		for(int i = 0; i < rows; i++){
			int currMax = 0;
			stack.clear();
			for(int j = 0; j < cols; j++){
				if(matrix[i][j] == '1') row[j] += 1; 
				else row[j] = 0;
			}
			int k = 0;
			while(k <= cols){
				int curr = k == cols ? 0 : row[k];
				if(stack.isEmpty() || curr >= row[stack.peek()]) stack.push(k);
				else{
					int top = stack.pop();
					currMax = Math.max(currMax, row[top] * (stack.isEmpty() ? k : k - stack.peek() - 1));
					k--;
				}
				k++;
			}
			max = Math.max(max, currMax);
		}
		return max;
	}
	
	public static void main(String[] args) {
		MaximalRectangle max = new MaximalRectangle();
		System.out.println(max.maximalRectangle(new char[][]{
			{'1','0','1','0','0'},
			{'1','0','1','1','1'},
			{'1','1','1','1','1'},
			{'1','0','0','1','0'}
		}));
	}

}
