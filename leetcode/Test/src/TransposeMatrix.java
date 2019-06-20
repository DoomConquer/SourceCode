public class TransposeMatrix {

    public int[][] transpose(int[][] A) {
    	if(A == null || A.length == 0) return A;
    	int width = A.length, height = A[0].length;
    	int[][] res = new int[height][width];
    	for(int i = 0; i < height; i++){
    		for(int j = 0; j < width; j++)
    			res[i][j] = A[j][i];
    	}
    	return res;
    }
    
	public static void main(String[] args) {
		TransposeMatrix transposeMatrix = new TransposeMatrix();
		int[][] res = transposeMatrix.transpose(new int[][]{{1,2,3},{4,5,6},{7,8,9}});
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[i].length; j++)
				System.out.print(res[i][j] + " ");
			System.out.println();
		}
		System.out.println();
		
		res = transposeMatrix.transpose(new int[][]{{1,2,3},{4,5,6}});
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[i].length; j++)
				System.out.print(res[i][j] + " ");
			System.out.println();
		}
	}

}
