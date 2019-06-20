public class PeakIndexinaMountainArray {

    public int peakIndexInMountainArray(int[] A) {
        int left = 0, right = A.length - 1;
        while(left < right){
        	int mid = (left + right) / 2;
        	if(A[mid] > A[mid - 1] && A[mid] > A[mid + 1]) return mid;
        	if(A[mid] < A[mid - 1]) right = mid;
        	else left = mid;
        }
        return -1;
    }
    
	public static void main(String[] args) {
		PeakIndexinaMountainArray peakIndexinaMountainArray = new PeakIndexinaMountainArray();
		System.out.println(peakIndexinaMountainArray.peakIndexInMountainArray(new int[]{0,1,0}));
		System.out.println(peakIndexinaMountainArray.peakIndexInMountainArray(new int[]{0,2,1,0}));
		System.out.println(peakIndexinaMountainArray.peakIndexInMountainArray(new int[]{0,2,3,2,1,0}));
		System.out.println(peakIndexinaMountainArray.peakIndexInMountainArray(new int[]{0,2,3,2,1,0}));
		System.out.println(peakIndexinaMountainArray.peakIndexInMountainArray(new int[]{3,4,11,15,18,24,30,36,44,57,62,64,68,88,90,91,99,100,81,74,61,55,49,39,23,15,11}));
	}

}
