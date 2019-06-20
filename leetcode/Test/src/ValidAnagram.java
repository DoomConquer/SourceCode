
public class ValidAnagram {

	public boolean isAnagram(String s, String t) {
		if(s.length() != t.length()) return false;
		int[] map = new int[26];
		for(int i = 0; i < s.length(); i++)
			map[s.charAt(i) - 'a']++;
		for(int i = 0; i < t.length(); i++)
			map[t.charAt(i) - 'a']--;
		for(int num : map)
			if(num != 0) return false;
		return true;
	}
	
	public static void main(String[] args) {
		ValidAnagram anagram = new ValidAnagram();
		System.out.println(anagram.isAnagram("anagram", "nagaram"));
	}

}
