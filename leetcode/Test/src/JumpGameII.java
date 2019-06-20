
public class JumpGameII {
	public int jump(int[] nums) {
        int curLongest = 0;
        int temp = 0;
        int steps = 0;
        for(int i = 0; i < nums.length; i++){
        	if(i > temp){
        		temp = curLongest;
        		steps++;
        	}
        	curLongest = Math.max(curLongest, i + nums[i]);
        }
        return steps;
    }
	
	public static void main(String[] args) {
		JumpGameII jump = new JumpGameII();
		int[] nums = new int[]{2,3,1,1,4};
		System.out.println(jump.jump(nums));
	}
}
