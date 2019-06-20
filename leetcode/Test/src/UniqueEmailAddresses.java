import java.util.HashSet;
import java.util.Set;

public class UniqueEmailAddresses {
	
	public int numUniqueEmails(String[] emails) {
        Set<String> emailSet = new HashSet<String>();
        for(String email : emails){
        	String[] emailNames = email.split("@");
        	String localName = emailNames[0];
        	StringBuilder sb = new StringBuilder();
        	for(int i = 0; i < localName.length(); i++){
        		if(localName.charAt(i) == '+') break;
        		if(localName.charAt(i) == '.') continue;
        		sb.append(localName.charAt(i));
        	}
        	emailSet.add(sb.append("@").append(emailNames[1]).toString());
        }
        return emailSet.size();
    }
	
	public static void main(String[] args) {
		UniqueEmailAddresses uniqueEmailAddresses = new UniqueEmailAddresses();
		System.out.println(uniqueEmailAddresses.numUniqueEmails(new String[]{"test.email+alex@leetcode.com","test.e.mail+bob.cathy@leetcode.com","testemail+david@lee.tcode.com"}));
	}
}
