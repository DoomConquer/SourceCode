
public class CountingBits {

	public int[] countBits(int num) {
		if(num < 0) return null;
		int[] bits = new int[num + 1];
		for(int i = 0; i <= num; i++){
			int count = 0;
			for (int j = 0; j < 32 ; j++) count += (i >> j) & 1;
			bits[i] = count;
		}
		return bits;
	}

	public static void main(String[] args) {
		CountingBits bits = new CountingBits();
		for (int num : bits.countBits(5))
			System.out.print(num + "  ");
	}

}
