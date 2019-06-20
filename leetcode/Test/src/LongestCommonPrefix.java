
public class LongestCommonPrefix {

	public String longestCommonPrefix(String[] strs) {
		if(strs == null || strs.length == 0) return "";
		int i = 0; boolean flag = true;
		out: while(flag){
			if(i >= strs[0].length()){
				flag = false;
				break out;
			}
			char ch = strs[0].charAt(i);
			for(int j = 1; j < strs.length; j++){
				if(i >= strs[j].length() || strs[j].charAt(i) != ch){
					flag = false;
					break out;
				}
			}
			i++;
		}
		
		return strs[0].substring(0, i);
	}
	
	public static void main(String[] args) {
		LongestCommonPrefix longest = new LongestCommonPrefix();
		System.out.println(longest.longestCommonPrefix(new String[]{"flower","flow","flight"}));
		System.out.println(longest.longestCommonPrefix(new String[]{"dog","racecar","car"}));
		System.out.println(longest.longestCommonPrefix(new String[]{" "}));
	}

}
