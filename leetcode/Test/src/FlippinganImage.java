public class FlippinganImage {

    public int[][] flipAndInvertImage(int[][] A) {
        for(int i = 0; i < A.length; i++){
        	int left = 0, right = A[i].length - 1;
        	while(left <= right){
        		int temp = A[i][right] ^ 1;
        		A[i][right--] = A[i][left] ^ 1;
        		A[i][left++] = temp;
        	}
        }
        return A;
    }
    
	public static void main(String[] args) {
		FlippinganImage flippinganImage = new FlippinganImage();
		int[][] res = flippinganImage.flipAndInvertImage(new int[][]{{1,1,0,0},{1,0,0,1},{0,1,1,1},{1,0,1,0}});
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[i].length; j++){
				System.out.print(res[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		res = flippinganImage.flipAndInvertImage(new int[][]{{1,1,0},{1,0,1},{0,0,0}});
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[i].length; j++){
				System.out.print(res[i][j] + " ");
			}
			System.out.println();
		}
	}

}
