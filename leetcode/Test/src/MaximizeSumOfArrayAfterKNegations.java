import java.util.Arrays;

public class MaximizeSumOfArrayAfterKNegations {

    public int largestSumAfterKNegations(int[] A, int K) {
        Arrays.sort(A);
        for(int i = 0; i < K; i++){
        	if(A[i] <= 0) A[i] = -A[i];
        	else{
        		if((K - i) % 2 == 0) break;
        		else{
        			if(i > 0 && A[i] > A[i - 1]) A[i - 1] = -A[i - 1];
        			else A[i] = -A[i]; 
        			break; 
    			}
        	}
        }
        int sum = 0;
        for(int i = 0; i < A.length; i++) sum += A[i];
        return sum;
    }
    
	public static void main(String[] args) {
		MaximizeSumOfArrayAfterKNegations maximizeSumOfArrayAfterKNegations = new MaximizeSumOfArrayAfterKNegations();
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{4,2,3}, 1));
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{3,-1,0,2}, 3));
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{3,-1,0,0}, 3));
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{3,-1,0,2}, 5));
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{2,-3,-1,5,-4}, 1));
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{2,-3,-1,5,-4}, 2));
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{2,-3,-1,5,-4}, 3));
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{2,-3,-1,5,-4}, 4));
		System.out.println(maximizeSumOfArrayAfterKNegations.largestSumAfterKNegations(new int[]{2,-3,-1,5,-4}, 5));
	}

}
