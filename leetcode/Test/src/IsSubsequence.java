/**
 * @author li_zhe
 * 贪心,如果前面的字符已经找到,那么只用找之后一个,前面找到的字符对后面的都满足,所以不用再关心前面找到的字符串.
 */
public class IsSubsequence {

	public boolean isSubsequence(String s, String t) {
		if(s == null || s.length() == 0) return true;
		if(t == null || t.length() == 0) return false;
		if(s.length() >= t.length()) return false;
		char[] sch = s.toCharArray();
		char[] tch = t.toCharArray();
		int index = 0;
		for(int i = 0; i < t.length(); i++){
			if(index == s.length()) return true;
			if(sch[index] == tch[i]){
				index++;
			}
		}
		return index == s.length();
	}
	
	public static void main(String[] args) {
		IsSubsequence sub = new IsSubsequence();
		System.out.println(sub.isSubsequence("abc", "ahbgdc"));
		System.out.println(sub.isSubsequence("aec", "ahbgdcec"));
		System.out.println(sub.isSubsequence("axc", "ahbgdc"));
		System.out.println(sub.isSubsequence("aaa", "ahbgdc"));
		System.out.println(sub.isSubsequence("c", "ahbgdc"));
		System.out.println(sub.isSubsequence("c", "c"));
		System.out.println(sub.isSubsequence("cc", "c"));
		System.out.println(sub.isSubsequence("", ""));
	}

}
