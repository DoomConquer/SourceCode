
public class ContainerWithMostWater {

	public int maxArea(int[] height) {
		int max = 0;
		for(int left = 0, right = height.length - 1; left < right;){
			int area = Math.min(height[left], height[right]) * (right - left);
			max = Math.max(max, area);
			if(height[left] < height[right]) left++;
			else right--;
		}
		return max;
	}
	
	public static void main(String[] args) {
		ContainerWithMostWater water = new ContainerWithMostWater();
		System.out.println(water.maxArea(new int[]{0,1,0,2,1,0,1,3,2,1,2,1}));
	}

}
