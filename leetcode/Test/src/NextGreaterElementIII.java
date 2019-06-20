import java.util.Arrays;

public class NextGreaterElementIII {

    public int nextGreaterElement(int n) {
        if(n <= 10) return -1;
        String s = String.valueOf(n);
        char[] sch = s.toCharArray();
        for(int i = sch.length - 2; i >= 0; i--){
        	for(int j = sch.length - 1; j > i; j--){
        		if(sch[i] < sch[j]){
        			char temp = sch[i];
        			sch[i] = sch[j];
        			sch[j] = temp;
        			Arrays.sort(sch, i + 1, sch.length);
        			long res = Long.parseLong(String.valueOf(sch));
        			if(res > Integer.MAX_VALUE) return -1;
        			return (int)res;
        		}
        	}
        }
        return -1;
    }
    
	public static void main(String[] args) {
		NextGreaterElementIII nextGreaterElementIII = new NextGreaterElementIII();
		System.out.println(nextGreaterElementIII.nextGreaterElement(12));
		System.out.println(nextGreaterElementIII.nextGreaterElement(21));
		System.out.println(nextGreaterElementIII.nextGreaterElement(1234));
		System.out.println(nextGreaterElementIII.nextGreaterElement(243));
		System.out.println(nextGreaterElementIII.nextGreaterElement(111));
		System.out.println(nextGreaterElementIII.nextGreaterElement(101));
		System.out.println(nextGreaterElementIII.nextGreaterElement(110));
		System.out.println(nextGreaterElementIII.nextGreaterElement(230241));
		System.out.println(nextGreaterElementIII.nextGreaterElement(241));
		System.out.println(nextGreaterElementIII.nextGreaterElement(1999999999));
	}

}
