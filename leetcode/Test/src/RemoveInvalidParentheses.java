import java.util.ArrayList;
import java.util.List;

public class RemoveInvalidParentheses {

	public List<String> removeInvalidParentheses(String s) {
		List<String> res = new ArrayList<>();
		remove(s, 0, 0, new char[]{'(',')'}, res);
		return res;
	}
	private void remove(String s, int left, int right, char[] par, List<String> res){
		int count = 0;
		for(int i = right; i < s.length(); i++){
			if(s.charAt(i) == par[0]) count++;
			if(s.charAt(i) == par[1]) count--;
			if(count >= 0) continue;
			for(int j = left; j <= i; j++){
				if(s.charAt(j) == par[1] && (j == left || s.charAt(j - 1) != par[1])){
					remove(s.substring(0, j) + s.substring(j + 1), j, i, par, res);
				}
			}
			return;
		}
		String s1 = new StringBuilder(s).reverse().toString();
		if(par[0] == '(')
			remove(s1, 0, 0, new char[]{')','('}, res);
		else
			res.add(s1);
	}
	
	public static void main(String[] args) {
		RemoveInvalidParentheses parenttheses = new RemoveInvalidParentheses();
		System.out.println(parenttheses.removeInvalidParentheses(")(a)()))"));
	}

}
