package toutiao;

public class Trap {

    public int trap(int[] height) {
        int water = 0;
        int leftMax = 0, rightMax = 0;
        int left = 0, right = height.length - 1;
        while(left < right){
        	leftMax = Math.max(leftMax, height[left]);
        	rightMax = Math.max(rightMax, height[right]);
        	if(height[left] < height[right]){
        		water += Math.min(leftMax, rightMax) - height[left];
        		left++;
        	}else{
        		water += Math.min(leftMax, rightMax) - height[right];
        		right--;
        	}
        }
        return water;
    }
    
	public static void main(String[] args) {
		Trap trap = new Trap();
		System.out.println(trap.trap(new int[]{0,1,0,2,1,0,1,3,2,1,2,1}));
	}

}
