import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PrimeNumberofSetBitsinBinaryRepresentation {

	public int countPrimeSetBits(int L, int R) {
		int count = 0;
		Set<Integer> set = new HashSet<>();
		Integer[] prime = new Integer[]{2, 3, 5, 7, 11, 13, 17 , 19};
		set.addAll(Arrays.asList(prime));
		for(int i = L; i <= R; i++){
			int num = i;
			int res = 0;
			while(num != 0){
				res += (num & 1);
				num >>>= 1;
			}
			if(set.contains(res)) count++;
		}
		return count;
	}
	
	public static void main(String[] args) {
		PrimeNumberofSetBitsinBinaryRepresentation prime = new PrimeNumberofSetBitsinBinaryRepresentation();
		System.out.println(prime.countPrimeSetBits(6, 10));
		System.out.println(prime.countPrimeSetBits(10, 15));
	}

}
