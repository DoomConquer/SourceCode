import java.util.ArrayList;
import java.util.List;

public class PalindromePartitioning {

	public List<List<String>> partition(String s) {
		List<List<String>> res = new ArrayList<List<String>>();
		part(res, new ArrayList<String>(), s, 0);
		return res;
			
	}
	private void part(List<List<String>> res, List<String> one, String s, int start){
		if(s == null) return;
		int len = s.length();
		if(start == len){
			res.add(new ArrayList<String>(one));
		}else{
			for(int i = start; i < len; i++){
				if(isPalindrome(s, start, i)){
					String s1 = s.substring(start, i + 1);
					one.add(s1);
					part(res, one, s, i + 1);
					one.remove(one.size() - 1);
				}
			}
		}
	}
	private boolean isPalindrome(String s, int left, int right){
		while(left < right){
			if(s.charAt(left) != s.charAt(right)) return false;
			left++;
			right--;
		}
		return true;
	}
	
	public static void main(String[] args) {
		PalindromePartitioning partition = new PalindromePartitioning();
		System.out.println(partition.partition("aabb"));
	}

}
