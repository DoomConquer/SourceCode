
public class ClimbingStairs {

	public int climbStairs(int n) {
		if(n <= 0) return 0;
		if(n == 1) return 1;
		if(n == 2) return 2;
		int[] f = new int[n + 1];
		f[1] = 1; f[2] = 2;
		for(int i = 3; i <= n; i++)
			f[i] = f[i - 2] + f[i - 1];
		return f[n];
	}
	
	public static void main(String[] args) {
		ClimbingStairs climb = new ClimbingStairs();
		System.out.println(climb.climbStairs(3));
		System.out.println(climb.climbStairs(20));
	}

}
