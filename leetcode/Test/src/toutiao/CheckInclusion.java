package toutiao;

public class CheckInclusion {

    public boolean checkInclusion(String s1, String s2) {
        if(s1.length() > s2.length()) return false;
        int[] letter = new int[26];
        for(int i = 0; i < s1.length(); i++) letter[s1.charAt(i) - 'a']++;
        int left = 0, right = 0;
        char[] sch = s2.toCharArray();
        while(right < s1.length()){
        	letter[sch[right++] - 'a']--;
        }
        if(isMatch(letter)) return true;
        while(right < s2.length()){
        	letter[sch[left++] - 'a']++;
        	letter[sch[right++] - 'a']--;
        	if(isMatch(letter)) return true;
        }
        return false;
    }
    private boolean isMatch(int[] letter){
    	for(int i : letter) if(i != 0) return false;
    	return true;
    }
    
	public static void main(String[] args) {
		CheckInclusion checkInclusion = new CheckInclusion();
		System.out.println(checkInclusion.checkInclusion("ab", "eidbaooo"));
		System.out.println(checkInclusion.checkInclusion("ab", "eidboaoo"));
		System.out.println(checkInclusion.checkInclusion("ab", "eidboaoobba"));
		System.out.println(checkInclusion.checkInclusion("eidboaoobba", "eidboaoobba"));
		System.out.println(checkInclusion.checkInclusion("", "eidboaoobba"));
		System.out.println(checkInclusion.checkInclusion("eidboaoobba", ""));
		System.out.println(checkInclusion.checkInclusion("a", "e"));
		System.out.println(checkInclusion.checkInclusion("aa", "eabaa"));
		System.out.println(checkInclusion.checkInclusion("az", "ezabaa"));
	}

}
