import java.util.Arrays;
import java.util.Comparator;

public class ReorganizeString {

	public String reorganizeString(String S) {
		if(S.isEmpty() || S.length() == 1) return S;
		char[] s = S.toCharArray();
		Count[] count = new Count[26];
		for(int i = 0; i < 26; i++)
			count[i] = new Count();
		for(char ch : s){
			count[ch - 'a'].sum++;
			count[ch - 'a'].ch = ch;
		}
		Arrays.sort(count, new Comparator<Count>(){

			@Override
			public int compare(Count o1, Count o2) {
				return o1.sum > o2.sum ? 1 : -1;
			}
			
		});
		int sum = 0;
		for(int i = count.length - 2; i >= 0; i--){
			sum += count[i].sum;
		}
		if(sum < count[count.length - 1].sum - 1) return "";
		StringBuilder sb = new StringBuilder();
		for(int i = count.length - 1; i >= 0; i--){
			if(count[i].sum == 0) break;
			while(count[i].sum-- > 0) sb.append(count[i].ch);
		}
		StringBuilder res = new StringBuilder();
		for(int i = 0, j = (s.length - 1) / 2 + 1; i <= (s.length - 1) / 2; i++, j++){
			res.append(sb.charAt(i));
			if(j < s.length)
				res.append(sb.charAt(j));
		}
		return res.toString();
	}
	class Count{
		int sum;
		char ch;
	}
	
	public static void main(String[] args) {
		ReorganizeString reorganize = new ReorganizeString();
		System.out.println(reorganize.reorganizeString("aabbccddww"));
	}

}
