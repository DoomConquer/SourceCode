
public class ConvertaNumbertoHexadecimal {

	public String toHex(int num) {
		StringBuilder sb = new StringBuilder();
		if(num == 0) sb.append(0);
		while(num != 0){
			int n = num & 0xf;
			if(n < 10) sb.append(n);
			else sb.append((char)(n - 10 + 'a'));
			num >>>= 4;
		}
		return sb.reverse().toString();
	}
	
	public static void main(String[] args) {
		ConvertaNumbertoHexadecimal convert = new ConvertaNumbertoHexadecimal();
		System.out.println(convert.toHex(-1));
		System.out.println(convert.toHex(8));
		System.out.println(convert.toHex(256));
		System.out.println(convert.toHex(26));
	}

}
