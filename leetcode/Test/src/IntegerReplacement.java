
public class IntegerReplacement {

	public int integerReplacement(int n) {
		int count = 0;
		if(n <= 0) return 0;
		while(n != 1){
			count++;
			if((n & 1) == 0){
				n >>>= 1;
			}else if(n == 3 || ((n >>> 1) & 1) == 0){
				n--;
			}else{
				n++;
			}
		}
		return count;
	}
	
	public static void main(String[] args) {
		IntegerReplacement replacement = new IntegerReplacement();
		System.out.println(replacement.integerReplacement(10));
		System.out.println(replacement.integerReplacement(8));
		System.out.println(replacement.integerReplacement(7));
		System.out.println(replacement.integerReplacement(3));
		System.out.println(replacement.integerReplacement(5));
		System.out.println(replacement.integerReplacement(1234));
		System.out.println(replacement.integerReplacement(100000000));
		System.out.println(replacement.integerReplacement(2147483647));
	}

}
