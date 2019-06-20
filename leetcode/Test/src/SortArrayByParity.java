public class SortArrayByParity {

    public int[] sortArrayByParity(int[] A) {
        int i = 0, j = A.length - 1;
        while(i < j){
        	while(i < j && (A[i] & 1) == 0) i++;
        	while(i < j && (A[j] & 1) == 1) j--;
        	if(i < j){
        		int temp = A[i];
        		A[i] = A[j];
        		A[j] = temp;
        	}
        }
        return A;
    }
    
	public static void main(String[] args) {
		SortArrayByParity SortArrayByParity = new SortArrayByParity();
		int[] res = SortArrayByParity.sortArrayByParity(new int[]{3,1,2,5,6});
		for(int num : res) System.out.print(num + "  ");
	}

}
