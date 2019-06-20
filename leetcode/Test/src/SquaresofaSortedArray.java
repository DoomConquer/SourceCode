public class SquaresofaSortedArray {
	public int[] sortedSquares(int[] A) {
        if(A == null || A.length == 0) return A;
        int index = 0;
        for(; index < A.length; index++){
        	if(A[index] >= 0) break;
        }
        int back = index;
        int[] B = new int[A.length];
        int bIndex = 0;
        index--;
        while(index >= 0 && back < A.length){
        	if(Math.abs(A[index]) > A[back]) B[bIndex++] = (int) Math.pow(A[back++], 2);
        	else B[bIndex++] = (int) Math.pow(A[index--], 2);
        }
        while(index >= 0){
        	B[bIndex++] = (int) Math.pow(A[index--], 2);
        }
        while(back < A.length){
        	B[bIndex++] = (int) Math.pow(A[back++], 2);
        }
        // for(int i = 0; i < B.length; i++) System.out.print(B[i] + " ");System.out.println();
        return B;
    }
	
	public static void main(String[] args) {
		SquaresofaSortedArray squaresofaSortedArray = new SquaresofaSortedArray();
		squaresofaSortedArray.sortedSquares(new int[]{-4});
		squaresofaSortedArray.sortedSquares(new int[]{-4,-1,0});
		squaresofaSortedArray.sortedSquares(new int[]{-4,-1,0,3,10});
		squaresofaSortedArray.sortedSquares(new int[]{-7,-3,2,3,11});
	}

}
