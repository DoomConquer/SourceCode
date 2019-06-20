public class MinimumMovestoEqualArrayElements {
	
    public int minMoves(int[] nums) {
        long sum = 0;
		int min = Integer.MAX_VALUE;
        for(int num : nums){
        	if(num < min) min = num;
        	sum += num;
        }
        return (int) (sum - nums.length * min);
    }

	public static void main(String[] args) {
		MinimumMovestoEqualArrayElements minimumMovestoEqualArrayElements = new MinimumMovestoEqualArrayElements();
		System.out.println(minimumMovestoEqualArrayElements.minMoves(new int[]{1,2,3}));
		System.out.println(minimumMovestoEqualArrayElements.minMoves(new int[]{1,2,100}));
	}
}
