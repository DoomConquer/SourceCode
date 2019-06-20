
public class FindtheDifference {

	public char findTheDifference(String s, String t) {
		int[] flag = new int[26];
		for(char ch : s.toCharArray()){
			flag[ch - 'a']++;
		}
		char target = 0;
		for(char ch : t.toCharArray()){
			flag[ch - 'a']--;
		}
		
		for(int i = 0; i < 26; i++){
			if(flag[i] < 0){
				target = (char) (i + 'a');
				break;
			}
		}
		return target;
	}
	
	public static void main(String[] args) {
		FindtheDifference difference = new FindtheDifference();
		System.out.println(difference.findTheDifference("a", "aa"));
	}

}
