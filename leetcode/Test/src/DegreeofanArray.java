
public class DegreeofanArray {
	public int findShortestSubArray(int[] nums) {
		if(nums.length <= 1)return nums.length;
        int[] set = new int[50000];
        int[] start = new int[50000];
        int[] end = new int[50000];
        for(int i = 0; i < nums.length; i++){
        	if(set[nums[i]] == 0){
        		start[nums[i]] = i;
        		end[nums[i]] = i;
        	} else
        		end[nums[i]] = i;
        	set[nums[i]]++;
        }
        int degree = 0;
        for(int n :set)
        	degree = Math.max(degree, n);
        int maxLen = Integer.MAX_VALUE;
        for(int i = 0; i < set.length; i++){
        	if(set[i] == degree){
    			maxLen = Math.min(maxLen, end[i] - start[i] + 1);
        	}
        }
        return maxLen;
    }
	
	public static void main(String[] args) {
		DegreeofanArray degree = new DegreeofanArray();
		System.out.println(degree.findShortestSubArray(new int[]{1, 2, 2, 3, 1}));
	}

}
