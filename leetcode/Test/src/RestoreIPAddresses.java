import java.util.ArrayList;
import java.util.List;

public class RestoreIPAddresses {

    public List<String> restoreIpAddresses(String s) {
    	List<String> res = new ArrayList<>();
    	restore(s, new ArrayList<>(), res, 3);
    	return res;
    }
    private void restore(String s, List<String> list, List<String> res, int count){
    	if(list.size() == 4){
    		res.add(list.get(0) + "." + list.get(1) + "." + list.get(2) + "." + list.get(3));
    	}
    	int len = s.length();
    	for(int i = 1; i <= 3; i++){
    		if(len - i <= 3 * count && len - i >= count){
	    		String ss = s.substring(0, i);
	    		if(ss.startsWith("0") && ss.length() != 1) continue;
	    		if(Integer.parseInt(ss) <= 255){
		    		list.add(ss);
		    		restore(s.substring(i), list, res, count - 1);
		    		list.remove(list.size() - 1);
	    		}
    		}
    	}
    }
    
	public static void main(String[] args) {
		RestoreIPAddresses restoreIPAddresses = new RestoreIPAddresses();
		System.out.println(restoreIPAddresses.restoreIpAddresses("25525511135"));
		System.out.println(restoreIPAddresses.restoreIpAddresses("2552551113"));
		System.out.println(restoreIPAddresses.restoreIpAddresses("71113"));
		System.out.println(restoreIPAddresses.restoreIpAddresses("010010"));
	}

}
