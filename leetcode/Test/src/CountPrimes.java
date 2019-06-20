
public class CountPrimes {

	public int countPrimes(int n) {
		if(n <= 1) return 0;
		int count = 0;
		boolean[] flag = new boolean[n];
		for(int i = 2; i * i < n; i++){
			if(flag[i]) continue;
			for(int j = i * i; j < n; j += i)
				flag[j] = true;
		}
		for(int i = 2; i < n; i++)
			if(!flag[i]) count++;
		return count;
	}
	
	public static void main(String[] args) {
		CountPrimes primes = new CountPrimes();
		System.out.println(primes.countPrimes(2));
	}

}
