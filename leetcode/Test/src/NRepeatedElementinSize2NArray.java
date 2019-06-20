public class NRepeatedElementinSize2NArray {

	// O(n)时间复杂度，O(1)空间复杂度
    public int repeatedNTimes(int[] A) {
        for(int i = 2; i < A.length; i++){
        	if(A[i] == A[i - 1] || A[i] == A[i - 2]) return A[i];
        }
        return A[0];
    }
    
	public static void main(String[] args) {
		NRepeatedElementinSize2NArray nRepeatedElementinSize2NArray = new NRepeatedElementinSize2NArray();
		System.out.println(nRepeatedElementinSize2NArray.repeatedNTimes(new int[]{1,2,3,3}));
		System.out.println(nRepeatedElementinSize2NArray.repeatedNTimes(new int[]{2,1,2,5,3,2}));
		System.out.println(nRepeatedElementinSize2NArray.repeatedNTimes(new int[]{5,1,5,2,5,3,5,4}));
	}

}
