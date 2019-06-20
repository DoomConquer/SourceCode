
public class ImplementstrStr {

	public int strStr(String haystack, String needle) {
		if(needle.isEmpty()) return 0;
		if(haystack.isEmpty()) return -1;
		int p = 0, q = 0;
		int start = 0;
		boolean first = true;
		while(p < haystack.length() && q < needle.length()){
			if(haystack.charAt(p) == needle.charAt(q)){
				if(first){
					start = p;
					first = false;
				}
				p++;
				q++;
			} else {
				p = start + 1;
				start = p;
				first = true;
				q = 0;
			}
		}
		if(q != needle.length()) return -1;
		return p - q;
	}
	
	public static void main(String[] args) {
		ImplementstrStr str = new ImplementstrStr();
		System.out.println(str.strStr("mississippi", "issip"));
	}

}
