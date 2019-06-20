
public class UglyNumberII {

	public int nthUglyNumber(int n) {
		int[] ugly = new int[n];
		ugly[0] = 1;
		int curr = 1;
		for(int i = 0, j = 0, k = 0; curr < n; curr++){
			int pi = ugly[i] * 2;
			int pj = ugly[j] * 3;
			int pk = ugly[k] * 5;
			int min = Math.min(pi, Math.min(pj , pk));
			if(pi == min) i++;
			if(pj == min) j++;
			if(pk == min) k++;
			ugly[curr] = min;
		}
		return ugly[n - 1];
	}
	
	public static void main(String[] args) {
		UglyNumberII ugly = new UglyNumberII();
		System.out.println(ugly.nthUglyNumber(5));
	}

}
