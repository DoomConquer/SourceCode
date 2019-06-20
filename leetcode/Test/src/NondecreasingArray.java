public class NondecreasingArray {

    public boolean checkPossibility(int[] nums) {
        int count = 0;
        int len = nums.length;
        for(int i = 0; i < len - 1; i++){
        	if(nums[i] > nums[i + 1]){
        		count++;
        		if(count > 1) return false;
        		if(i > 0){
        			if(nums[i - 1] > nums[i + 1]) nums[i + 1] = nums[i];
        		}
        	}
        }
        return true;
    }
    
	public static void main(String[] args) {
		NondecreasingArray nondecreasingArray = new NondecreasingArray();
		System.out.println(nondecreasingArray.checkPossibility(new int[]{4,2,3}));
		System.out.println(nondecreasingArray.checkPossibility(new int[]{4,2,1}));
		System.out.println(nondecreasingArray.checkPossibility(new int[]{3,4,2,3}));
		System.out.println(nondecreasingArray.checkPossibility(new int[]{2,3,3,2,4}));
		System.out.println(nondecreasingArray.checkPossibility(new int[]{2,3,3,2,2}));
		System.out.println(nondecreasingArray.checkPossibility(new int[]{2,2,3,2,2}));
		System.out.println(nondecreasingArray.checkPossibility(new int[]{3,3,3,1,2}));
		System.out.println(nondecreasingArray.checkPossibility(new int[]{1,2,4,5,3}));
	}

}
