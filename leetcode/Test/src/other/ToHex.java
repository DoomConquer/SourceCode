package other;

public class ToHex {
	
	public String toHex(int n) throws Exception{
		StringBuilder sb = new StringBuilder();
		if(n == 0) return String.valueOf(0);
		int m = n;
		while(n > 0){
			sb.insert(0, toHexString(n % 16));
			n /= 16;
		}
		if(!Integer.toHexString(m).equals(sb.toString())){
			System.out.println("²»Ò»ÖÂ£º" + sb.toString() + "   " + Integer.toHexString(m));
		}
		return sb.toString();
	}
	private String toHexString(int n) throws Exception{
		if(n >= 0 && n < 10) return String.valueOf(n);
		switch(n){
		case 10:
			return "a";
		case 11:
			return "b";
		case 12:
			return "c";
		case 13:
			return "d";
		case 14:
			return "e";
		case 15:
			return "f";
		default:
			throw new Exception("error");
		}
	}
	
	public static void main(String[] args) throws Exception {
		ToHex hex = new ToHex();
		System.out.println(hex.toHex(20));
		System.out.println(hex.toHex(2));
		System.out.println(hex.toHex(0));
		System.out.println(hex.toHex(16));
		System.out.println(hex.toHex(17));
		System.out.println(hex.toHex(1722));
		System.out.println(hex.toHex(2000000000));
		System.out.println(hex.toHex(Integer.MAX_VALUE));
	}
}
