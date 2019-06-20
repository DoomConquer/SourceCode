public class ToLowerCase {

	public String toLowerCase(String str) {
        if(str == null) return null;
        char[] ch = str.toCharArray();
        for(int i = 0; i < ch.length; i++){
        	if(ch[i] >= 'A' && ch[i] <= 'Z') ch[i] = (char) (ch[i] - 'A' + 'a');
        }
        return new String(ch);
    }

	public static void main(String[] args) {
		ToLowerCase toLowerCase = new ToLowerCase();
		System.out.println(toLowerCase.toLowerCase("LOVELY"));
		System.out.println(toLowerCase.toLowerCase("LOwww"));
		System.out.println(toLowerCase.toLowerCase("Hello"));
		System.out.println(toLowerCase.toLowerCase("hello"));
		System.out.println(toLowerCase.toLowerCase("hed12@#1lLLKo"));
	}
}
