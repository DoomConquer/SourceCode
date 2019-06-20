public class PartitionArrayIntoThreePartsWithEqualSum {

    public boolean canThreePartsEqualSum(int[] A) {
    	if(A == null || A.length < 3) return false;
        int[] sums = new int[A.length + 1]; 
        for(int i = 0; i < A.length; i++){
        	sums[i + 1] = sums[i] + A[i];
        }
        if(sums[A.length] % 3 != 0) return false;
        int part = sums[A.length] / 3;
        int left = 0, right = A.length - 1;
        while(left < right){
        	if(sums[left + 1] != part){ left++; continue; }
        	if(sums[A.length] - sums[right] != part){ right--; continue; }
        	if(sums[right] - sums[left + 1] != part || right == left + 1) return false; // ×¢ÒâÌõ¼þright == left + 1
        	return true;
        }
        return false;
    }
    
	public static void main(String[] args) {
		PartitionArrayIntoThreePartsWithEqualSum partitionArrayIntoThreePartsWithEqualSum = new PartitionArrayIntoThreePartsWithEqualSum();
		System.out.println(partitionArrayIntoThreePartsWithEqualSum.canThreePartsEqualSum(new int[]{0,2,1,-6,6,-7,9,1,2,0,1}));
		System.out.println(partitionArrayIntoThreePartsWithEqualSum.canThreePartsEqualSum(new int[]{0,2,1,-6,6,7,9,-1,2,0,1}));
		System.out.println(partitionArrayIntoThreePartsWithEqualSum.canThreePartsEqualSum(new int[]{3,3,6,5,-2,2,5,1,-9,4}));
		System.out.println(partitionArrayIntoThreePartsWithEqualSum.canThreePartsEqualSum(new int[]{3,-3,0}));
		System.out.println(partitionArrayIntoThreePartsWithEqualSum.canThreePartsEqualSum(new int[]{0,0,0,0,0,0}));
		System.out.println(partitionArrayIntoThreePartsWithEqualSum.canThreePartsEqualSum(new int[]{2,-2,2,-2,2,-2,2,-2,2,-2}));
	}

}
