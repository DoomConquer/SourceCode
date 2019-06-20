package toutiao;

import java.util.ArrayList;
import java.util.List;

public class RestoreIpAddresses {

    public List<String> restoreIpAddresses(String s) {
    	List<String> res = new ArrayList<>();
    	restore(s, 0, 3, res, new ArrayList<String>());
    	return res;
    }
    private void restore(String s, int index, int step, List<String> res, List<String> one){
    	int len = s.length();
    	if(one.size() == 4){
    		res.add(one.get(0) + "." + one.get(1) + "." + one.get(2) + "." + one.get(3));
    		return;
    	}
    	for(int i = 0; i < 3; i++){
    		if(len - index - i - 1 < step || len - index - i - 1 > 3 * step) continue;
    		if(index + i >= len) return;
    		String str = s.substring(index, index + i + 1);
    		if(str.startsWith("0") && str.length() != 1) continue;
    		if(Integer.parseInt(str) > 255) return;
    		one.add(str);
    		restore(s, index + i + 1, step - 1, res, one);
    		one.remove(one.size() - 1);
    	}
    }
    
	public static void main(String[] args) {
		RestoreIpAddresses restoreIpAddresses = new RestoreIpAddresses();
		System.out.println(restoreIpAddresses.restoreIpAddresses("25525511135"));
		System.out.println(restoreIpAddresses.restoreIpAddresses("2552551113"));
		System.out.println(restoreIpAddresses.restoreIpAddresses("71113"));
		System.out.println(restoreIpAddresses.restoreIpAddresses("010010"));
	}

}
