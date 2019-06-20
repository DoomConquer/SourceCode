/**
 * @author li_zhe
 * ̰��,���ǰ����ַ��Ѿ��ҵ�,��ôֻ����֮��һ��,ǰ���ҵ����ַ��Ժ���Ķ�����,���Բ����ٹ���ǰ���ҵ����ַ���.
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
