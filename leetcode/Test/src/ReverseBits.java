
public class ReverseBits {

	public int reverseBits(int n) {
		long m = 0;
		for(int i = 0; i < 32; i++){
			m = (m << 1) | ((n >>> i) & 1);
		}
		return (int)m;
	}
	
	public static void main(String[] args) {
		ReverseBits reverse = new ReverseBits();
		System.out.println(reverse.reverseBits(1));
	}

}
