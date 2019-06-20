
public class NumberofOneBits {

	public int hammingWeight(int n) {
		int count = 0;
		for(int i = 0; i < 32; i++)
			count += n>>i & 1;
		return count;
	}
	
	public static void main(String[] args) {
		NumberofOneBits bits = new NumberofOneBits();
		System.out.println(bits.hammingWeight(Integer.MAX_VALUE));
	}

}
