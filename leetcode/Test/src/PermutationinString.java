import java.util.Arrays;

public class PermutationinString {

	public boolean checkInclusion(String s1, String s2) {
		if(s2.length() < s1.length()) return false;
		int len = s1.length();
		int[] map = new int[26];
		for(int i = 0; i < len; i++)
			map[s1.charAt(i) - 'a']++;
		for(int i = 0; i + len <= s2.length();){
			int[] temp = Arrays.copyOf(map, 26);
			for(int j = 0; j < len; j++){
				if(temp[s2.charAt(i + j) - 'a'] <= 0){
					i++;
					break;
				}
				temp[s2.charAt(i + j) - 'a']--;
				if(j == len - 1) return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		PermutationinString permutation = new PermutationinString();
		System.out.println(permutation.checkInclusion("abc", "eidbaooo"));
	}
}
