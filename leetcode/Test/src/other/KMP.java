package other;

public class KMP {

	public int find(String source, String pattern){
		if(source == null || pattern == null || source.length() == 0 || pattern.length() == 0) return -1;
		int sLen = source.length();
		int pLen = pattern.length();
		int[] next = getNext(pattern);
		System.out.print("Next数组：  "); for(int i : next) System.out.print(i + "  "); System.out.println();
		int i = 0, j = 0;
		while(i < sLen && j < pLen){
			if(j == -1 || source.charAt(i) == pattern.charAt(j)){ // j = -1表示没有前缀匹配，将i后移一位，j从0开始继续查找
				i++; j++;
			}else j = next[j];
		}
		if(j == pLen) return i - j;
		return -1;
	}
	private int[] getNext(String pattern){
		int pLen = pattern.length();
		int[] next = new int[pLen];
		next[0] = -1;
		int j = 0, k = -1;
		while(j < pLen - 1){ // 计算1...pLen - 1的next
			if(k == -1 || pattern.charAt(k) == pattern.charAt(j)){
				j++; k++;
				if(pattern.charAt(k) != pattern.charAt(j)) next[j] = k;
				else next[j] = next[k];
			}else{
				k = next[k];
			}
		}
		return next;
	}
	
	public static void main(String[] args) {
		KMP kmp = new KMP();
		System.out.println(kmp.find("fdjkajjjfppPPPPPPPPPARTICIPATEINPARACHUTE", "PARTICIPATEINPARACHUTE"));
		System.out.println(kmp.find("fdjkajjjfppPPPPPPPPPARTICIPATEINPARACHUTE", "PARTICIPATEINPARACHiTE"));
		System.out.println(kmp.find("a", "abc"));
		System.out.println(kmp.find("abc", "a"));
		System.out.println(kmp.find("aaaaa", "aaaaa"));
		System.out.println(kmp.find("aaaaaababaaa", "abab"));
	}

}
