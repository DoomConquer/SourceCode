public class StringWithoutAAAorBBB {

    public String strWithout3a3b(int A, int B) {
        StringBuilder sb = new StringBuilder();
        while(A > 0 && B > 0){
        	if(A > B){
        		sb.append("aa"); A -= 2;
        		sb.append('b'); B--;
        	}else if(A < B){
        		sb.append("bb"); B -= 2;
        		sb.append('a'); A--;
        	}else{
        		if(sb.length() > 0){
        			if(sb.charAt(sb.length() - 1) == 'a') { sb.append('b'); B--; }
        			else { sb.append('a'); A--; }
        		}else{
        			sb.append('a'); A--;
        		}
        	}
        }
        while(A > 0) { sb.append('a'); A--; }
        while(B > 0) { sb.append('b'); B--; }
        return sb.toString();
    }
    
	public static void main(String[] args) {
		StringWithoutAAAorBBB stringWithoutAAAorBBB = new StringWithoutAAAorBBB();
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(2, 6));
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(1, 2));
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(1, 3));
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(4, 1));
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(3, 3));
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(1, 1));
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(2, 2));
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(4, 4));
		System.out.println(stringWithoutAAAorBBB.strWithout3a3b(6, 2));
	}

}
