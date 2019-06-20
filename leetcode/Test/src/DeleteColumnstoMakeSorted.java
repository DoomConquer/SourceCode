public class DeleteColumnstoMakeSorted {

    public int minDeletionSize(String[] A) {
        if(A == null || A.length == 0) return 0;
        int min = 0;
        for(int i = 0; i < A[0].length(); i++){
        	for(int j = 1; j < A.length; j++){
            	if(A[j].charAt(i) < A[j - 1].charAt(i)){ min++; break; }
            }
        }
        return min;
    }
    
	public static void main(String[] args) {
		DeleteColumnstoMakeSorted deleteColumnstoMakeSorted = new DeleteColumnstoMakeSorted();
		System.out.println(deleteColumnstoMakeSorted.minDeletionSize(new String[]{"cba","daf","ghi"}));
		System.out.println(deleteColumnstoMakeSorted.minDeletionSize(new String[]{"a","b"}));
		System.out.println(deleteColumnstoMakeSorted.minDeletionSize(new String[]{"zyx","wvu","tsr"}));
	}

}
