import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author li_zhe
 * ³¬Ê±
 */
public class AccountsMerge {

	public List<List<String>> accountsMerge(List<List<String>> accounts) {
		List<List<String>> res = new ArrayList<>();
		if(accounts == null || accounts.size() == 0) return res;
		int len = accounts.size();
		int[] visited = new int[len];
		List<String> names = new ArrayList<>();
		Map<Integer, Set<String>> emails = new HashMap<>();
		for(int i = 0; i < len; i++){
			List<String> list = accounts.get(i);
			Set<String> set = new HashSet<>();
			names.add(list.get(0));
			for(int j = 1; j < list.size(); j++)
				set.add(list.get(j));
			emails.put(i, set);
		}
		for(int i = 0; i < len; i++){
			if(visited[i] == 0){
				Set<String> set = new HashSet<>();
				set.addAll(emails.get(i));
				merge(emails, set, visited, len, i);
				List<String> account = new ArrayList<>(set);
				Collections.sort(account);
				account.add(0, names.get(i));
				res.add(account);
			}
		}
		return res;
	}
	private void merge(Map<Integer, Set<String>> emails, Set<String> account, int[] visited, int len, int i){
		for(int j = 0; j < len; j++){
			if(i != j && visited[j] == 0){
				Set<String> set = emails.get(j);
				for(String email : set){
					if(emails.get(i).contains(email)){
						visited[j] = 1;
						account.addAll(set);
						merge(emails, account, visited, len, j);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		AccountsMerge merge = new AccountsMerge();
		List<List<String>> accounts = new ArrayList<>();
		accounts.add(Arrays.asList(new String[]{"John", "johnsmith@mail.com", "john00@mail.com"}));
		accounts.add(Arrays.asList(new String[]{"John", "johnnybravo@mail.com"}));
		accounts.add(Arrays.asList(new String[]{"John", "johnsmith@mail.com", "john_newyork@mail.com"}));
		accounts.add(Arrays.asList(new String[]{"Mary", "mary@mail.com"}));
		List<List<String>> res = merge.accountsMerge(accounts);
		for(List<String> list : res)
			System.out.println(list);
	}

}
