
public class NumberComplement {

	public int findComplement(int num) {
		int n = num;
		int mask = 1;
		n >>= 1;
		while(n != 0){
			n >>= 1;
			mask = (mask << 1) + 1;
		}
		return ~num & mask;
	}
	
	public static void main(String[] args) {
		NumberComplement number = new NumberComplement();
		System.out.println(number.findComplement(1));
		System.out.println(number.findComplement(5));
	}

}
