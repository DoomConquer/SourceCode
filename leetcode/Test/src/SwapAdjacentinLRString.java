
public class SwapAdjacentinLRString {

	 public boolean canTransform(String start, String end) {
		 int startLen = start.length();
		 int endLen = end.length();
		 if(startLen != endLen) return false;
		 int s = 0, e = 0;
		 while(s < startLen && e < endLen){
			 while(s < startLen && start.charAt(s) == 'X') s++;
			 while(e < endLen && end.charAt(e) == 'X') e++;
			 if(s == startLen || e == endLen) break;
			 if(start.charAt(s) != end.charAt(e)) return false;
			 if(start.charAt(s) == 'R' && s > e) return false;
			 if(start.charAt(s) == 'L' && s < e) return false;
			 s++; e++;
		 }
		 while(s < startLen && start.charAt(s) == 'X') s++; 
		 while(e < endLen && end.charAt(e) == 'X') e++;
		 return s == e;
	 }
	 
	public static void main(String[] args) {
		SwapAdjacentinLRString swap = new SwapAdjacentinLRString();
		System.out.println(swap.canTransform("XL", "XL"));
		System.out.println(swap.canTransform("XL", "LX"));
		System.out.println(swap.canTransform("XXXR", "XRXX"));
	}

}
