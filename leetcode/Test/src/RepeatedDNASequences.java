import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepeatedDNASequences {

	public List<String> findRepeatedDnaSequences(String s) {
		Set<String> set = new HashSet<>();
		List<String> res = new ArrayList<>();
		for(int i = 0; i <= s.length() - 10; i++){
			String str = s.substring(i, i + 10);
			if(set.contains(str) && !res.contains(str)) { res.add(str); continue; }
			set.add(str);
		}
		return res;
	}
	
	public static void main(String[] args) {
		RepeatedDNASequences repeate = new RepeatedDNASequences();
		for(String s : repeate.findRepeatedDnaSequences("AAAAACCCCCAAAAACCCCCCAAAAAGGGTTT"))
			System.out.println(s);
		for(String s : repeate.findRepeatedDnaSequences("AAAAAAAAAAAA"))
			System.out.println(s);
	}

}
