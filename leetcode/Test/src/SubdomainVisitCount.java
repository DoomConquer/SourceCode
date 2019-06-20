import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubdomainVisitCount {

	public List<String> subdomainVisits(String[] cpdomains) {
		List<String> res = new ArrayList<String>();
		Map<String, Integer> domainMap = new HashMap<String, Integer>();
		for(String cpdomain: cpdomains){
			String[] domainArr = cpdomain.split(" ");
			int count = Integer.parseInt(domainArr[0]);
			String subDomain = domainArr[1];
			while(subDomain.contains(".")){
				if(domainMap.containsKey(subDomain)){
					domainMap.put(subDomain, domainMap.get(subDomain) + count);
				}else{
					domainMap.put(subDomain, count);
				}
				subDomain = subDomain.substring(subDomain.indexOf(".") + 1);
			}
			if(domainMap.containsKey(subDomain)){
				domainMap.put(subDomain, domainMap.get(subDomain) + count);
			}else{
				domainMap.put(subDomain, count);
			}
		}
		for(Map.Entry<String, Integer> entry : domainMap.entrySet()){
			res.add(entry.getValue() + " " + entry.getKey());
		}
		return res;
	}
	
	public static void main(String[] args) {
		SubdomainVisitCount subDomain = new SubdomainVisitCount();
		System.out.println(subDomain.subdomainVisits(new String[]{"900 google.mail.com", "50 yahoo.com", "1 intel.mail.com", "5 wiki.org"}));
	}

}
