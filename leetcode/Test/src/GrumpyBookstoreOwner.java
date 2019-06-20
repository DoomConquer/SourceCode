public class GrumpyBookstoreOwner {

    public int maxSatisfied(int[] customers, int[] grumpy, int X) {
    	int len = customers.length;
        int[] sum = new int[len + 1];
        int[] satisfied = new int[len + 1];
        for(int i = 0; i < len; i++){
        	sum[i + 1] = sum[i] + customers[i];
        	satisfied[i + 1] = satisfied[i] + (grumpy[i] == 0 ? customers[i] : 0);
        }
        if(X >= len) return sum[len];
        int max = 0;
        for(int i = 0; i + X <= len; i++){
        	max = Math.max(max, satisfied[i] + sum[i + X] - sum[i] + satisfied[len] - satisfied[i + X]);
        }
        return max;
    }
    
	public static void main(String[] args) {
		GrumpyBookstoreOwner grumpyBookstoreOwner = new GrumpyBookstoreOwner();
		System.out.println(grumpyBookstoreOwner.maxSatisfied(new int[]{2,6,6,9}, new int[]{0,0,1,1}, 1));
		System.out.println(grumpyBookstoreOwner.maxSatisfied(new int[]{1,0,1,2,1,1,7,5}, new int[]{0,1,0,1,0,1,0,1}, 3));
		System.out.println(grumpyBookstoreOwner.maxSatisfied(new int[]{1,0,1,2,1,1,7,5}, new int[]{0,1,0,1,0,1,0,0}, 3));
	}
}
