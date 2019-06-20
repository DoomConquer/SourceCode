
public class BinaryNumberwithAlternatingBits {

	public boolean hasAlternatingBits(int n) {
		while(n != 0){
			if(((n & 1) == 0 && (n & 2) == 2) || ((n & 1) == 1 && (n & 2) == 0)) {
				n >>= 1;
				continue;
			}
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		BinaryNumberwithAlternatingBits alternating = new BinaryNumberwithAlternatingBits();
		System.out.println(alternating.hasAlternatingBits(5));
		System.out.println(alternating.hasAlternatingBits(7));
		System.out.println(alternating.hasAlternatingBits(11));
		System.out.println(alternating.hasAlternatingBits(10));
	}

}
