package toutiao;

public class LongestCommonPrefix {

    public String longestCommonPrefix(String[] strs) {
    	if(strs == null || strs.length == 0) return "";
    	int index = 0;
    	while(true){
	    	for(int i = 0; i < strs.length; i++){
	    		if(strs[i].length() <= index) return strs[0].substring(0, index);
	    		char ch = strs[0].charAt(index);
	    		if(strs[i].charAt(index) != ch) return strs[0].substring(0, index);
	    	}
	    	index++;
    	}
    }
    
	public static void main(String[] args) {
		LongestCommonPrefix longestCommonPrefix = new LongestCommonPrefix();
		System.out.println(longestCommonPrefix.longestCommonPrefix(new String[]{"flower","flow","flight"}));
		System.out.println(longestCommonPrefix.longestCommonPrefix(new String[]{"flower","flower","flower"}));
		System.out.println(longestCommonPrefix.longestCommonPrefix(new String[]{"flower","flowerq","flower"}));
		System.out.println(longestCommonPrefix.longestCommonPrefix(new String[]{"dog","racecar","car"}));
		System.out.println(longestCommonPrefix.longestCommonPrefix(new String[]{"","a","a"}));
		System.out.println(longestCommonPrefix.longestCommonPrefix(new String[]{"a","aa","aa"}));
	}

}
